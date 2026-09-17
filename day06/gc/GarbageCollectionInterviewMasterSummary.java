package gc;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JVM 面试专题 - 垃圾回收（Garbage Collection）13问全景大纲与面试通关核心速记
 *
 * 本类整合并浓缩了用户提出的 13 个 JVM 垃圾回收核心问题，提供标准化面试回答模版与金句速记：
 *
 * 1. 什么是Java里的垃圾回收？如何触发垃圾回收?
 * 2. 判断垃圾的方法有哪些?
 * 3. 垃圾回收算法是什么，是为了解决什么问题?
 * 4. java的gc 机制你知道哪些?
 * 5. 垃圾回收算法有哪些?
 * 6. 垃圾回收器有哪些?
 * 7. 标记清除算法的缺点是什么?
 * 8. 垃圾回收算法哪些阶段会stop the world?
 * 9. minorGC、majorGC、fulIGC的区别，什么场景触发fullGC
 * 10. 垃圾回收器 CMS 和 G1的区别?
 * 11. 什么情况下使用CMS，什么情况使用G1?
 * 12. G1回收器的特色是什么?
 * 13. GC只会对堆进行GC吗?
 *
 * 源码分类索引导航：
 * - 基础原理与存活性判定: GcBasicsAndObjectLivenessJudgement.java
 * - 算法体系与 STW 深度剖析: GcAlgorithmsAndStopTheWorldAnalysis.java
 * - 分代收集机制与 Full GC 触发: MinorMajorFullGcDifferencesAndTriggers.java
 * - 收集器全景、CMS/G1对比与选型: GarbageCollectorsOverviewAndCmsG1Comparison.java
 */
public class GarbageCollectionInterviewMasterSummary {

    private static final Map<Integer, QuestionAnswerEntry> INTERVIEW_KB = new LinkedHashMap<>();

