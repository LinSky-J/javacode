package gc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;

/**
 * JVM 面试专题 - Minor GC、Major GC 与 Full GC 深度对比与 Full GC 触发场景剖析
 *
 * 本类针对经典高频面试题进行深度解析与实战演示：
 * 1. minorGC、majorGC、fulIGC的区别，什么场景触发fullGC
 *
 * 核心考点：
 * - GC分类标准：Minor GC (Young GC)、Major GC (Old GC)、Mixed GC (G1特有)、Full GC (整堆收集)
 * - 各类GC的收集区域、回收算法、发生频率与STW耗时对比
 * - 空间分配担保机制（Handle Promotion Failure）工作流程
 * - Full GC 触发的六大核心场景深度剖析与线上排查策略
 */
public class MinorMajorFullGcDifferencesAndTriggers {

    /*
     * =========================================================================
     * 面试核心问答整理
     * =========================================================================
     *
     * 问题：minorGC、majorGC、fulIGC的区别，什么场景触发fullGC？
     *
     * -------------------------------------------------------------------------
     * 一、Minor GC、Major GC、Mixed GC 与 Full GC 的概念与区别
     * -------------------------------------------------------------------------
     *
     * 1. Minor GC（又称 Young GC）：
     *    - 收集范围：仅针对新生代（Eden区以及其中一个Survivor区，如From Survivor）。
     *    - 回收算法：标记-复制算法（Mark-Copy）。存活对象被复制到另一个Survivor区或直接晋升到老年代。
     *    - 触发条件：新生代的 Eden 区空间不足以分配新对象时立即触发。
     *    - 执行特点：非常频繁（因为大部分对象生命周期极短，朝生夕灭），回收速度极快（通常在毫秒级），
     *      虽然也会发生 STW（Stop The World），但停顿时间通常极其微弱，对系统吞吐量和响应速度影响较小。
     *
     * 2. Major GC（又称 Old GC）：
     *    - 收集范围：仅针对老年代（Old Generation / Tenured Generation）的垃圾回收。
     *    - 回收算法：标记-清除（Mark-Sweep）或 标记-整理（Mark-Compact）算法。
     *    - 注意混淆点：在很多技术文档和口头交流中，Major GC 经常与 Full GC 混用。但在 HotSpot JVM 官方严谨定义中：
     *      * 只有 CMS 收集器会有单独收集老年代的行为（Concurrent Mark Sweep 属于单纯的 Old GC）。
     *      * 在大部分其他收集器（如 Parallel Scavenge / Serial）中，老年代满通常直接触发的是 Full GC。
     *      * Major GC 的耗时通常比 Minor GC 慢 10 倍以上，STW 停顿时间明显更长。
     *
     * 3. Mixed GC（混合收集，G1 收集器特有）：
     *    - 收集范围：回收整个新生代（Young Region）以及部分老年代（Old Region）和巨型大对象区（Humongous Region）。
     *    - 触发条件：老年代占用空间超过整堆的阈值（通过 -XX:InitiatingHeapOccupancyPercent 设定，默认45%）时启动全局并发标记，随后进入 Mixed GC 周期。
     *
     * 4. Full GC（整堆收集）：
     *    - 收集范围：回收整个 Java 堆（新生代、老年代）以及方法区/元空间（Metaspace / PermGen）。
     *    - 回收算法：通常伴随老年代的标记-整理或标记-清除，并清空新生代、清理元空间的类元数据与废弃常量。
     *    - 执行特点：STW 停顿时间最长（可能从几百毫秒到数秒甚至数分钟），是 JVM 调优中重点避免或降低频率的目标。
     *
     * -------------------------------------------------------------------------
     * 二、维度对比表
     * -------------------------------------------------------------------------
     * | 指标维度     | Minor GC (Young GC)     | Major GC (Old GC)       | Full GC                 |
     * |-------------|-------------------------|-------------------------|-------------------------|
     * | 回收区域     | 新生代 (Eden + Survivor)| 老年代                  | 新生代 + 老年代 + 元空间|
     * | 常用算法     | 标记-复制               | 标记-清除 / 标记-整理   | 标记-整理 / 标记-清除   |
     * | 触发频率     | 极高（几秒或几分钟一次）| 较低                    | 极低（健康系统几天或更长）|
     * | STW 停顿时间| 极短（几毫秒至几十毫秒）| 较长（通常比Young GC慢）| 最长（几百毫秒至数秒）  |
     * | 对系统影响   | 轻微                    | 较大                    | 极高，可能引发系统假死  |
     *
     * -------------------------------------------------------------------------
     * 三、什么场景下会触发 Full GC？（高频考点，务必全方位记忆）
     * -------------------------------------------------------------------------
     *
     * 场景 1：System.gc() 或 Runtime.getRuntime().gc() 的显式调用
     *    - 说明：代码中显式建议 JVM 启动 Full GC。虽然 JVM 规范不保证立即执行，但 HotSpot 默认会发起 Full GC。
     *    - 防范调优：生产环境强烈建议添加 JVM 参数 `-XX:+DisableExplicitGC`，忽略代码中的显式调用，
     *      防止第三方库（如 RMI、NIO 直接内存释放调用）滥用触发整堆停顿。
     *
     * 场景 2：老年代空间不足（Promotion Failed / 大对象直接晋升失败）
     *    - 细分原因A（大对象直接进入老年代）：超过 `-XX:PretenureSizeThreshold` 的大对象（如巨型byte数组）直接在老年代分配，
     *      若老年代连续可用空间不足，立即触发 Full GC。
     *    - 细分原因B（长期存活对象晋升）：Minor GC 时，存活对象年龄达到 `-XX:MaxTenuringThreshold`（默认15）时晋升老年代，
     *      若老年代剩余空间无法容纳，触发 Full GC。
     *    - 细分原因C（动态年龄判定）：Survivor 空间中相同年龄所有对象大小的总和大于 Survivor 空间的一半（TargetSurvivorRatio），
     *      大于或等于该年龄的对象直接提前进入老年代，导致老年代快速填满并触发 Full GC。
     *
     * 场景 3：空间分配担保失败（Handle Promotion Failure）
     *    - 执行逻辑：在每次执行 Minor GC 之前，JVM 会先检查老年代最大可用的连续空间：
     *      1. 如果老年代连续空间 > 新生代所有对象的总大小，则 Minor GC 确保安全。
     *      2. 如果小于，JVM 检查 `-XX:-HandlePromotionFailure` 设置以及老年代连续空间是否 > 历次晋升老年代对象的平均大小。
     *      3. 如果大于，尝试进行 Minor GC（有风险，如果晋升对象突增依然可能担保失败）；
     *      4. 如果小于或者担保失败（Promotion Failed），则必须直接触发一次 Full GC 进行空间腾挪。
     *
     * 场景 4：元空间 / 方法区空间不足（Metaspace OOM 预警）
     *    - 说明：JDK 8 之后永久代被元空间（Metaspace）取代，使用本地内存。但元空间仍有初始阈值 `-XX:MetaspaceSize`。
     *    - 过程：当系统动态生成大量类（如 CGLIB 代理、JSP 编译、Groovy 脚本、大量反射加载类）导致元空间达到高水位线时，
     *      JVM 为了卸载无用的类加载器和类元数据，会主动触发 Full GC 进行垃圾清理并调整元空间阈值。
     *
     * 场景 5：CMS 收集器中的 Concurrent Mode Failure 或 Promotion Failed
     *    - Concurrent Mode Failure（并发模式失败）：CMS 在并发标记/清除过程中，业务线程还在持续产生垃圾对象。
     *      如果老年代预留空间被填满（未能赶在老年代满之前完成并发回收），CMS 宣告失败，
     *      临时冻结所有业务线程，退化（Fallback）为单线程的 Serial Old 收集器来执行一次沉重的 Full GC。
     *    - Promotion Failed（晋升失败）：Minor GC 存活对象晋升老年代时，老年代虽然总空闲空间足够，
     *      但由于 CMS 标记-清除算法带来的内存碎片，没有足够大的“连续空间”容纳对象，退化为 Serial Old Full GC 并开启内存整理。
     *
     * 场景 6：G1 收集器的 Evacuation Failure 导致的退化 Full GC
     *    - 说明：G1 在 Mixed GC 或 Young GC 阶段，如果对象复制（Evacuation）时没有空闲的 Region 承接存活对象，
     *      或者 Humongous 巨型对象分配时找不到足够连续的 Region，G1 会被迫退化为单线程或多线程的 Full GC（Serial Full GC），
     *      整个堆被完全 STW。
     */

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("            JVM GC 分级（Minor/Major/Full GC）与 Full GC 触发深度剖析            ");
        System.out.println("================================================================================");

