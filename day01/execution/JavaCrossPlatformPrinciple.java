package execution;

/**
 * 面试专题：Java 跨平台本质 与 混合编译/解释执行模式深度解析。
 *
 * 本类对应面试核心题目：
 * 1. Java为什么是跨平台的?
 * 2. 为什么Java解释和编译都有?
 *
 * 设计目标：
 * 彻底击破“背诵八股文”的局限，用系统级视角揭示 JVM 与字节码的架构精髓，
 * 讲清现代 HotSpot 虚拟机解释器与 JIT 编译器的分工与协同机制。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaCrossPlatformPrinciple {

    public static void main(String[] args) {
        initConsoleEncoding();
        System.out.println("==================================================");
        System.out.println("     Java 跨平台原理 与 混合编译执行机制深度解析     ");
        System.out.println("==================================================");

        explainCrossPlatformPrinciple();
        explainWhyBothCompilationAndInterpretation();
        explainTieredCompilationArchitecture();

        System.out.println("==================================================");
        System.out.println("             核心原理解析完毕，请细读类中注释       ");
        System.out.println("==================================================");
    }

    /**
     * 第一部分：Java 为什么是跨平台的？
     *
     * 面试核心金句：
     * “跨平台的是 Java 程序和字节码，不跨平台的是 JVM 虚拟机！”
     */
    public static void explainCrossPlatformPrinciple() {
        /*
         * 1. 传统 C/C++ 为什么难以跨平台？
         *    - C/C++ 源码直接通过平台编译器编译为目标机器码（如 x86 汇编生成的机器指令）。
         *    - Windows、Linux、macOS 提供的系统调用接口（Syscalls）各不相同，CPU 架构（x86, ARM）指令集也不同。
         *    - 想要跨平台，必须针对每个平台修改与系统相关的代码，并针对该平台重新交叉编译。
         *
         * 2. Java 的解耦创新设计：字节码（Bytecode）与虚拟计算机（JVM）
         *    - 第一步：统一标准规范。Java 源码（.java）被统一的 javac 编译器编译成平台无关的【字节码文件（.class）】。
         *      字节码是一套由 JVM 规范定义的标准虚拟指令集，与任何具体的物理 CPU 架构完全无关。
         *    - 第二步：平台专属翻译官（JVM）。Oracle / OpenJDK 社区针对 Windows、Linux、macOS 以及
         *      x86_64、AArch64、RISC-V 等不同的操作系统和硬件，分别使用 C/C++ 编写了特定版本的 JVM 虚拟机。
         *    - 第三步：运行时对接。同一份编译好的 .class 字节码，拿到 Windows 上的 JVM 就能在 Windows 上跑；
         *      拿到 Linux 上的 JVM 就能在 Linux 上跑。
         *
         * 3. 架构思想升华：
         *    计算机科学领域的经典法则：“任何软件工程中的问题，都可以通过增加一个中间间接层（Indirection Layer）来解决。”
         *    JVM 与 字节码，就是 Java 解决操作系统和物理硬件异构性问题所引入的中间抽象层！
         */
        System.out.println("1. 跨平台原理：Java 依靠标准字节码作为媒介，由各操作系统专属的 JVM 抹平底层硬件和系统调用差异。");
    }

    /**
     * 第二部分：为什么 Java 解释和编译都有？（混合执行模式）
     *
     * 面试核心追问：
     * 为什么不全部用解释执行？为什么不全部预先编译？为什么不启动时把字节码全部编译完再跑？
     */
    public static void explainWhyBothCompilationAndInterpretation() {
        /*
         * 1. 为什么不全部采用“解释执行”？
         *    - 痛点：早期的 Java 1.0 / 1.1 确实是纯解释执行的。解释器逐条读取字节码并翻译为机器码执行，
         *      导致同一个循环体被执行 100 万次，就被重复翻译了 100 万次，极其低效，Java 因此被讽刺为“树懒语言”。
         *    - 结论：纯解释执行无法满足现代企业级高并发、高性能计算的严苛需求。
         *
         * 2. 为什么不全部采用“运行前全量编译为机器码（AOT）”？
         *    - 痛点一：如果直接把 .java 源码全量编译成本地机器码，那就丧失了“平台无关性”，倒退回了 C/C++ 的老路。
         *    - 痛点二：动态性丧失。Java 是一门高度动态的语言，支持反射、动态代理、运行时自定义类加载（Class Loading）。
         *      运行前很难完全确定运行期究竟会加载哪些类。
         *    - 痛点三：丧失运行时基于画像的动态优化（PGO - Profile-Guided Optimization）。JIT 编译器能根据运行期实际
         *      发生的数据流和分支预测（例如某个分支 99.9% 都不走）进行极其激进的内联和消除，这甚至是静态编译很难做到的。
         *
         * 3. 为什么不“在程序启动时把所有字节码编译为机器码再运行”？
         *    - 痛点：一个中大型 Spring Boot 微服务依赖数百个 jar 包，包含数万个类和数十万个方法。
         *      如果启动时必须全部编译成机器码，启动耗时可能会长达几十分钟，且编译需要消耗极大的内存，严重影响开发与运维。
         *
         * 4. 黄金折中方案：混合执行（Mixed Mode）
         *    - 解释器（Interpreter）：程序启动时，解释器立刻介入执行字节码，无需等待漫长的编译，实现【快速启动与极速响应】。
         *    - 即时编译器（JIT - Just In Time）：在解释器执行的同时，JVM 默默监控代码运行状态。对于频繁调用的热点代码
         *      （Hot Spot Code），JIT 在后台异步将其编译为本地 CPU 机器码并存入 CodeCache，后续直接执行机器码，实现【峰值极致性能】。
         */
        System.out.println("2. 混合执行动因：解释器保证了【极速启动与响应】，JIT 编译器保证了【长期运行的高峰性能】，两者优势互补。");
    }

    /**
     * 第三部分：深入了解 HotSpot 的分层编译架构（Tiered Compilation）
     *
     * 面试拔高亮点：
     * 掌握分层编译的五个阶段，向面试官展示高级开发者的技术底蕴。
     */
    public static void explainTieredCompilationArchitecture() {
        /*
         * 从 Java 7 引入、Java 8 开始默认开启的“分层编译”体系：
         * 1. 第 0 层：解释执行（Interpreter）。收集基础的调用次数和运行时分析数据（Profiling）。
         * 2. 第 1 层：C1 客户端编译器（Simple C1）。纯简单编译为机器码，不带运行时分析信息。
         * 3. 第 2 层：带有限分析数据的 C1 编译（Limited C1 Profiling）。
         * 4. 第 3 层：带完整分析数据的 C1 编译（Full C1 Profiling）。统计方法调用计数器和回边计数器。
         * 5. 第 4 层：C2 服务端编译器（Server Compiler）。根据前面收集到的完备画像数据，
         *    执行最彻底、最激进的深度全局优化（逃逸分析标量替换、循环展开、虚方法内联），生成终极高效机器码。
         *
         * 如果优化做错了（例如运行期突然出现了一个以前从未出现过的子类实现导致虚方法内联失效），
         * JVM 还支持【逆优化（Deoptimization）】，优雅退回解释执行，确保系统绝对安全稳定。
         */
        System.out.println("3. 架构演进：HotSpot 采用分层编译，结合 C1（快速编译）与 C2（激进深度优化），并支持运行时逆优化回退。");
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