    static {
        INTERVIEW_KB.put(1, new QuestionAnswerEntry(
                "什么是Java里的垃圾回收？如何触发垃圾回收?",
                "GC是JVM后台自动管理内存并释放不再被任何强引用引用的对象内存机制，避免C/C++时代的手动free与内存泄漏风险。\n" +
                        "触发方式分为两类：\n" +
                        "1) 自动隐式触发：Eden区满触发Minor GC；老年代空间不足或元空间满触发Major/Full GC；\n" +
                        "2) 手动显式建议：调用System.gc()或Runtime.getRuntime().gc()，建议JVM发起Full GC（生产环境常用-XX:+DisableExplicitGC禁用）。",
                "GcBasicsAndObjectLivenessJudgement.java"
        ));

        INTERVIEW_KB.put(2, new QuestionAnswerEntry(
                "判断垃圾的方法有哪些?",
                "主流判断方法有两种：\n" +
                        "1) 引用计数法（Reference Counting）：给对象添加引用计数器，有引用+1，引用失效-1。缺点是无法解决循环引用，主流JVM均不采用。\n" +
                        "2) 可达性分析算法（Reachability Analysis）：从GC Roots根对象集合出发向下搜索引用链（Reference Chain），不可达的对象判定为垃圾。\n" +
                        "对象即使不可达，还会经历两次标记：若未执行且重写了finalize()方法，有一次在F-Queue中自我拯救的机会，逃逸失败才最终被回收。",
                "GcBasicsAndObjectLivenessJudgement.java"
        ));

        INTERVIEW_KB.put(3, new QuestionAnswerEntry(
                "垃圾回收算法是什么，是为了解决了什么问题?",
                "垃圾回收算法是JVM回收无用对象内存并重新分配利用的一套自动化数学与内存管理策略。\n" +
                        "解决三大核心问题：\n" +
                        "1) 内存泄漏与内存耗尽（自动化释放无用内存，避免手工释放遗漏）；\n" +
                        "2) 悬空指针与野指针（避免指针提前释放引发程序崩溃与安全漏洞）；\n" +
                        "3) 内存碎片化问题（规避碎片导致有可用总内存却无法分配连续大对象的窘境）。",
                "GcAlgorithmsAndStopTheWorldAnalysis.java"
        ));

        INTERVIEW_KB.put(4, new QuestionAnswerEntry(
                "java的gc 机制你知道哪些?",
                "Java GC 机制是一整套精密协作的体系：\n" +
                        "1) 分代收集理论：绝大多数对象朝生夕灭（弱分代假说），熬过多次GC的对象难消亡（强分代假说），跨代引用占极少数；\n" +
                        "2) 内存划分：新生代（Eden + 2个Survivor，默认8:1:1）与老年代（默认1:2），JDK 8后元空间代替永久代；\n" +
                        "3) 对象分配与晋升：TLAB快速分配 -> Eden分配 -> Minor GC存活进Survivor并累加年龄 -> 达到阈值（默认15）或动态年龄判定进入老年代 -> 大对象直接进老年代；\n" +
                        "4) 空间分配担保机制（Handle Promotion Failure）；\n" +
                        "5) 卡表（Card Table）与记忆集（RSet）记录跨代/跨区引用；\n" +
                        "6) 安全点（SafePoint）与安全区域（SafeRegion）协同STW。",
                "GcBasicsAndObjectLivenessJudgement.java"
        ));

        INTERVIEW_KB.put(5, new QuestionAnswerEntry(
                "垃圾回收算法有哪些?",
                "三大经典基础算法 + 两大综合工程思想：\n" +
                        "1) 标记-清除（Mark-Sweep）：直接标记并清除垃圾。缺点：效率不稳定、产生大量不连续内存碎片；\n" +
                        "2) 标记-复制（Mark-Copy）：半区复制或Appel式Eden+Survivor，无碎片、顺序分配，适用于存活率极低的新生代；\n" +
                        "3) 标记-整理（Mark-Compact）：标记后存活对象向一端滑动压缩并更新指针，无碎片但移动开销大，适用于老年代；\n" +
                        "4) 分代收集理论（Generational Collection）：新老年代按存活率特性分别选用复制或标记整理/清除；\n" +
                        "5) 分区算法（Region-based）：G1与ZGC将堆拆分为独立Region，按回收收益动态化整为零收集。",
                "GcAlgorithmsAndStopTheWorldAnalysis.java"
        ));

        INTERVIEW_KB.put(6, new QuestionAnswerEntry(
                "垃圾回收器有哪些?",
                "1) 经典新生代：Serial（单线程复制）、ParNew（多线程复制，配CMS）、Parallel Scavenge（高吞吐复制）；\n" +
                        "2) 经典老年代：Serial Old（单线程标记整理）、Parallel Old（多线程标记整理，JDK 8默认）、CMS（并发标记清除，低停顿）；\n" +
                        "3) 全堆综合：G1（Garbage-First，JDK 9+默认，Region化整为零、可预测停顿）；\n" +
                        "4) 现代超低延迟：ZGC（着色指针+读屏障，STW<1ms，JDK 21分代）、Shenandoah（并发疏散整理）、Epsilon（No-Op测试）。",
                "GarbageCollectorsOverviewAndCmsG1Comparison.java"
        ));

        INTERVIEW_KB.put(7, new QuestionAnswerEntry(
                "标记清除算法的缺点是什么?",
                "两大致命缺陷：\n" +
                        "1) 执行效率不稳定：标记和清除过程耗时与堆中对象数量成正比，对象越多效率越低；\n" +
                        "2) 产生大量不连续的内存碎片：清除后散落大量小空洞，虽然总空闲内存很大，但遇到较大连续对象（如大数组）分配时无法容纳，\n" +
                        "   被迫提前触发又一次垃圾收集甚至Full GC（如CMS发生Promotion Failed退化）。",
                "GcAlgorithmsAndStopTheWorldAnalysis.java"
        ));

        INTERVIEW_KB.put(8, new QuestionAnswerEntry(
                "垃圾回收算法哪些阶段会stop the world?",
                "STW 是为了在一致性快照下安全枚举根节点与更新引用。\n" +
                        "1) 必须全局 STW 的阶段：\n" +
                        "   - 根节点枚举（Root Enumeration）：所有收集器（包括CMS、G1、ZGC）在可达性分析起点枚举GC Roots都必须STW（依靠OopMap加速，耗时几毫秒）；\n" +
                        "   - 对象复制疏散与整理（Evacuation / Compacting）：Serial、Parallel、G1在移动存活对象并重定向指针时均会STW；\n" +
                        "2) 并发收集器（CMS / G1）的阶段划分：\n" +
                        "   - CMS: 初始标记（STW）-> 并发标记（不STW）-> 重新标记（STW，增量更新）-> 并发清除（不STW）；\n" +
                        "   - G1 : 初始标记（STW）-> 并发标记（不STW）-> 最终标记（STW，SATB）-> 筛选回收（STW，多线程复制存活对象）。",
                "GcAlgorithmsAndStopTheWorldAnalysis.java"
        ));

        INTERVIEW_KB.put(9, new QuestionAnswerEntry(
                "minorGC、majorGC、fulIGC的区别，什么场景触发fullGC",
                "区别：\n" +
                        "1) Minor GC（Young GC）：仅收集新生代（Eden+From），Eden满触发，复制算法，高频且STW极短（毫秒级）；\n" +
                        "2) Major GC（Old GC）：仅收集老年代，严格来说特指CMS收集器，比Minor GC慢10倍以上；\n" +
                        "3) Full GC：整堆收集（新生代 + 老年代 + 元空间/永久代），STW耗时最长，调优重点避免。\n" +
                        "触发 Full GC 的六大场景：\n" +
                        "1. 显式调用 System.gc()；\n" +
                        "2. 老年代空间不足（大对象分配或对象晋升失败 Promotion Failed）；\n" +
                        "3. 空间分配担保失败（Handle Promotion Failure）；\n" +
                        "4. 元空间不足（Metaspace 高水位线触发类卸载）；\n" +
                        "5. CMS 并发模式失败（Concurrent Mode Failure）或内存碎片引发晋升失败；\n" +
                        "6. G1 收集器 Evacuation 疏散失败或 Humongous 巨型大对象分配失败退化。",
                "MinorMajorFullGcDifferencesAndTriggers.java"
        ));

        INTERVIEW_KB.put(10, new QuestionAnswerEntry(
                "垃圾回收器 CMS 和 G1的区别?",
                "六大核心区别：\n" +
                        "1) 内存布局：CMS采用传统固定连续分代；G1化整为零划分约2048个动态Region（含Eden, Survivor, Old, Humongous）；\n" +
                        "2) 核心算法：CMS老年代为标记-清除（碎片多）；G1局部采用标记-复制（疏散整理，基本无碎片）；\n" +
                        "3) 回收范围：CMS仅回收老年代；G1是全堆收集器，支持Young GC与Mixed GC；\n" +
                        "4) 停顿可控性：CMS停顿不可预测；G1建立可预测停顿模型（-XX:MaxGCPauseMillis），按性价比优先回收（Garbage First）；\n" +
                        "5) 漏标处理：CMS采用增量更新（写屏障记新增引用）；G1采用原始快照SATB（写屏障记删除引用）；\n" +
                        "6) 内存开销：CMS简单卡表占堆1%~3%；G1独立RSet和复杂屏障占堆10%~20%。",
                "GarbageCollectorsOverviewAndCmsG1Comparison.java"
        ));

        INTERVIEW_KB.put(11, new QuestionAnswerEntry(
                "什么情况下使用CMS，什么情况使用G1?",
                "使用 CMS 场景：\n" +
                        "1) 历史系统停留在 JDK 8 且无升级计划；\n" +
                        "2) 堆内存较小（通常在 4GB~6GB 以内），RSet额外开销不划算；\n" +
                        "3) 对响应延迟敏感，且很少产生大对象导致碎片。\n" +
                        "（注意：CMS已在JDK 9废弃并在JDK 14正式删除，新系统严禁选型！）\n" +
                        "使用 G1 场景：\n" +
                        "1) 运行在现代 JDK（JDK 9、11、17、21默认推荐）；\n" +
                        "2) 堆内存较大（6GB~8GB 及以上，至几十GB）；\n" +
                        "3) 业务有严格的 P99 停顿时间 SLA 承诺（如限制在100~200ms内）；\n" +
                        "4) 对象分配速率高，频繁变动易产生碎片，需替换CMS解决Promotion Failed退化痛点。",
                "GarbageCollectorsOverviewAndCmsG1Comparison.java"
        ));

        INTERVIEW_KB.put(12, new QuestionAnswerEntry(
                "G1回收器的特色是什么?",
                "G1 的七大核心特色：\n" +
                        "1. Region 化整为零内存布局（打破物理连续分代，角色动态转换）；\n" +
                        "2. Garbage-First 垃圾优先机制（动态跟踪分区回收价值，优先回收垃圾比例最高的分区）；\n" +
                        "3. 可预测的停顿时间模型（-XX:MaxGCPauseMillis 软实时约束）；\n" +
                        "4. 空间整合无碎片（基于复制算法疏散对象，杜绝碎片累积）；\n" +
                        "5. Mixed GC 混合回收（一次GC同时回收年轻代与收益最高的老年代Region）；\n" +
                        "6. SATB 原始快照并发标记（结合写屏障高效处理并发漏标）；\n" +
                        "7. Humongous 巨型对象专门区域分配与快速回收。",
                "GarbageCollectorsOverviewAndCmsG1Comparison.java"
        ));

        INTERVIEW_KB.put(13, new QuestionAnswerEntry(
                "GC只会对堆进行GC吗?",
                "不会！GC 主要针对堆（新生代、老年代），但同时会对非堆区域（方法区 / 元空间）进行垃圾回收。\n" +
                        "元空间的回收主要包括两部分：\n" +
                        "1) 废弃常量的回收（常量池中不再有任何对象引用的字面量与符号引用）；\n" +
                        "2) 不再使用的类型的卸载（必须同时满足三个苛刻条件：该类所有实例已回收、加载该类的ClassLoader已回收、该类对应的java.lang.Class对象没有在任何地方被引用且无法反射调用）。",
                "GcBasicsAndObjectLivenessJudgement.java"
        ));
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("        JVM 垃圾回收（GC）面试 13 问完整清单与高频考点大纲                      ");
        System.out.println("================================================================================");

        for (Map.Entry<Integer, QuestionAnswerEntry> entry : INTERVIEW_KB.entrySet()) {
            System.out.printf("\n[问题 %02d]: %s%n", entry.getKey(), entry.getValue().question);
            System.out.printf("  -> 关联代码文件: %s%n", entry.getValue().sourceFile);
            System.out.println("  -> 核心速记解答:");
            String[] lines = entry.getValue().answer.split("\n");
            for (String line : lines) {
                System.out.println("     " + line);
            }
        }

        System.out.println("\n================================================================================");
        System.out.println("        13 问大纲索引打印完毕，所有关联代码均已按题分类存放于 day06/gc/ 目录    ");
        System.out.println("================================================================================");
    }

    private static class QuestionAnswerEntry {
        final String question;
        final String answer;
        final String sourceFile;

        QuestionAnswerEntry(String question, String answer, String sourceFile) {
            this.question = question;
            this.answer = answer;
            this.sourceFile = sourceFile;
        }
    }
}
