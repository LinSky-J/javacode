package memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

/**
 * JVM 内存模型、运行时数据区划分、栈帧结构与方法执行全景深度解析
 *
 * 涵盖面试核心题目：
 * 1. JVM的内存模型介绍一下
 * 2. JVM内存模型里的堆和栈有什么区别?
 * 3. 栈中存的到底是指针还是对象?
 * 4. 程序计数器的作用，为什么是私有的?
 * 5. 方法区中的方法的执行过程？
 * 6. 方法区中还有哪些东西?
 */
public class JvmMemoryLayoutAndExecutionEngine {

    // 静态变量属于类的元信息关联数据（JDK 8+ 静态对象引用随 Class 对象存放于堆中）
    private static final String APP_NAME = "JVM-Memory-Inspector";
    private static int executionCounter = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          JVM 运行时数据区结构、堆栈差异与方法执行流程深度实测          ");
        System.out.println("======================================================================");

        explainJvmMemoryLayout();
        explainHeapVsStackDifferences();
        explainStackPointerOrObject();
        explainProgramCounterRegister();
        explainMethodExecutionProcess();
        explainMethodAreaContents();

        System.out.println("======================================================================");
        System.out.println("                 JVM 运行时数据区与方法执行解析完成                  ");
        System.out.println("======================================================================");
    }

    /**
     * 问题 1：JVM的内存模型介绍一下
     *
     * 面试核心注意点：
     * 1. 概念厘清：Java 内存模型（JMM，Java Memory Model）与 JVM 运行时数据区（Runtime Data Areas）的区别。
     *    - JMM 是多线程并发规范，定义共享内存中变量访问规则与可见性（volatile, synchronized, happens-before）。
     *    - 运行时数据区是 JVM 虚拟机规范所定义的运行时内存物理与逻辑划分。
     * 2. 运行时数据区五大核心组成：
     *    - 线程共享：堆（Heap）、方法区（Method Area / Metaspace）
     *    - 线程私有：程序计数器（Program Counter Register）、虚拟机栈（JVM Stack）、本地方法栈（Native Method Stack）
     */
    public static void explainJvmMemoryLayout() {
        System.out.println("\n--- 1. JVM 运行时数据区全景架构解析 ---");
        System.out.println("JVM 运行时数据区按照线程可见性分为两大阵营：");
        System.out.println("一、线程共享区域（生命周期伴随 JVM 进程，多线程共享，存在线程安全问题）：");
        System.out.println("   1. Java 堆（Heap）：存储几乎所有的对象实例与数组，GC 垃圾回收的主战场。");
        System.out.println("   2. 方法区（Method Area）：存储已被虚拟机加载的类信息、字段、方法元数据、运行时常量池、JIT 代码缓存等。");
        System.out.println("      - JDK 7 之前由永久代（PermGen）实现，使用 JVM 堆内存。");
        System.out.println("      - JDK 8+ 彻底废除永久代，改为元空间（Metaspace），使用操作系统本地直接内存（Native Memory）。");
        System.out.println("二、线程私有区域（生命周期伴随线程创建而生、线程终止而灭，不存在线程安全竞争）：");
        System.out.println("   1. 程序计数器（PC Register）：记录当前线程正在执行的字节码指令地址，唯一没有规定 OOM 的区域。");
        System.out.println("   2. Java 虚拟机栈（JVM Stack）：主管方法调用与执行，每个方法调用产生一个栈帧（Stack Frame）。");
        System.out.println("   3. 本地方法栈（Native Method Stack）：为 JVM 调用底层 C/C++ Native 方法（JNI）提供栈服务。");

        // 运行时内存探查
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

        System.out.println(String.format("   [当前 JVM 内存实测] 堆初始: %d MB, 堆已用: %d MB, 堆最大: %d MB",
                heapUsage.getInit() / 1024 / 1024,
                heapUsage.getUsed() / 1024 / 1024,
                heapUsage.getMax() / 1024 / 1024));
        System.out.println(String.format("   [非堆/元空间实测] 初始: %d MB, 已用: %d MB, 最大: %s",
                nonHeapUsage.getInit() / 1024 / 1024,
                nonHeapUsage.getUsed() / 1024 / 1024,
                nonHeapUsage.getMax() < 0 ? "受限于物理机内存" : (nonHeapUsage.getMax() / 1024 / 1024 + " MB")));
    }

    /**
     * 问题 2：JVM内存模型里的堆和栈有什么区别?
     *
     * 对比维度：存储内容、共享方式、生命周期、空间连续性、垃圾回收、异常形态、分配速度。
     */
    public static void explainHeapVsStackDifferences() {
        System.out.println("\n--- 2. JVM 堆（Heap）与 栈（Stack）核心区别对比 ---");
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println("对比维度          | Java 堆（Heap）                     | Java 虚拟机栈（Stack）");
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println("管理与职责        | 存储数据：对象实例、数组、实例变量  | 负责逻辑执行：方法调用、运算、程序流转");
        System.out.println("线程可见性        | 线程全局共享                        | 线程独占私有，各线程栈完全隔离");
        System.out.println("生命周期          | 与 JVM 进程同生共死                 | 与线程生命周期同步，随线程创建与销毁");
        System.out.println("内存回收方式      | 依赖垃圾收集器（GC），不可控回收    | 依靠栈帧自动压栈出栈，方法结束立即释放，无 GC");
        System.out.println("内存空间物理形态  | 逻辑上连续，物理内存可离散          | 连续内存空间，结构简单快速");
        System.out.println("分配与访问性能    | 相对较慢，需要寻址、GC 维护空闲列表 | 极快，直接移动栈顶指针（仅次于寄存器）");
        System.out.println("可能的异常类型    | OutOfMemoryError: Java heap space   | StackOverflowError（深度溢出）");
        System.out.println("                  |                                     | OutOfMemoryError: unable to create new native thread");
        System.out.println("---------------------------------------------------------------------------------------------");
    }

    /**
     * 问题 3：栈中存的到底是指针还是对象?
     *
     * 核心结论：
     * 栈中存储的永远是指针（引用 Reference），对象本身永远在堆中（除非逃逸分析标量替换优化，此时拆为标量而非对象）。
     */
    public static void explainStackPointerOrObject() {
        System.out.println("\n--- 3. 栈中存的到底是指针还是对象？ ---");
        System.out.println("权威定论：Java 栈中只存储对象引用（指针 / Reference），真正的对象本体永远分配在堆（Heap）中！");
        System.out.println("1. 栈帧（Stack Frame）内部的局部变量表（Local Variable Table）包含：");
        System.out.println("   - 基本数据类型（boolean, byte, char, short, int, float, long, double）：直接存储具体数值。");
        System.out.println("   - 引用类型（Reference）：存储指向堆中对象首地址的引用指针（Direct Pointer）或句柄地址（Handle）。");
        System.out.println("   - 返回地址（ReturnAddress）：指向字节码指令的地址。");
        System.out.println("2. 为什么对象不能直接放在栈上？");
        System.out.println("   - 对象的生命周期可能逃逸出当前方法（如 return 返回或赋值给全局变量），栈帧退出即销毁，若放栈上会导致悬垂引用。");
        System.out.println("   - 对象大小在编译期无法完全预知，而栈帧大小在类加载编译成字节码时就必须确定（Code 属性的 max_locals 与 max_stack）。");
        System.out.println("3. 高级特例（面试加分项）：逃逸分析与标量替换（Escape Analysis & Scalar Replacement）");
        System.out.println("   - 在 JIT 即时编译器优化中，如果经逃逸分析确认一个对象【绝不逃逸出当前方法】，JIT 会进行【标量替换】。");
        System.out.println("   - 标量替换将对象的成员字段打散成普通基本类型变量，直接在栈帧局部变量表上分配，避免在堆中分配对象和触发 GC。");
        System.out.println("   - 注意：此时在栈上分配的是离散的【标量（基本类型变量）】，依然不是完整的 Java 对象实例！");
    }

    /**
     * 问题 4：程序计数器的作用，为什么是私有的?
     *
     * 作用：记录当前线程正在执行的字节码指令地址；分支、跳转、循环、异常处理、线程恢复的基础。
     * 为什么私有：多线程时间片轮转上下文切换，切回时能恢复到正确的指令执行位置。
     */
    public static void explainProgramCounterRegister() {
        System.out.println("\n--- 4. 程序计数器（PC Register）的作用与线程私有原因 ---");
        System.out.println("1. 程序计数器的核心作用：");
        System.out.println("   - 记录当前线程正在执行的 JVM 字节码指令地址（行号指示器）。");
        System.out.println("   - 字节码解释器通过改变程序计数器的值，来选取下一条需要执行的字节码指令。");
        System.out.println("   - 分支跳转（if/else）、循环控制（for/while）、异常抛出与捕获（try/catch）、线程恢复等功能都依赖它。");
        System.out.println("   - 注意特例：如果当前线程正在执行的是 Native（本地）方法，程序计数器的值为空（Undefined），因为 Native 属于 C 代码，不在字节码指令控制范围。");
        System.out.println("2. 为什么程序计数器必须是线程私有的？");
        System.out.println("   - Java 虚拟机支持多线程并发执行，多线程是通过 CPU 时间片轮转调度、抢占式切换来实现的。");
        System.out.println("   - 任何一个确定的时刻，一个 CPU 核心只能执行某一条线程的指令。");
        System.out.println("   - 当线程 CPU 时间片耗尽或发生上下文切换时，必须记录下当前线程执行到的具体字节码位置；");
        System.out.println("   - 当线程重新争抢到 CPU 资源恢复执行时，才能从准确中断的指令位置继续执行，因此每个线程必须拥有独立私有的程序计数器！");
        System.out.println("3. 关键考点：");
        System.out.println("   - 程序计数器是 Java 虚拟机规范中【唯一没有规定任何 OutOfMemoryError】的内存区域，且所占内存极小。");
    }

    /**
     * 问题 5：方法区中的方法的执行过程？
     *
     * 流程：类加载与解析 -> 符号引用转直接引用 -> 栈帧创建并压栈 -> 字节码解释或 JIT 执行 -> 操作数栈与局部变量表流转 -> 结果返回与出栈恢复现场。
     */
    public static void explainMethodExecutionProcess() {
        System.out.println("\n--- 5. 方法区中方法的完整生命周期与执行过程 ---");
        System.out.println("从源码编写到方法被 JVM 调用的完整流转过程：");
        System.out.println("步骤 1：编译期生成字节码");
        System.out.println("   - javac 编译 Java 源码生成 .class 文件，方法代码被编译为一连串字节码指令，连同异常表存储在 Code 属性中。");
        System.out.println("步骤 2：类加载进入方法区");
        System.out.println("   - ClassLoader 类加载器加载类，将类结构元数据、常量池、方法表（Method Table）存放于方法区（元空间）。");
        System.out.println("步骤 3：符号引用解析与动态绑定");
        System.out.println("   - 遇到方法调用指令（如 invokevirtual, invokespecial, invokestatic, invokeinterface, invokedynamic）；");
        System.out.println("   - 将常量池中的符号引用解析为实际内存入口地址（直接引用 / vtable 虚方法表偏移量）。");
        System.out.println("步骤 4：栈帧创建与压入虚拟机栈顶（Push Frame）");
        System.out.println("   - 线程为该方法分配栈帧，栈帧包含四大部分：");
        System.out.println("     1) 局部变量表（Local Variable Table）：入参 this、实参及方法内局部变量；");
        System.out.println("     2) 操作数栈（Operand Stack）：用于存放计算过程中的中间变量与运算结果；");
        System.out.println("     3) 动态链接（Dynamic Linking）：指向方法区运行时常量池中该方法的引用；");
        System.out.println("     4) 方法返回地址（Return Address）：方法正常退出或异常退出的恢复现场地址。");
        System.out.println("步骤 5：字节码执行引擎流转");
        System.out.println("   - 程序计数器指向首条指令，执行引擎从操作数栈、局部变量表取数进行计算（或由 JIT 编译为本地机器指令直接由 CPU 执行）。");
        System.out.println("步骤 6：方法返回与栈帧弹出（Pop Frame）");
        System.out.println("   - 执行 return 或异常未捕获，将返回值推入调用方操作数栈，当前栈帧销毁并出栈，恢复调用方方法的执行上下文。");

        // 模拟一个简单计算方法的调用，直观展示调用栈轨迹
        int calculatedValue = sampleCalculateMethod(10, 20);
        System.out.println("   [实测方法调用结果] 10 + 20 = " + calculatedValue);
    }

    private static int sampleCalculateMethod(int a, int b) {
        // 打印当前调用栈帧信息
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        System.out.println("   [当前执行中线程栈帧深度: " + stackTrace.length + "]");
        if (stackTrace.length > 2) {
            System.out.println("      当前执行方法: " + stackTrace[1].getMethodName());
            System.out.println("      调用方方法:   " + stackTrace[2].getMethodName());
        }
        return a + b;
    }

    /**
     * 问题 6：方法区中还有哪些东西?
     *
     * 包含内容：类型信息、运行时常量池、字段信息、方法信息、静态变量（引用关系）、JIT 编译缓存。
     * 演进：PermGen (JDK 7) -> Metaspace (JDK 8+)。
     */
    public static void explainMethodAreaContents() {
        System.out.println("\n--- 6. 方法区中存储的核心元素与历史演进 ---");
        System.out.println("一、方法区存储的核心元数据内容：");
        System.out.println("   1. 类型信息（Type Information）：全限定类名、访问修饰符、父类全名、实现的接口列表、是否为枚举/注解/接口。");
        System.out.println("   2. 运行时常量池（Runtime Constant Pool）：");
        System.out.println("      - 编译期产生的字面量（Literal，如整数、浮点数、常量值）和符号引用（Symbolic References，类/方法/字段符号）。");
        System.out.println("      - 具备动态性，运行时可以通过 String.intern() 向常量池动态添加。");
        System.out.println("   3. 字段信息（Field Information）：字段名称、字段类型、修饰符（public, private, volatile, static 等）。");
        System.out.println("   4. 方法信息（Method Information）：方法名、返回值类型、参数类型及顺序、方法修饰符、字节码指令、操作数栈与局部变量表大小、异常表。");
        System.out.println("   5. 静态变量（Class Variables / static）：随类加载初始化的静态数据。");
        System.out.println("   6. JIT 代码缓存（Code Cache）：即时编译器将高频热点字节码编译成的本地机器指令（Native Code）。");

        System.out.println("二、方法区的重大历史演进（永久代 vs 元空间）：");
        System.out.println("   1. JDK 7 及以前（永久代 PermGen）：");
        System.out.println("      - 位于 JVM 虚拟机连续堆内存中，参数为 -XX:PermSize 和 -XX:MaxPermSize。");
        System.out.println("      - 缺点：受制于固定大小上限，当大量动态加载类（如 Spring CGLIB、JSP 频繁加载）时极易抛出 java.lang.OutOfMemoryError: PermGen space。");
        System.out.println("   2. JDK 7 过渡期：");
        System.out.println("      - 字符串常量池（StringTable）与静态变量（static 变量）移出永久代，转移到 Java 堆（Heap）中！");
        System.out.println("   3. JDK 8 及以后（元空间 Metaspace）：");
        System.out.println("      - 彻底移除永久代，改用操作系统的本地内存（Native Memory）来实现元空间。");
        System.out.println("      - 默认不设置上限（仅受宿主机可用物理内存限制），参数改为 -XX:MetaspaceSize 与 -XX:MaxMetaspaceSize。");
        System.out.println("      - 优势：极大降低了元数据溢出风险，类加载垃圾回收效率显著提高。");
    }
}
