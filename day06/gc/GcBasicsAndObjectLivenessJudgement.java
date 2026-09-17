package gc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * 垃圾回收核心概念、GC 触发方式、对象存活判定算法与回收作用域深度解析
 *
 * 涵盖面试核心题目：
 * 1. 什么是Java里的垃圾回收？如何触发垃圾回收?
 * 2. 判断垃圾的方法有哪些?
 * 3. GC只会对堆进行GC吗?
 * 4. java的gc 机制你知道哪些?
 */
public class GcBasicsAndObjectLivenessJudgement {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          Java 垃圾回收本质、触发条件、存活判定与回收范围实测          ");
        System.out.println("======================================================================");

        explainWhatIsGarbageCollection();
        explainHowGcIsTriggered();
        explainObjectLivenessAlgorithms();
        explainGcScopeDoesItOnlyCollectHeap();
        inspectRuntimeGarbageCollectors();

        System.out.println("======================================================================");
        System.out.println("                 垃圾回收基础与存活判定解析完成                      ");
        System.out.println("======================================================================");
    }

    /**
     * 问题 1 第一问 & 问题 4：什么是Java里的垃圾回收？java的gc 机制你知道哪些?
     */
    public static void explainWhatIsGarbageCollection() {
        System.out.println("\n--- 1. 什么是 Java 里的垃圾回收（Garbage Collection）？ ---");
        System.out.println("一、垃圾回收的本质：");
        System.out.println("   - 在 C/C++ 语言中，内存管理由程序员手动负责（malloc/free, new/delete），极易因遗漏产生内存泄漏或悬垂指针；");
        System.out.println("   - Java 引入了【自动内存管理（Automatic Memory Management）】机制，");
        System.out.println("     由运行在后台的垃圾收集器（Garbage Collector）负责动态跟踪、识别并回收那些【不再被任何活动对象所引用的内存空间】；");
        System.out.println("   - 所谓“垃圾（Garbage）”，是指在内存中分配的、后续程序逻辑再也无法访问和使用的对象实例。");

        System.out.println("\n二、Java 的 GC 机制核心体系（宏观认知）：");
        System.out.println("   1. 分代收集理论：依据对象存活周期的不同，将内存划分为年轻代（朝生夕灭）与老年代（长命百岁），采取针对性收集算法；");
        System.out.println("   2. 自动化存活判定：基于【可达性分析算法（GC Roots）】遍历引用链，辅以四种引用类型（强/软/弱/虚）弹性控制；");
        System.out.println("   3. 内存分配与回收闭环：通过 TLAB 无锁分配、Eden 区指针碰撞、Survivor 区复制晋升、老年代整理清除实现内存闭环；");
        System.out.println("   4. 低延迟演进：从早期单线程 Serial，到多线程并行 Parallel，到并发标记清除 CMS，再到面向全堆 Region 的 G1 与纳秒级停顿的 ZGC。");
    }

    /**
     * 问题 1 第二问：如何触发垃圾回收?
     *
     * 自动触发场景 vs 手动触发机制。
     */
    public static void explainHowGcIsTriggered() {
        System.out.println("\n--- 2. 如何触发垃圾回收（触发条件与主动/被动机制）？ ---");
        System.out.println("一、被动自动触发机制（JVM 内部运行时监控触发 - 生产常态）：");
        System.out.println("   1. Young GC / Minor GC 触发条件：");
        System.out.println("      - 当年轻代中的【Eden 区空间耗尽】时，JVM 无法为新对象（或新申请的 TLAB）分配内存，自动触发 Minor GC。");
        System.out.println("   2. Old GC / Major GC / Mixed GC 触发条件：");
        System.out.println("      - 老年代可用空间低于阈值（如 CMS 默认的 -XX:CMSInitiatingOccupancyFraction）；");
        System.out.println("      - G1 收集器中整个堆占用率达到阈值（-XX:InitiatingHeapOccupancyPercent=45%）时触发并发标记并开启 Mixed GC。");
        System.out.println("   3. Full GC 触发条件：");
        System.out.println("      - 老年代空间不足以容纳晋升的对象；");
        System.out.println("      - 空间分配担保失败（Minor GC 前检查老年代连续空间不足以支撑历次晋升平均大小）；");
        System.out.println("      - 方法区/元空间（Metaspace）空间不足，扩展达到最大阈值；");
        System.out.println("      - CMS 发生并发模式失败（Concurrent Mode Failure）或晋升失败（Promotion Failed）。");

        System.out.println("\n二、主动手动触发机制（程序代码建议触发）：");
        System.out.println("   - 代码方式：调用 System.gc() 或 Runtime.getRuntime().gc()；");
        System.out.println("   - 核心认知：System.gc() 仅仅是【向虚拟机发出一次建议性的垃圾回收通知】，JVM 并不保证立即执行，也无法控制回收力度！");
        System.out.println("   - 生产环境禁忌：生产环境严禁在业务代码中显式调用 System.gc()（频繁触发 Full GC 会引起长时间 STW 停顿，甚至引发 RPC 超时）；");
        System.out.println("   - JVM 防御参数：生产启动参数建议配置【-XX:+DisableExplicitGC】，将代码中的 System.gc() 调用静默忽略屏蔽！");
    }

    /**
     * 问题 2：判断垃圾的方法有哪些?
     *
     * 引用计数算法（Reference Counting） vs 可达性分析算法（Reachability Analysis）。
     */
    public static void explainObjectLivenessAlgorithms() {
        System.out.println("\n--- 3. 判断对象是否为垃圾的核心算法（判定方法对比） ---");
        System.out.println("判定对象是否存活主要有两种经典算法：");

        System.out.println("方法一：引用计数法（Reference Counting Collector）");
        System.out.println("   - 算法原理：每个对象持有一个引用计数器。每当有一个地方引用它时，计数器加 1；当引用失效时，计数器减 1。任何时刻计数器为 0 的对象即被判定为垃圾。");
        System.out.println("   - 优势：实现极其简单，判定效率高，几乎不需要等待全堆遍历即可实时回收。");
        System.out.println("   - 致命缺陷：无法解决【对象间相互循环引用（Circular References）】问题！");
        System.out.println("     * 例如：ObjectA.instance = ObjectB; ObjectB.instance = ObjectA;");
        System.out.println("     * 当外部再无任何引用指向 A 和 B 时，由于两者互相引用，它们的计数器永远为 1，导致对象永远无法被回收，发生严重内存泄漏！");
        System.out.println("   - 结论：主流商用 Java 虚拟机（HotSpot、OpenJ9 等）**均不采用**引用计数法！");

        System.out.println("\n方法二：可达性分析算法（Reachability Analysis - 主流 JVM 标配）");
        System.out.println("   - 算法原理：通过一系列称为【GC Roots】的根对象作为起始节点集，从这些节点根据引用关系向下搜索，搜索走过的路径称为【引用链（Reference Chain）】；");
        System.out.println("   - 判定准则：如果从 GC Roots 到某个对象没有任何引用链相连（从图论角度即从 GC Roots 到该对象不可达），则证明此对象不可能再被使用，判定为可回收垃圾！");
        System.out.println("   - 优势：彻底解决了循环引用问题。哪怕 A 和 B 互相环形引用，只要它们整体与 GC Roots 断开，均会被判定为垃圾并安全回收！");

        System.out.println("\n[面试重点] 究竟哪些对象可以作为 GC Roots？（大厂必考背诵点）");
        System.out.println("   1. 虚拟机栈（栈帧局部变量表）引用的对象（如方法中正在使用的局部变量、入参对象）；");
        System.out.println("   2. 方法区中类静态属性引用的对象（如类的 static 变量持有的对象）；");
        System.out.println("   3. 方法区中常量引用的对象（如字符串常量池 StringTable 里的引用、final 常量）；");
        System.out.println("   4. 本地方法栈中 JNI（即 Native 方法）引用的对象；");
        System.out.println("   5. Java 虚拟机内部的引用（如系统类加载器、基本数据类型对应的 Class 对象、常驻异常对象如 NullPointerException）；");
        System.out.println("   6. 所有被同步锁（synchronized 关键字）持有的对象；");
        System.out.println("   7. 反映 JVM 内部情况的 JMXBean、JVMTI 中注册的回调等。");
    }

    /**
     * 问题 13：GC只会对堆进行GC吗?
     *
     * 权威定论：不仅对堆进行回收，方法区（元空间）也会被回收！
     */
    public static void explainGcScopeDoesItOnlyCollectHeap() {
        System.out.println("\n--- 4. GC 只会对堆进行垃圾回收吗？（深度辨析） ---");
        System.out.println("权威回答：【绝对不是！GC 不仅回收堆内存，方法区（元空间）同样存在垃圾回收机制！】");

        System.out.println("一、方法区（元空间 Metaspace / 永久代 PermGen）的垃圾回收：");
        System.out.println("   - 《Java 虚拟机规范》明确指出：方法区可以不实现垃圾回收，且方法区回收的“性价比”通常较低（堆回收率 70%~90%，元空间常常不到 10%）；");
        System.out.println("   - 但主流商用 JVM（如 HotSpot）对方法区均实现了完整的垃圾回收机制。方法区垃圾回收主要涵盖两大目标：");
        System.out.println("     1. 废弃的常量（如字符串常量、字面量）：");
        System.out.println("        - 只要当前系统中没有任何一个对象引用常量池中的该字符串，且无任何地方引用此常量符号，就会被垃圾回收器清理出常量池。");
        System.out.println("     2. 不再使用的类（类卸载 / Class Unloading）：");
        System.out.println("        - 必须【同时严格满足以下三大苛刻条件】，类才会被 JVM 允许卸载：");
        System.out.println("          ① 该类的所有实例都已经被垃圾收集器回收（堆中不存在该类及其任何派生子类的实例）；");
        System.out.println("          ② 加载该类的 ClassLoader 类加载器已经被回收（通常只有自定义加载器如 OSGi、JSP、动态代理加载器才能被卸载，系统 AppClassLoader 永不卸载）；");
        System.out.println("          ③ 该类对应的 java.lang.Class 对象没有任何地方被引用，无法在任何地方通过反射访问该类的方法或字段。");

        System.out.println("\n二、堆外内存/直接内存（Direct Memory）的回收联动：");
        System.out.println("   - NIO 的 DirectByteBuffer 在操作系统物理内存中分配堆外内存；");
        System.out.println("   - 其内部通过【虚引用（PhantomReference）】关联一个 Cleaner 对象；");
        System.out.println("   - 当堆内的 DirectByteBuffer 对象被 GC 回收时，关联的虚引用进入 ReferenceQueue，由后台守护线程触发 unsafe.freeMemory() 释放堆外物理内存！");
    }

    /**
     * 探查当前运行期 JVM 所生效的垃圾收集器名称
     */
    public static void inspectRuntimeGarbageCollectors() {
        System.out.println("\n--- 5. [当前运行期实测] 探查 JVM 生效的垃圾收集器 ---");
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.println(String.format("   [收集器名称: %-25s] 累计发生 GC 次数: %3d 次 | 累计 STW 停顿时间: %4d ms | 负责内存池: %s",
                    gcBean.getName(),
                    gcBean.getCollectionCount(),
                    gcBean.getCollectionTime(),
                    java.util.Arrays.toString(gcBean.getMemoryPoolNames())));
        }
    }
}
