package gc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JVM 面试专题 - 经典与现代垃圾回收器全景、CMS 与 G1 深度对比及选型指南
 *
 * 本类针对四大经典高频面试题进行深度解析与实战演示：
 * 1. 垃圾回收器有哪些?
 * 2. 垃圾回收器 CMS 和 G1的区别?
 * 3. 什么情况下使用CMS，什么情况使用G1?
 * 4. G1回收器的特色是什么?
 *
 * 核心考点：
 * - 经典七大分代垃圾回收器组合与低延迟新一代回收器（ZGC、Shenandoah）
 * - CMS 收集器架构（并发标记、增量更新、碎片痛点、退化 Serial Old）
 * - G1 收集器核心架构（Region 化整为零、RSet/卡表、SATB、Mixed GC、停顿预测模型）
 * - 选型方法论：根据堆内存规模、延迟容忍度与 JDK 版本科学决策
 */
public class GarbageCollectorsOverviewAndCmsG1Comparison {

    /*
     * =========================================================================
     * 面试核心问答整理
     * =========================================================================
     *
     * 问题一：垃圾回收器有哪些？
     * -------------------------------------------------------------------------
     * 按照 JVM 分代与架构演进，主要垃圾收集器划分为三大阵营：
     *
     * 1. 传统分代垃圾收集器（新生代 / 老年代搭配使用）：
     *    【新生代收集器】（均基于标记-复制算法）：
     *      - Serial: 单线程新生代收集器，简单高效，STW 较长，适用于单核或客户端环境。
     *      - ParNew: Serial 收集器的多线程并发版本，早期专门用于与 CMS 搭配工作。
     *      - Parallel Scavenge: 多线程新生代收集器，以“高吞吐量”（Throughput）为目标，自适应调节策略。
     *
     *    【老年代收集器】：
     *      - Serial Old: 单线程老年代收集器，基于标记-整理算法。作为 CMS 发生并发失败时的后备保底方案。
     *      - Parallel Old: Parallel Scavenge 的老年代版本，基于多线程标记-整理，JDK 8 默认组合（Parallel Scavenge + Parallel Old）。
     *      - CMS (Concurrent Mark Sweep): 业界首款以“获取最短回收停顿时间”为目标的老年代收集器，多线程并发标记-清除。
     *
     * 2. 全堆综合垃圾收集器：
     *    - G1 (Garbage-First): 面向服务端应用的划时代全堆收集器（JDK 9+ 默认），打破物理连续分代，
     *      将堆划分为大量 Region，实现软实时、可预测的暂停时间模型（Predictable Pause-Time Model）。
     *
     * 3. 现代超低延迟垃圾收集器（停顿时间进入毫秒/亚毫秒级）：
     *    - ZGC (Z Garbage Collector): JDK 11 引入、JDK 15 生产可用、JDK 21 引入分代 ZGC。
     *      采用“着色指针（Colored Pointers）”与“读屏障（Load Barrier）”，在 TB 级大堆下将 STW 严格控制在 1ms 以内。
     *    - Shenandoah GC: Red Hat 开源并贡献给 OpenJDK，使用读写屏障与 Brooks Pointers 实现并发疏散整理。
     *    - Epsilon GC: No-Op（无操作）收集器，不执行任何实际垃圾清理，用于极短生命周期任务、性能基准测试与微服务容器。
     *
     * -------------------------------------------------------------------------
     * 经典搭配组合表：
     * -------------------------------------------------------------------------
     * | 新生代收集器    | 对应老年代收集器 | 算法组合             | 适用场景与状态                         |
     * |----------------|------------------|----------------------|----------------------------------------|
     * | Serial         | Serial Old       | 复制 + 整理          | 单核客户端应用、极小内存环境           |
     * | ParNew         | CMS              | 复制 + 并发清除      | 响应优先的 Web 系统（JDK 9 废弃，JDK 14 移除） |
     * | Parallel Scavenge| Parallel Old   | 复制 + 多线程整理    | 算力/批处理密集型、高吞吐量优先（JDK 8 默认） |
     * | G1 (整堆)      | G1 (整堆)        | Region复制/局部整理  | 兼顾低延迟与高吞吐、大堆场景（JDK 9+ 默认）    |
     *
     * =========================================================================
     * 问题二：垃圾回收器 CMS 和 G1 的区别？
     * -------------------------------------------------------------------------
     *
     * 维度 1：堆内存空间布局（核心差异）
     *   - CMS: 严格遵守传统的“物理连续分代”，年轻代（Eden+From+To）与老年代各自占据固定且连续的物理内存空间。
     *   - G1: 打破物理连续分代，将整堆划分为 2048 个左右大小相等（1MB~32MB，必须为2的幂）的独立独立区块（Region）。
     *     每个 Region 在逻辑上可以动态扮演 Eden、Survivor、Old，或者专门存放超大对象的 Humongous 区。
     *
     * 维度 2：核心垃圾回收算法与内存碎片
     *   - CMS: 老年代基于【标记-清除（Mark-Sweep）】算法。
     *     * 致命缺点: 不会进行内存压缩整理，长期运行必然产生大量内存碎片。
     *     * 导致恶果: 当需要分配大对象却找不到连续空间时，发生 Promotion Failed，被迫退化为 Serial Old 进行漫长的单线程 STW 整理。
     *   - G1: 从整体上看基于“标记-整理”，从两个 Region 局部之间看基于【标记-复制（Mark-Copy）】算法。
     *     * 核心优势: 对象从一个 Region 疏散（Evacuate）拷贝并紧凑压入另一个空闲 Region，全程几乎不产生内存碎片。
     *
     * 维度 3：收集范围与运作模式
     *   - CMS: 仅能收集老年代（Major GC），新生代必须协同 ParNew 收集器；无法对全堆进行宏观动态资源倾斜。
     *   - G1: 属于全堆统一收集器，支持 Young GC（收集全部年轻代 Region）和 Mixed GC（收集全部年轻代 Region + 挑选收益最高的部分老年代 Region）。
     *
     * 维度 4：STW 停顿时间的可控性
     *   - CMS: 停顿不可控。虽然初始标记和重新标记 STW 很短，但无法设定停顿上限，碎片严重时退化导致停顿长达数秒。
     *   - G1: 引入【可预测的停顿时间模型（Predictable Pause-Time Model）】。
     *     允许用户通过 `-XX:MaxGCPauseMillis=200` 设置期望最大停顿目标（默认 200ms），G1 根据历史衰减均值智能挑选回收性价比最高的分区。
     *
     * 维度 5：并发标记漏标问题的解决方案
     *   - CMS: 采用【增量更新（Incremental Update）】。当黑色对象新增指向白色对象的引用时，写屏障记录新引用，重新标记时重新扫描黑色对象。
     *   - G1: 采用【原始快照（SATB, Snapshot At The Beginning）】。当灰色对象删除指向白色对象的引用时，写屏障记录被删除的引用，
     *     保证在并发标记开始时的对象图快照中白色对象仍然被标记，避免漏标。
     *
     * 维度 6：内存与运行额外开销（RSet 与卡表）
     *   - CMS: 仅需维护一个较简单的老年代到新生代的卡表（Card Table），额外内存开销仅占堆的 1%~3%。
     *   - G1: 每个 Region 都需要维护一个复杂的记忆集（Remembered Set, RSet）记录谁引用了我，
     *     外加复杂的写屏障与并发日志队列，额外内存消耗通常占整堆的 10%~20%，对硬件内存要求更高。
     *
     * =========================================================================
     * 问题三：什么情况下使用 CMS，什么情况使用 G1？
     * -------------------------------------------------------------------------
     *
     * 【使用 CMS 的场景】：
     * 1. 历史系统版本限制：仍运行在 JDK 8 及更低版本，且短期内无升级规划。
     * 2. 堆内存偏小：物理堆内存一般在 4GB~6GB 以下（4GB 以下 CMS 的卡表开销极小，轻量快速）。
     * 3. 延迟敏感型业务：对响应延迟敏感（如前端 Web 接口、电商网关），且很少生成超大对象，堆内存碎片积累慢。
     * 4. CPU 核心资源充裕：CMS 的并发标记与并发清除需要多线程介入，CPU 算力充沛时对吞吐量影响可控。
     * 5. 官方状态警示：CMS 在 JDK 9 废弃，JDK 14 已被彻底移除。全新技术栈与系统坚决不应再采用 CMS。
     *
     * 【使用 G1 的场景】：
     * 1. 现代化 JDK 架构：运行在 JDK 9、JDK 11、JDK 17、JDK 21 等现代 LTS 版本上（G1 是默认且高度优化的收集器）。
     * 2. 堆内存适中偏大：堆内存通常在 6GB~8GB 及以上（几十GB），大内存下传统收集器 STW 过于可怕，G1 能将 STW 约束在极小范围。
     * 3. 严格的延迟 SLA 指标：对服务最大停顿时间有确定要求（如要求 P99 停顿时间不超过 100ms~200ms）。
     * 4. 易产生内存碎片或分配大量巨型对象：系统高频创建大字符串、大集合，CMS 容易频繁 Promotion Failed 的场景。
     * 5. 替换老旧的 CMS 或 Parallel 组合：解决 CMS 停顿不可控与碎片问题，或者解决 Parallel 收集器整堆回收停顿过长的痛点。
     *
     * =========================================================================
     * 问题四：G1 回收器的特色是什么？
     * -------------------------------------------------------------------------
     *
     * 1. 化整为零的 Region 内存化划分：
     *    打破连续分代，以 1MB~32MB 的独立 Region 划分堆，动态流转角色（Eden, Survivor, Old, Humongous），
     *    极大提高了内存分配与清理的灵活性。
     *
     * 2. Garbage-First（垃圾优先，收益最大化原则）：
     *    后台跟踪每个 Region 里的回收价值（回收可腾出空间大小与所需耗时的经验比），维护优先列表。
     *    每次 GC 在有限的停顿时间内，优先回收垃圾最多、性价比最高的分区。
     *
     * 3. 软实时、可预测的停顿时间模型（Predictable Pause-Time Model）：
     *    支持用户指定目标停顿时间（-XX:MaxGCPauseMillis），G1 通过历史衰减标准差模型计算出当前时间内能够回收多少 Region，
     *    保证系统响应时间平稳可控。
     *
     * 4. 局部复制算法实现空间紧凑无碎片：
     *    Region 之间存活对象基于复制整理算法（Evacuation），回收完成后旧 Region 成为完全连续干净的空闲空间，
     *    杜绝了 CMS 标记-清除产生的碎片问题。
     *
     * 5. Mixed GC（混合收集）打破单代隔离：
     *    不仅能做 Young GC，还能在 Mixed GC 阶段同时回收新生代和一部分价值最高的老年代 Region，
     *    使得老年代回收不再像 Full GC 那样沉重。
     *
     * 6. Humongous 巨型大对象专门机制：
     *    凡是体积大于 Region 一半（> 0.5 * RegionSize）的对象定义为巨型对象，存放在一组连续的 Humongous Region 中。
     *    巨型对象可以直接在并发标记完成或 Young GC 阶段被提早回收，大幅避免进入老年代带来膨胀。
     *
     * 7. 记忆集（RSet）高效处理跨 Region 引用：
     *    通过 Points-into（谁引用了我）结构的 RSet，GC 时扫描某个 Region 无需全堆遍历，大幅缩短标记扫描时间。
     */

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("        JVM 垃圾回收器全景图谱、CMS 与 G1 深度对比及选型指南                    ");
        System.out.println("================================================================================");