        printJvmGcCollectorsAndPools();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("1. 核心概念对比与解析");
        System.out.println("--------------------------------------------------------------------------------");
        explainGcLevels();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("2. 空间分配担保机制（Handle Promotion Failure）流程演示");
        System.out.println("--------------------------------------------------------------------------------");
        explainPromotionFailureMechanism();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("3. Full GC 触发场景全解析与模拟验证");
        System.out.println("--------------------------------------------------------------------------------");
        explainFullGcTriggerScenarios();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("4. 生产环境 Full GC 排查步骤与调优实践");
        System.out.println("--------------------------------------------------------------------------------");
        explainProductionTroubleshooting();

        System.out.println("\n================================================================================");
        System.out.println("            Minor GC / Major GC / Full GC 面试解析执行完毕                      ");
        System.out.println("================================================================================");
    }

    /**
     * 打印当前运行环境的垃圾回收器与内存池结构
     */
    private static void printJvmGcCollectorsAndPools() {
        System.out.println("[当前 JVM 垃圾回收器 MXBean 信息]:");
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.printf("  -> 收集器名称: %-25s | 发生GC次数: %-5d | 累计耗时: %d ms | 关联内存池: %s%n",
                    gcBean.getName(),
                    gcBean.getCollectionCount(),
                    gcBean.getCollectionTime(),
                    String.join(", ", gcBean.getMemoryPoolNames()));
        }

        System.out.println("\n[当前 JVM 内存池 (MemoryPoolMXBean) 分布]:");
        List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : memoryPoolBeans) {
            long used = pool.getUsage().getUsed() / (1024 * 1024);
            long max = pool.getUsage().getMax() / (1024 * 1024);
            String maxStr = max > 0 ? max + " MB" : "未指定上限/物理内存无硬限制";
            System.out.printf("  -> 内存池: %-30s | 类型: %-8s | 已用: %4d MB | 最大: %s%n",
                    pool.getName(),
                    pool.getType() == MemoryType.HEAP ? "堆内存" : "非堆内存",
                    used,
                    maxStr);
        }
    }

    /**
     * 解析 Minor GC, Major GC, Mixed GC 与 Full GC
     */
    private static void explainGcLevels() {
        System.out.println("[Minor GC (Young GC)]:");
        System.out.println("  1. 触发源头: Eden 空间耗尽。新对象分配在 Eden 区，Eden 无法再分配新对象时触发。");
        System.out.println("  2. 回收区域: 仅新生代 (Eden + From Survivor)。");
        System.out.println("  3. 算法: 标记-复制算法。存活对象拷贝至 To Survivor；达到阈值或 Survivor 溢出则晋升老年代。");
        System.out.println("  4. 性能表现: 频率高、STW 时间短（毫秒级），非常高效。");

        System.out.println("\n[Major GC (Old GC)]:");
        System.out.println("  1. 官方严谨定义: 仅指老年代的垃圾回收动作。目前主要特指 CMS 收集器的并发老年代回收。");
        System.out.println("  2. 口语/非严谨常态: 很多开发者将 Major GC 等同于 Full GC，但在底层源码中两者有严格区分。");
        System.out.println("  3. 性能表现: 速度通常比 Minor GC 慢 10 倍以上，停顿时间更长。");

        System.out.println("\n[Mixed GC (混合回收，G1 特有)]:");
        System.out.println("  1. 回收区域: 新生代所有 Region + 根据暂停时间目标挑选出的回收收益最高的部分老年代 Region。");
        System.out.println("  2. 触发阈值: IHOP (Initiating Heap Occupancy Percent) 默认整堆占用超过 45% 时启动并发标记。");

        System.out.println("\n[Full GC (整堆回收)]:");
        System.out.println("  1. 回收区域: 新生代 + 老年代 + 方法区/元空间 全局回收。");
        System.out.println("  2. 算法: 标记-清除 或 标记-整理，往往伴随类卸载和元空间清理。");
        System.out.println("  3. 性能表现: 全局 STW 停顿，耗时最长，若频繁发生会导致系统吞吐量断崖式下跌，严重时发生响应超时。");
    }

    /**
     * 深度解析空间分配担保机制（Handle Promotion Failure）
     */
    private static void explainPromotionFailureMechanism() {
        System.out.println("空间分配担保发生在 Minor GC 触发之前，JVM 评估老年代是否有足够能力容纳可能全部存活的新生代对象：");
        System.out.println("  步骤 1: 检查 [老年代最大可用连续空间] 是否大于 [新生代所有对象总大小]。");
        System.out.println("          -> 若大于: 保证安全，直接执行 Minor GC。");
        System.out.println("          -> 若小于: 说明可能发生担保失败，进入步骤 2。");
        System.out.println("  步骤 2: 查看 `-XX:-HandlePromotionFailure` 参数是否允许担保失败（JDK 7+ 默认允许）：");
        System.out.println("          -> 检查 [老年代最大可用连续空间] 是否大于 [历次晋升老年代对象的平均大小]。");
        System.out.println("          -> 若大于: 冒险尝试进行一次 Minor GC（如果本次晋升量突增，依然可能失败）。");
        System.out.println("          -> 若小于或不允许担保: 不做冒险，立即发起一次 Full GC 先清理老年代空间。");
        System.out.println("  步骤 3: 如果冒险执行 Minor GC 之后，存活对象体积远超预估，老年代依旧放不下：");
        System.out.println("          -> 发生 Promotion Failed，随即补救触发一次沉重的 Full GC。");
    }

    /**
     * 深度解析 Full GC 触发的六大场景并演示
     */
    private static void explainFullGcTriggerScenarios() {
        System.out.println("场景 1: System.gc() 或 Runtime.getRuntime().gc() 显式调用");
        System.out.println("  - 原理: 建议 JVM 执行 Full GC。生产环境应通过 -XX:+DisableExplicitGC 参数关闭。");

        System.out.println("\n场景 2: 老年代空间不足（Promotion Failed 或大对象直接晋升）");
        System.out.println("  - 原因 A: 大对象超过 PretenureSizeThreshold 直接在老年代分配，老年代无连续空间。");
        System.out.println("  - 原因 B: 对象年龄达到 MaxTenuringThreshold 晋升，老年代空间不足。");
        System.out.println("  - 原因 C: 动态年龄判定生效（Survivor区同龄对象总和 > 50%），中轻龄对象提前涌入老年代。");

        System.out.println("\n场景 3: 空间分配担保失败（Handle Promotion Failure）");
        System.out.println("  - 原理: 老年代剩余连续空间不足以支撑历次晋升平均值，直接触发 Full GC。");

        System.out.println("\n场景 4: 元空间 / 方法区空间不足（Metaspace High Water Mark）");
        System.out.println("  - 原理: 动态加载大量类（CGLIB/动态代理/JSP/Groovy）触碰 MetaspaceSize 阈值，触发 Full GC 尝试卸载类。");

        System.out.println("\n场景 5: CMS 收集器 Concurrent Mode Failure 与 Promotion Failed 退化");
        System.out.println("  - 原理: 并发标记清除过程中业务线程继续产生垃圾导致老年代撑满，退化为 Serial Old 串行 Full GC。");

        System.out.println("\n场景 6: G1 收集器 Evacuation Failure 或 Humongous 大对象分配失败退化");
        System.out.println("  - 原理: 存活对象拷贝找不到空闲 Region，或者超大对象分配无连续 Region，退化为 Full GC。");

        // 安全触发显式 GC 并观察前后的 GC 统计变化
        simulateExplicitGcObservation();
    }

    /**
     * 演示观察显式 GC 对 GC MXBean 的影响
     */
    private static void simulateExplicitGcObservation() {
        System.out.println("\n[代码演示: 观察显式 System.gc() 调用引发的 GC 统计增量]:");
        long beforeCount = 0;
        long beforeTime = 0;
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            beforeCount += gcBean.getCollectionCount();
            beforeTime += gcBean.getCollectionTime();
        }

        // 创建临时对象模拟工作负载
        List<byte[]> transientList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            transientList.add(new byte[64 * 1024]); // 64 KB 临时块
        }
        transientList.clear(); // 断开引用，使其成为可回收垃圾

        // 显式建议 GC
        System.gc();

        long afterCount = 0;
        long afterTime = 0;
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            afterCount += gcBean.getCollectionCount();
            afterTime += gcBean.getCollectionTime();
        }

        System.out.printf("  -> System.gc() 前 GC 总次数: %d, 总耗时: %d ms%n", beforeCount, beforeTime);
        System.out.printf("  -> System.gc() 后 GC 总次数: %d, 总耗时: %d ms%n", afterCount, afterTime);
        System.out.printf("  -> 本次 GC 增加次数: %d 次, 增加耗时: %d ms%n", (afterCount - beforeCount), (afterTime - beforeTime));
    }

    /**
     * 生产环境 Full GC 排查策略与调优总结
     */
    private static void explainProductionTroubleshooting() {
        System.out.println("[生产环境 Full GC 频繁的排查黄金步骤]:");
        System.out.println("  1. 开启 GC 日志参数: -Xlog:gc* (JDK 9+) 或 -XX:+PrintGCDetails -XX:+PrintGCDateStamps (JDK 8)");
        System.out.println("  2. 查看 GC 原因 (GC Cause): 日志中会明确输出 Full GC (System.gc())、Allocation Failure、Metadata GC Clear Soft 等");
        System.out.println("  3. 开启 OOM 自动 Dump 转储快照: -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/dump/");
        System.out.println("  4. 使用 JVM 监控分析工具: MAT (Memory Analyzer Tool)、JProfiler、VisualVM 分析堆内存泄露大对象");
        System.out.println("  5. 排查代码热点: 是否存在静态大集合无限积压、ThreadLocal 未 remove、大批量查询未分页、动态代理反复生成新类");
        System.out.println("  6. JVM 参数调优方向:");
        System.out.println("     - 合理调整新生代与老年代比例 (-Xmn 或 -XX:NewRatio=2)");
        System.out.println("     - 适当提高 Survivor 空间比例 (-XX:SurvivorRatio=8)");
        System.out.println("     - 调整老年代并发回收阈值（如 CMSInitiatingOccupancyFraction 或 G1 IHOP 阈值，避免并发失败）");
        System.out.println("     - 增大元空间初始阈值 (-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m)，避免元空间刚启动频繁扩容 Full GC");
    }
}
