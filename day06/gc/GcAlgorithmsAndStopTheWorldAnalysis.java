package gc;

/**
 * 垃圾回收算法原理、标记清除致命缺陷与 Stop-The-World (STW) 触发阶段深度剖析
 *
 * 涵盖面试核心题目：
 * 1. 垃圾回收算法是什么，是为了解决什么问题?
 * 2. 垃圾回收算法有哪些?
 * 3. 标记清除算法的缺点是什么?
 * 4. 垃圾回收算法哪些阶段会stop the world?
 */
public class GcAlgorithmsAndStopTheWorldAnalysis {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          垃圾回收核心算法对比、碎片痛点与 STW 发生阶段全景实测       ");
        System.out.println("======================================================================");

        explainWhatAreGcAlgorithmsAndProblemsSolved();
        explainFourCoreGcAlgorithms();
        explainMarkSweepDisadvantages();
        explainWhichPhasesTriggerStopTheWorld();

        System.out.println("======================================================================");
        System.out.println("                 垃圾回收算法与 STW 机制解析完成                      ");
        System.out.println("======================================================================");
    }

    /**
     * 问题 1：垃圾回收算法是什么，是为了解决什么问题?
     */
    public static void explainWhatAreGcAlgorithmsAndProblemsSolved() {
        System.out.println("\n--- 1. 垃圾回收算法是什么？是为了解决什么问题？ ---");
        System.out.println("一、垃圾回收算法的定义：");
        System.out.println("   - 垃圾回收算法（Garbage Collection Algorithm）是 JVM 底层负责自动追踪内存对象引用关系、");
        System.out.println("     安全识别死亡对象并物理回收其所占内存、重新整理并规划可用堆空间的一套标准化【内存调度策略与数学模型】。");

        System.out.println("\n二、垃圾回收算法是为了解决什么核心痛点与问题？");
        System.out.println("   1. 解决手动内存管理的灾难性缺陷：");
        System.out.println("      - 彻底杜绝 C/C++ 中由于程序员疏漏引发的【内存泄漏（Memory Leak）】、");
        System.out.println("        以及因释放时机不当引发的【野指针/悬垂指针（Dangling Pointer）】和内存非法访问崩溃；");
        System.out.println("   2. 解决堆内存物理碎片化（Fragmentation）问题：");
        System.out.println("      - 频繁的对象创建与销毁会在堆中留下犬牙交错的内存空隙，算法必须解决【如何为新对象快速分配连续内存】；");
        System.out.println("   3. 解决多线程并发下的高效分配问题：");
        System.out.println("      - 保证在多线程超高并发申请内存时，无需在整堆全局加锁（通过指针碰撞与 TLAB 结合）；");
        System.out.println("   4. 在【吞吐量（Throughput）】与【低延迟（Latency / STW 停顿时间）】之间寻找最佳平衡：");
        System.out.println("      - 吞吐量优先：让 CPU 尽可能多地执行用户业务代码（如批处理、大数据计算）；");
        System.out.println("      - 延迟优先：让每一次垃圾回收暂停时间尽可能短，保障 Web 应用界面的毫秒级极速响应。");
    }

    /**
     * 问题 2：垃圾回收算法有哪些?
     *
     * 标记-清除（Mark-Sweep）、复制算法（Copying）、标记-整理（Mark-Compact）、分代收集理论（Generational Collection）。
     */
    public static void explainFourCoreGcAlgorithms() {
        System.out.println("\n--- 2. 四大经典垃圾回收核心算法对比 ---");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.println("算法名称           | 核心执行原理                                  | 核心优势               | 核心劣势");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.println("1. 标记-清除算法   | 遍历标记存活对象，统一回收未标记垃圾对象      | 原理最基础，无需移动对象| 内存碎片严重，分配变慢");
        System.out.println("   (Mark-Sweep)   |                                               | 实现简单               | 效率随垃圾量激增而波动");
        System.out.println("2. 复制算法        | 内存按比例划分，将存活对象连续复制到空闲区，  | 绝无碎片，指针碰撞极快 | 浪费可用内存空间（减半）");
        System.out.println("   (Copying)      | 一次性清理旧空间（Eden:S0:S1=8:1:1）          | 适合朝生夕灭的新生代   | 存活率高时复制成本高昂");
        System.out.println("3. 标记-整理算法   | 标记后让所有存活对象向内存一端挤压移动，      | 绝无碎片，100%空间利用 | 移动对象需修正所有指针，");
        System.out.println("   (Mark-Compact) | 直接清理掉边界以外的所有死内存空间            | 适合长命百岁的老年代   | STW 停顿时间显著延长");
        System.out.println("4. 分代收集理论    | 新生代对象死得多，采用【复制算法】；          | 因地制宜，综合性能最优 | 跨代引用需维护记忆集");
        System.out.println("   (Generational) | 老年代存活率极高，采用【标记-整理/清除算法】  | 主流商用 JVM 核心基石  | 复杂度较高");
        System.out.println("---------------------------------------------------------------------------------------------------------");
    }

    /**
     * 问题 3：标记清除算法的缺点是什么?
     *
     * 效率不稳定 + 严重内存空间碎片化 + 分配速度变慢（使用空闲列表无法使用指针碰撞）。
     */
    public static void explainMarkSweepDisadvantages() {
        System.out.println("\n--- 3. 标记-清除（Mark-Sweep）算法的三大致命缺点 ---");

        System.out.println("缺点一：内存空间碎片化严重（Memory Fragmentation - 最致命痛点）");
        System.out.println("   - 标记和清除之后会产生大量不连续的内存碎片（如同蜂窝煤洞）；");
        System.out.println("   - 恶果：虽然总空闲内存很多，但由于缺少足够大的【连续内存块】，当程序需要分配一个大对象（如超长 byte[] 数组、大集合）时，");
        System.out.println("     直接无法分配，不得不提前触发一次额外的垃圾回收（甚至直接恶化为耗时极长的 Full GC）！");

        System.out.println("\n缺点二：执行效率极不稳定，随堆内对象数量增加而急剧恶化");
        System.out.println("   - 如果 Java 堆中包含大量对象且其中大部分是需要被回收的；");
        System.out.println("   - 算法必须对整个堆执行深度遍历与扫描，标记和清除这两个过程的执行效率都随对象数量的增长而线性下降。");

        System.out.println("\n缺点三：对象内存分配速度变慢（无法使用高效的指针碰撞）");
        System.out.println("   - 在规整内存中分配对象只需挪动指针（指针碰撞 Bump the Pointer），仅需一条汇编指令，耗时极短；");
        System.out.println("   - 但在碎片化的内存中，JVM 必须维护一个复杂的【空闲列表（Free List）】，");
        System.out.println("     每次创建新对象时，都必须遍历空闲列表去挑选一个合适大小的内存块，大幅拉低了系统的对象吞吐量与性能！");
    }

    /**
     * 问题 4：垃圾回收算法哪些阶段会stop the world?
     *
     * 什么是 STW？为什么必须 STW？各个算法与收集器的 STW 阶段汇总。
     */
    public static void explainWhichPhasesTriggerStopTheWorld() {
        System.out.println("\n--- 4. 垃圾回收中哪些阶段会触发 Stop-The-World (STW)？ ---");

        System.out.println("一、什么是 Stop-The-World（STW）？");
        System.out.println("   - 在垃圾收集过程中的某些关键阶段，JVM 会下达全局指令，暂停所有正在执行的用户业务线程；");
        System.out.println("   - 在 STW 期间，整个应用程序就像被“时间定格”一样静止，除了 GC 收集器工作线程外，没有任何业务代码在运行。");

        System.out.println("\n二、为什么垃圾收集必须要有 STW？（不可避免的核心原因）");
        System.out.println("   1. 保证【可达性分析的一致性快照】：");
        System.out.println("      - 如果在扫描对象引用链时，用户线程还在并发创建新对象或修改引用指针，就如同“一边打扫房间，一边有人在房间随地乱扔垃圾”，");
        System.out.println("        甚至可能把原本存活的对象误判为垃圾而误杀（致命数据丢失！）；");
        System.out.println("   2. 保证【对象内存地址移动时指针更新的安全性】：");
        System.out.println("      - 当采用复制算法或标记-整理算法移动对象位置时，对象的内存首地址发生了改变；");
        System.out.println("      - 必须暂停所有访问该对象的线程，统一把全堆中所有指向该对象的引用指针全部修正为新地址后，才能唤醒线程继续执行，否则将发生非法内存越界访问！");

        System.out.println("\n三、主流垃圾收集器中具体触发 STW 的阶段全景盘点：");
        System.out.println("   1. 传统收集器（Serial / ParNew / Parallel Scavenge / Parallel Old）：");
        System.out.println("      - 【全程 STW！】从 GC 开始到 GC 结束，所有用户线程全程暂停，没有并发阶段。");
        System.out.println("   2. CMS 并发收集器（Concurrent Mark Sweep）：");
        System.out.println("      - 【初始标记（Initial Mark）- 触发 STW】：仅标记与 GC Roots 直接关联的第一层对象，耗时极短（几毫秒）；");
        System.out.println("      - 并发标记（Concurrent Mark）- 不触发 STW：与用户线程并发运行；");
        System.out.println("      - 【重新标记（Remark）- 触发 STW】：修正在并发标记期间因用户操作产生变动的对象，耗时较短；");
        System.out.println("      - 并发清除（Concurrent Sweep）- 不触发 STW：与用户线程并发执行。");
        System.out.println("   3. G1 收集器（Garbage-First）：");
        System.out.println("      - 【初始标记（Initial Mark）- 触发 STW】：借调 Minor GC 暂停时顺便完成；");
        System.out.println("      - 并发标记（Concurrent Mark）- 不触发 STW；");
        System.out.println("      - 【最终标记（Final Mark）- 触发 STW】：处理 SATB 原始快照遗留记录；");
        System.out.println("      - 【筛选回收（Live Data Counting and Evacuation）- 触发 STW】：把选定收益最高 Region 中的存活对象复制到空闲 Region 中，更新引用指针。");
        System.out.println("   4. 前沿超低延迟收集器（ZGC / Shenandoah）：");
        System.out.println("      - 引入了【染色指针（Colored Pointers）】与【读屏障（Read Barrier）】；");
        System.out.println("      - 实现了在用户线程继续运行的同时，并发移动对象并并发修正指针！");
        System.out.println("      - 仅在初始标记、最终标记保留微秒级的极短 STW，整体停顿时间控制在 1ms 以内！");
    }
}
