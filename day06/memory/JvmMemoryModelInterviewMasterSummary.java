package memory;

/**
 * ==============================================================================================
 * 面试专题：JVM 运行时内存模型 17 问大厂通关速记与全景复盘 (JvmMemoryModelInterviewMasterSummary)
 * ==============================================================================================
 * 本类将本次 JVM 内存模型（Runtime Data Areas）专题中的全部 17 道高频面试题目进行结构化提炼，
 * 形成直击底层、大厂面试背诵金句，并作为整个 memory 文件夹的核心导航入口。
 *
 * ==============================================================================================
 * 17 大核心问题通关口诀与黄金回答速记
 * ==============================================================================================
 *
 * Q1: JVM的内存模型介绍一下
 * -> 答：区分 JMM（并发规范）与运行时数据区（内存划分）。五大区域：
 *        ① 线程共享：Java 堆（对象与数组、GC 主战场）、方法区（JDK 8+ 元空间 Metaspace，类元数据、常量池、JIT 代码缓存）；
 *        ② 线程私有：虚拟机栈（主管方法执行与栈帧）、本地方法栈（服务 JNI 本地方法）、程序计数器（字节码行号指示器，唯一无 OOM 区域）。
 *
 * Q2: JVM内存模型里的堆和栈有什么区别?
 * -> 答：六大核心维度：
 *        ① 职责：堆存对象实例与数组数据；栈负责方法调用与程序运算；
 *        ② 可见性：堆是全局线程共享；栈是线程独占私有；
 *        ③ 生命周期：堆与 JVM 进程同生共死；栈随线程创建而生、线程终止而灭；
 *        ④ 内存回收：堆依赖 GC，有 STW 停顿；栈自动压栈出栈，方法结束立即释放，零 GC；
 *        ⑤ 分配速度：栈移动栈顶指针极快（仅次于寄存器）；堆需寻址、CAS/加锁与碎片维护，相对较慢；
 *        ⑥ 异常表现：堆抛 OOM: Java heap space；栈抛 StackOverflowError 或 OOM: unable to create new native thread。
 *
 * Q3: 栈中存的到底是指针还是对象?
 * -> 答：只存指针（引用 Reference）！
 *        局部变量表中存基本数据类型的数值，引用类型则存储指向堆中对象内存首地址的直接指针或句柄。
 *        特例加分点：JIT 逃逸分析如果确定对象未逃逸出方法，会进行【标量替换】，把对象字段打散为基本类型标量在栈上分配，
 *        但此时栈上存储的依然是离散的标量，而非完整的 Java 对象本体。
 *
 * Q4: 堆分为哪几部分呢?
 * -> 答：传统物理分代：新生代（约 1/3，Eden:S0:S1 默认 8:1:1，复制算法）与老年代（约 2/3，存活长对象与大对象，标记-清除/整理）。
 *        永久代/元空间澄清：永久代在 JDK 8 彻底废除，元空间使用操作系统本地内存，不属于堆。
 *        现代低延迟架构（G1 / ZGC）：将堆切分为 2048 个大小相等的独立 Region，增加 Humongous 巨型对象区。
 *
 * Q5: 如果有个大对象一般是在哪个区域？
 * -> 答：直接在【老年代】分配（G1 中超过 Region 50% 分配在 Humongous Region，归老年代管理）。
 *        参数：-XX:PretenureSizeThreshold 设置大对象字节阈值。
 *        原因：避免大对象在新生代 Eden 与 Survivor 区之间来回深拷贝复制的高昂 CPU 开销；
 *              防止瞬间占满 Survivor 区触发动态年龄判定，迫使年轻代短命对象提前晋升老年代引发频繁 Full GC。
 *
 * Q6: 程序计数器的作用，为什么是私有的?
 * -> 答：作用：指示当前线程正在执行的字节码指令地址，分支、循环、跳转、异常处理、线程恢复的基础（Native 方法时未定义）。
 *        为什么私有：多线程依靠 CPU 时间片轮转并发调度。上下文切换后重新抢到 CPU 时，必须精准恢复到中断的字节码位置，故各线程独占私有。
 *        特点：JVM 规范中唯一没有规定任何 OutOfMemoryError 的区域。
 *
 * Q7: 方法区中的方法的执行过程？
 * -> 答：① 编译期生成字节码，封装在 Code 属性中；② 类加载进入方法区元空间，生成方法表；
 *        ③ 执行方法调用指令（invokevirtual/invokestatic 等），将符号引用解析为直接引用；
 *        ④ 线程虚拟机栈为该方法分配栈帧并压入栈顶（局部变量表、操作数栈、动态链接、方法返回地址）；
 *        ⑤ 执行引擎根据程序计数器在操作数栈与局部变量表间流转计算；⑥ 执行 return，栈帧弹出出栈，恢复调用方现场。
 *
 * Q8: 方法区中还有哪些东西?
 * -> 答：存储内容：类型元信息、运行时常量池、字段信息、方法信息、静态变量、JIT 代码缓存（Code Cache）。
 *        演进：JDK 7 永久代 PermGen（受限于 -XX:MaxPermSize 易 OOM）-> JDK 7 字符串常量池与静态变量移入堆 -> JDK 8+ 元空间 Metaspace（本地内存，无默认上限）。
 *
 * Q9: String保存在哪里呢?
 * -> 答：① new String 出来的实例对象本体：永远在【Java 堆】中；
 *        ② 字符串常量池（StringTable）：JDK 6 在永久代，JDK 7+ 迁移至【Java 堆】，享受到 Minor GC 及时回收；
 *        ③ 存储结构：JDK 9+ 由 char[] 优化为紧凑型 byte[] + coder，英文单字节编码内存直接减半。
 *
 * Q10: String s = new String (“abc”) 执行过程中分别对应哪些内存区域？
 * -> 答：① 栈：局部变量表分配引用变量 s，存放堆中对象的指针；
 *        ② 堆：执行 new 在堆中开辟空间，创建 String 实例对象；
 *        ③ 字符串常量池（堆中）：若常量池无 "abc"，在常量池中创建字面量对象；若已有则直接复用；
 *        创建对象数：常量池此前无则创建 2 个，常量池此前已有则创建 1 个。
 *
 * Q11: 引用类型有哪些？有什么区别?
 * -> 答：强引用（只要存在绝不回收，宁死不屈）、软引用（内存不足即将 OOM 前二次回收，用于敏感缓存）、
 *        弱引用（只要发现 GC 立即回收，用于 ThreadLocal/WeakHashMap）、虚引用（随时可回收，get() 为 null，配合 ReferenceQueue 跟踪堆外内存释放）。
 *
 * Q12: 弱引用了解吗?举例说明在哪里可以用?
 * -> 答：定义：只拥有弱引用的对象，在下一次 GC 时必然被回收。
 *        三大场景：① ThreadLocalMap.Entry 继承 WeakReference，Key 为弱引用，断开外部强引用后防止 Key 泄漏（注：Value 仍需在 finally 中调用 remove() 手动释放）；
 *        ② WeakHashMap：作为元数据弱键缓存，失去强引用的 Key 自动清理；
 *        ③ LeakCanary：监控 Activity 生命周期销毁，判断是否发生强引用未释放泄漏。
 *
 * Q13: 内存泄漏和内存溢出的理解?
 * -> 答：内存泄漏（Memory Leak）：无用对象因 GC Roots 强引用链未断开而无法被 GC 回收（暗中滴水）；
 *        内存溢出（Memory Overflow）：申请内存时超出 JVM 可分配上限抛出 OOM（水满瘫痪）；
 *        关系：泄漏持续积累必然导致溢出；但溢出不一定是泄漏导致（如突发大促瞬间并发超限、未分页导出 100 万大表）。
 *
 * Q14: jvm内存结构有哪几种内存溢出的情况?
 * -> 答：① 堆溢出：OOM: Java heap space、OOM: GC overhead limit exceeded；
 *        ② 栈溢出：StackOverflowError（调用深度超限）、OOM: unable to create new native thread（线程创建超限）；
 *        ③ 元空间溢出：OOM: Metaspace（动态生成类过多）；
 *        ④ 直接内存溢出：OOM: Direct buffer memory（堆外内存超限且未被 Cleaner 回收）。
 *
 * Q15: 遇到过堆溢出的情况吗？如何解决?
 * -> 答：生产排障 SOP 四步法：
 *        ① 事前防御：启动参数配置 -XX:+HeapDumpOnOutOfMemoryError 和 -XX:HeapDumpPath 保留现场；
 *        ② 工具分析：MAT / JProfiler / VisualVM 打开快照，看 Leak Suspects 与 Dominator Tree 支配树（按 Retained Heap 排查元凶）；
 *        ③ 追溯链路：Path to GC Roots 排除弱引用，找出是哪个单例/静态变量抓着不放；
 *        ④ 对症根治：修复泄漏代码（清理引用、分页查库、流式导出）或根据容量调大 -Xms 与 -Xmx。
 *
 * Q16: 栈溢出的情况呢?
 * -> 答：表现：java.lang.StackOverflowError。
 *        诱因：递归缺少终止条件、双向引用实体 JSON 序列化无限死循环、Spring 构造器循环依赖。
 *        解决：修正递归出口、深度树改用循环迭代算法、实体加 @JsonIgnore、适当调大 -Xss 参数。
 *
 * Q17: 有具体的内存泄漏和内存溢出的例子么请举例及解决方案?
 * -> 答：三大典型案例：
 *        ① 静态集合类不断 add 未清理：改用带 LRU 淘汰的 Caffeine/Guava 缓存，或业务结束调用 clear()；
 *        ② 线程池中 ThreadLocal 未 remove()：必须在 finally 块中显式调用 threadLocal.remove()；
 *        ③ IO/Socket/Connection 未关闭：使用 Java 7+ try-with-resources 自动关闭。
 */