        printJvmRuntimeCollectorInfo();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("1. 经典与现代垃圾回收器体系结构全览");
        System.out.println("--------------------------------------------------------------------------------");
        explainAllGarbageCollectors();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("2. CMS 收集器与 G1 收集器的全方位对比（六大核心维度）");
        System.out.println("--------------------------------------------------------------------------------");
        compareCmsAndG1InDetail();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("3. CMS 与 G1 的应用场景决策树");
        System.out.println("--------------------------------------------------------------------------------");
        explainCollectorSelectionGuidelines();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("4. G1 收集器的七大核心特色与底层机制剖析");
        System.out.println("--------------------------------------------------------------------------------");
        explainG1FeaturesAndArchitecture();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("5. G1 堆 Region 划分与 Humongous 对象分配逻辑计算模拟");
        System.out.println("--------------------------------------------------------------------------------");
        simulateG1RegionAndHumongousCalculations();

        System.out.println("\n================================================================================");
        System.out.println("            垃圾回收器全览、CMS 与 G1 对比解析执行完毕                          ");
        System.out.println("================================================================================");
    }

    /**
     * 获取当前 JVM 实际生效的收集器信息
     */
    private static void printJvmRuntimeCollectorInfo() {
        System.out.println("[当前环境运行的 JVM 垃圾回收器]:");
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean bean : beans) {
            System.out.printf("  -> 收集器: %-25s | 内存池: %s%n",
                    bean.getName(),
                    Arrays.toString(bean.getMemoryPoolNames()));
        }
    }

    /**
     * 梳理所有主流垃圾回收器
     */
    private static void explainAllGarbageCollectors() {
        System.out.println("1. 新生代收集器（Mark-Copy 算法）:");
        System.out.println("   - Serial: 单线程，STW 停顿长，资源占用极小，适合小内存、客户端模式。");
        System.out.println("   - ParNew: Serial 的多线程版本，用于与 CMS 配合（JDK 9 废弃，JDK 14 移除）。");
        System.out.println("   - Parallel Scavenge: 多线程高吞吐优先，具备自适应调节策略（UseAdaptiveSizePolicy）。");

        System.out.println("\n2. 老年代收集器:");
        System.out.println("   - Serial Old: 单线程标记-整理，作为 CMS 并发失败或担保失败时的退化兜底机制。");
        System.out.println("   - Parallel Old: 多线程标记-整理，与 Parallel Scavenge 构成 JDK 8 经典默认吞吐组合。");
        System.out.println("   - CMS: 首款并发老年代收集器，基于标记-清除，目标最短停顿，存在碎片与退化痛点。");

        System.out.println("\n3. 全堆与现代低延迟收集器:");
        System.out.println("   - G1 (Garbage-First): JDK 9+ 默认。Region 划分、复制整理无碎片、软实时可预测停顿。");
        System.out.println("   - ZGC: 基于着色指针与读屏障，并发标记与并发移动，TB 级堆 STW < 1ms。JDK 21 引入分代。");
        System.out.println("   - Shenandoah: 基于 Brooks 转发指针与读写屏障，追求低延迟并发疏散整理。");
        System.out.println("   - Epsilon: No-Op 空回收器，不回收内存，用于测试及瞬时微服务容器。");
    }

    /**
     * 六大维度对比 CMS 和 G1
     */
    private static void compareCmsAndG1InDetail() {
        System.out.println("【维度 1: 内存布局】");
        System.out.println("  - CMS: 传统连续物理分代，年轻代与老年代边界固定死板。");
        System.out.println("  - G1 : 化整为零，将堆划分为约 2048 个独立的 Region，逻辑角色动态分配。");

        System.out.println("\n【维度 2: 回收算法与内存碎片】");
        System.out.println("  - CMS: 标记-清除（Mark-Sweep）。不整理内存，必然产生碎片；严重时退化为 Serial Old Full GC。");
        System.out.println("  - G1 : 局部基于标记-复制（Mark-Copy）。存活对象疏散压缩到空闲 Region，几乎不产生内存碎片。");

        System.out.println("\n【维度 3: 收集范围】");
        System.out.println("  - CMS: 只管老年代，必须依赖 ParNew 收集新生代，无法统筹调度全堆。");
        System.out.println("  - G1 : 全堆收集器，支持独立的 Young GC，也支持混合回收的 Mixed GC。");

        System.out.println("\n【维度 4: 停顿时间可控性】");
        System.out.println("  - CMS: 尽力缩短停顿，但停顿时间完全不可预期。");
        System.out.println("  - G1 : 建立了【可预测的停顿模型】，支持 -XX:MaxGCPauseMillis 设定目标停顿时间。");

        System.out.println("\n【维度 5: 并发标记漏标解决算法】");
        System.out.println("  - CMS: 增量更新（Incremental Update），写屏障记录新增加的引用关系。");
        System.out.println("  - G1 : 原始快照（SATB），写屏障记录删除的旧引用关系，配合 RSet 保障快照完整性。");

        System.out.println("\n【维度 6: 内存额外开销】");
        System.out.println("  - CMS: 简单卡表，仅占堆内存约 1%~3%。");
        System.out.println("  - G1 : 每个 Region 独立 RSet 与复杂写屏障，额外消耗堆内存约 10%~20%。");
    }

    /**
     * 选型指南
     */
    private static void explainCollectorSelectionGuidelines() {
        System.out.println("[什么情况使用 CMS?]");
        System.out.println("  1. 系统运行在 JDK 8 且没有升级计划；");
        System.out.println("  2. 堆内存规模较小（通常在 4GB~6GB 以内），RSet 内存开销过重不划算时；");
        System.out.println("  3. 吞吐量和响应时间中更偏向低延迟，且无大量大对象产生导致碎片；");
        System.out.println("  注意: CMS 已在 JDK 9 弃用并在 JDK 14 正式删除，新系统严禁选型 CMS！");

        System.out.println("\n[什么情况使用 G1?]");
        System.out.println("  1. 系统运行在现代 JDK（JDK 9+，特别是 JDK 11 / 17 / 21 LTS 版本）；");
        System.out.println("  2. 堆内存较大（6GB~8GB 及以上，至几十GB甚至上百GB）；");
        System.out.println("  3. 对服务的响应时间有明确 SLA（例如要求 P99 停顿时间稳定在 200ms 内）；");
        System.out.println("  4. 内存对象分配速率高，或者存活对象突变频繁，容易产生内存碎片；");
        System.out.println("  5. 希望摆脱 CMS 频繁出现的 Promotion Failed 和长时间退化停顿。");
    }

    /**
     * 剖析 G1 的七大核心特色
     */
    private static void explainG1FeaturesAndArchitecture() {
        System.out.println("1. Region 化整为零布局: 堆被等分为 Eden、Survivor、Old、Humongous 四种角色。");
        System.out.println("2. Garbage-First 垃圾优先: 优先回收价值最大的 Region，追求最高的收集性价比。");
        System.out.println("3. 可预测的停顿时间模型: 用户期望时间驱动收集行为，避免长时间无限制 STW。");
        System.out.println("4. 复制整理无碎片: 疏散复制机制杜绝内存碎片积累，提升空间利用率。");
        System.out.println("5. Mixed GC 混合收集: 在一次 GC 中同时回收年轻代与部分老年代，平衡吞吐与延迟。");
        System.out.println("6. SATB 原始快照: 保证并发标记过程中的对象存活性判定准确高效。");
        System.out.println("7. 巨型对象 (Humongous) 专门优化: 避免超大对象频繁复制晋升带来的高昂开销。");
    }

    /**
     * 模拟计算 G1 的 Region 大小与 Humongous 对象判定规则
     */
    private static void simulateG1RegionAndHumongousCalculations() {
        System.out.println("[G1 Region 规格计算模拟 (堆总容量 -> Region 大小 -> Humongous 判定门槛)]:");

        long[] sampleHeapSizesMB = {4096, 8192, 16384, 32768}; // 4G, 8G, 16G, 32G
        for (long heapMB : sampleHeapSizesMB) {
            // G1 算法目标是将堆划分为大约 2048 个 Region
            // Region 大小必须为 2 的幂，范围在 1MB 到 32MB 之间
            long targetRegionMB = heapMB / 2048;
            long calculatedRegionMB = 1;
            while (calculatedRegionMB < targetRegionMB && calculatedRegionMB < 32) {
                calculatedRegionMB <<= 1;
            }
            if (calculatedRegionMB < 1) calculatedRegionMB = 1;
            if (calculatedRegionMB > 32) calculatedRegionMB = 32;

            long humongousThresholdKB = (calculatedRegionMB * 1024) / 2; // > 0.5 * RegionSize 为巨型对象

            System.out.printf("  -> 堆大小: %5d MB | 计算得 Region 大小: %2d MB | 巨型对象判定门槛: > %5d KB%n",
                    heapMB, calculatedRegionMB, humongousThresholdKB);
        }
    }
}
