package jvm;

/**
 * 面试专题：JVM 本质、JDK/JRE/JVM 包含关系 与 JVM 和 Java 的解耦区别深度解析。
 *
 * 本类对应面试核心题目：
 * 1. jvm是什么
 * 2. JVM、JDK、JRE三者关系?
 * 3. JVM 和 Java 有啥区别?
 *
 * 设计目标：
 * 建立清晰的 Java 生态体系架构视图，搞懂虚拟机的运行时职责、开发与运行环境的分界，
 * 以及 JVM 作为一个通用多语言生态平台的广阔内涵。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JvmConceptAndJdkJreRelationship {

    public static void main(String[] args) {
        initConsoleEncoding();
        System.out.println("==================================================");
        System.out.println("     JVM 本质定义、三者层级关系与生态解耦深度解析      ");
        System.out.println("==================================================");

        explainWhatIsJvm();
        explainJdkJreJvmRelationship();
        explainDifferenceBetweenJvmAndJava();

        System.out.println("==================================================");
        System.out.println("             架构梳理完毕，请参考类中注释深入消化     ");
        System.out.println("==================================================");
    }

    /**
     * 第一部分：深入讲透 JVM 究竟是什么？
     *
     * 面试核心考点：
     * JVM 不仅是一个软件进程，更是具备完整计算机体系结构的虚拟计算机与标准规范。
     */
    public static void explainWhatIsJvm() {
        /*
         * 1. 核心定义：
         *    - JVM（Java Virtual Machine，Java 虚拟机）是一台逻辑上的【虚拟计算机】。
         *    - 它有自己完善的硬件抽象架构：包括一套虚拟指令集（字节码指令）、程序计数器、
         *      操作数栈、局部变量表以及运行时内存区域分配与管理模型。
         *
         * 2. JVM 的核心核心四大子系统：
         *    - 【类加载子系统（Class Loader Subsystem）】：负责从文件系统或网络中定位并加载 .class 字节码文件，
         *      并进行严格的连接（验证、准备、解析）与初始化。
         *    - 【运行时数据区（Runtime Data Area，即 JVM 内存）】：
         *      * 线程共享：堆（Heap，存放对象实例）、方法区/元空间（Metaspace，存放类型信息、常量池）。
         *      * 线程私有：程序计数器（PC Register）、Java 虚拟机栈（JVM Stack）、本地方法栈（Native Method Stack）。
         *    - 【执行引擎（Execution Engine）】：
         *      * 解释器（Interpreter）：读取并逐行执行字节码。
         *      * JIT 编译器（Just-In-Time Compiler）：将热点代码编译成本地机器码。
         *      * 垃圾收集器（Garbage Collector，GC）：全自动追踪无用对象并释放堆内存。
         *    - 【本地方法接口（JNI - Java Native Interface）与本地库】：与底层 C/C++ 动态链接库进行交互。
         *
         * 3. JVM 是一套【开放标准规范】：
         *    - Oracle 官方发布的《Java 虚拟机规范》定义了二进制 class 文件的结构与指令集行为。
         *    - 任何公司都可以根据规范编写符合标准的虚拟机实现，著名实现包括：
         *      * HotSpot：Oracle JDK 和 OpenJDK 默认的旗舰虚拟机。
         *      * OpenJ9：IBM 开发的低内存占用、高启动性能虚拟机（现归 Eclipse 基金会）。
         *      * Zing：Azul 公司开发的企业级无停顿（C4 垃圾回收器）高性能虚拟机。
         *      * GraalVM：支持全语言互操作与原生提前编译（AOT）的新一代高性能虚拟机。
         *      * Dragonwell（龙井）：阿里巴巴基于 OpenJDK 定制优化的高并发虚拟机。
         */
        System.out.println("1. JVM 本质：具备类加载、内存管理、执行引擎与垃圾回收的虚拟计算机，也是一套开放的指令集标准规范。");
    }

    /**
     * 第二部分：JVM、JDK、JRE 三者的层次包含关系
     *
     * 面试核心考点：层级嵌套关系与面向的使用人群划分。
     */
    public static void explainJdkJreJvmRelationship() {
        /*
         * 1. 三者包含关系公式：
         *    【JDK】 包含 【JRE】 包含 【JVM】
         *    用集合概念表示：JDK ⊃ JRE ⊃ JVM
         *
         * 2. JVM（Java Virtual Machine）：
         *    - 作用：处于最底层，提供字节码运行平台、内存分配与自动垃圾回收。
         *    - 限制：仅有 JVM 是无法运行任何业务程序的，因为业务程序调用 System.out.println 或 List 时，
         *      必须依赖基础类库。
         *
         * 3. JRE（Java Runtime Environment，Java 运行时环境）：
         *    - 构成：JVM + Java 核心基础类库（如 java.base, java.sql 等标准 API 的运行版代码）。
         *    - 定位：面向【最终用户与生产运行环境】。如果只需要在服务器上运行别人已经编译好的 .jar 程序，
         *      在传统模式下只需要安装 JRE 即可，不需要安装庞大的开发工具。
         *    - 现代演进（重要考点）：从 Java 9 模块化（Project Jigsaw）开始，Oracle 已经取消了单独的 JRE 安装包，
         *      官方倡导使用 jlink 工具根据应用实际依赖的模块，定制裁剪出最小化的专属运行时环境。
         *
         * 4. JDK（Java Development Kit，Java 开发者工具包）：
         *    - 构成：JRE + 完整的开发、调试、监控与诊断工具链。
         *    - 核心开发工具：
         *      * javac：Java 源码编译器，将 .java 编译为 .class。
         *      * jar：打包归档工具。
         *      * javadoc：自动根据规范注释生成 API HTML 文档的工具。
         *      * javap：反编译分析字节码的工具（深入排查底层机制利器）。
         *      * 生产排障与性能监控工具：jps、jstat、jmap、jstack、jcmd、jconsole、VisualVM、JFR 等。
         *    - 定位：面向【软件开发人员】。只要需要编写代码并进行编译调试，就必须安装 JDK。
         */
        System.out.println("2. 三者关系：JDK 包含 JRE 包含 JVM。JDK 面向开发者（含编译器与诊断工具），JRE 面向生产运行，JVM 为执行核心。");
    }

    /**
     * 第三部分：JVM 和 Java 有什么区别？
     *
     * 面试拔高亮点：
     * 打破“JVM 等同于 Java”的误区，指出语言与运行平台的彻底解耦。
     */
    public static void explainDifferenceBetweenJvmAndJava() {
        /*
         * 1. 概念层面的根本解耦：
         *    - Java 是一门【高级面向对象编程语言】，规定了语法、关键词、访问控制、异常规则和面向对象语法糖。
         *    - JVM 是一个【通用的中间字节码执行平台与运行时操作系统】。
         *
         * 2. JVM 从不认识 Java 源码：
         *    - JVM 的眼里没有任何 .java 源代码，也不在乎代码是用什么语言写的。
         *    - JVM 只认且严格校验符合规范的 .class 字节码二进制流（前四个字节为经典的魔数 0xCAFEBABE）。
         *
         * 3. JVM 是跨语言的生态载体：
         *    - 任何一种编程语言，只要其专属编译器能够将该语言的源码编译成符合 JVM 规范的 .class 字节码，
         *      就能直接在 JVM 上流畅运行，并与现有的上百万 Java 库无缝相互调用！
         *    - 典型运行在 JVM 上的其他语言：
         *      * Kotlin：现代 Android 开发第一官方语言，语法现代化，完全运行在 JVM 上。
         *      * Scala：融合面向对象与函数式编程的高性能语言，大数据神器 Spark 和 Kafka 底层大量采用。
         *      * Groovy：高度动态灵活的脚本语言，常用于 Gradle 构建脚本与微服务热脚本规则引擎。
         *      * Clojure：JVM 上的 Lisp 方言函数式编程语言。
         *
         * 4. 架构总结：
         *    Java 语言成就了 JVM，而 JVM 如今超越了 Java 语言，成为了支撑整个软件工业的通用虚拟生态基石。
         */
        System.out.println("3. JVM 与 Java 区别：Java 是高级编程语言，JVM 是通用的字节码虚拟平台；JVM 完全解耦 Java，支持 Kotlin/Scala 多语言生态。");
    }

    /**
     * 初始化控制台字符编码，解决 Windows 环境终端输出中文乱码的问题。
     */
    private static void initConsoleEncoding() {
        try {
            System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}