public class JvmMemoryModelInterviewMasterSummary {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("        JVM 运行时内存模型 17 问全景面试速记与模块导引导航            ");
        System.out.println("======================================================================");

        System.out.println("【day06/memory 模块类结构索引】");
        System.out.println("1. JvmMemoryLayoutAndExecutionEngine.java");
        System.out.println("   -> 涵盖：JVM 运行时数据区全景、堆栈 6 大区别、栈中只存指针、PC 寄存器作用与私有原因、方法区执行流程与存储内容。");
        System.out.println("2. JvmHeapStructureAndObjectAllocation.java");
        System.out.println("   -> 涵盖：堆的新生代/老年代/Eden/Survivor 划分与比例、大对象直接进老年代机制、TLAB 与逃逸分析标量替换、完整对象分配流转链。");
        System.out.println("3. StringMemoryAllocationAndConstantPool.java");
        System.out.println("   -> 涵盖：String 存储位置历史演变（JDK 6/7/8/9）、String s = new String(\"abc\") 内存区域对应与对象创建数量、intern() 核心机制。");
        System.out.println("4. JavaReferenceTypesAndWeakReferenceScenarios.java");
        System.out.println("   -> 涵盖：强软弱虚四大引用对比、弱引用生命周期与 GC 现场联动、ThreadLocalMap.Entry 防泄漏机制、WeakHashMap 缓存。");
        System.out.println("5. MemoryLeakAndOverflowTroubleshooting.java");
        System.out.println("   -> 涵盖：内存泄漏 vs 内存溢出、JVM 各区域 OOM 类型、栈溢出现场实测与排查、典型泄漏案例代码整改、MAT 排障 SOP 全流程。");

        System.out.println("\n======================================================================");
        System.out.println("            全部 17 道面试题已全部归整在 day06/memory 文件夹中         ");
        System.out.println("======================================================================");
    }
}
