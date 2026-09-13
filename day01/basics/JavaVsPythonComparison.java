package basics;

/**
 * 面试专题：Java 与 Python 的全维度深度对比与选型思考。
 *
 * 本类对应面试核心题目：
 * Python和Java区别是什么?
 *
 * 设计目标：
 * 从语言类型系统、执行机制、并发模型、语法生态到典型业务场景，
 * 建立系统性的工程技术认知，彻底告别单薄的表面回答。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaVsPythonComparison {

    public static void main(String[] args) {
        initConsoleEncoding();
        System.out.println("==================================================");
        System.out.println("          Java 与 Python 核心特性深度对比报告       ");
        System.out.println("==================================================");

        explainTypingSystem();
        explainExecutionAndPerformance();
        explainConcurrencyModel();
        explainSyntaxAndEngineering();
        explainApplicationDomains();

        System.out.println("==================================================");
        System.out.println("             对比解析完成，请查阅源码深入复习       ");
        System.out.println("==================================================");
    }

    /**
     * 维度一：类型系统的本质区别
     *
     * 面试核心考点：静态类型 vs 动态类型；类型检查发生的时机。
     */
    public static void explainTypingSystem() {
        /*
         * 1. Java 属于【静态强类型语言】：
         *    - 静态类型（Static Typing）：所有变量必须显式（或通过 var 隐式推断）声明类型，
         *      类型检查在【编译期】严格进行。如果类型不匹配，在代码编写和编译阶段就会直接报错。
         *    - 优点：重构安全性极高。在数十万行代码的大型企业项目中，修改一个字段或方法签名，
         *      IDE 和编译器能立刻找出所有受影响的代码位置，将大量隐患消灭在上线之前。
         *
         * 2. Python 属于【动态强类型语言】：
         *    - 动态类型（Dynamic Typing）：变量本身没有固定的静态类型，变量名只是指向内存中对象的引用。
         *      类型检查在【运行期】动态发生。
         *    - 强类型含义：即使是动态类型，Python 也不会进行隐式危险转换（例如字符串 "123" + 数字 456 在 Python 中会报错 TypeError，
         *      而在弱类型的 JavaScript 中会被自动拼接）。
         *    - 优缺点：编写极其灵活迅速，无需冗长类型声明；但是在超大型系统中，如果缺乏完善的单元测试和类型标注（Type Hints），
         *      很多类型不匹配的致命 Bug 只有在特定分支运行到那一行时才会暴露。
         */
        System.out.println("1. 类型系统：Java 是编译期严格检查的静态强类型；Python 是运行期判定的动态强类型。");
    }

    /**
     * 维度二：执行机制与执行性能对比
     *
     * 面试核心考点：混合编译 JIT 与 纯解释器运行的性能差距。
     */
    public static void explainExecutionAndPerformance() {
        /*
         * 1. Java 的执行机制与性能：
         *    - 流程：.java 源码 -> javac 编译为平台无关的 .class 字节码 -> JVM 加载执行。
         *    - 性能引擎：HotSpot 虚拟机的 JIT（即时编译器）会持续监控并把高频热点代码直接编译为物理机机器码，
         *      配合方法内联、逃逸分析、分支预测优化等，使得长期运行的 Java 程序在 CPU 密集型任务下性能非常优异。
         *
         * 2. Python 的执行机制与性能：
         *    - 流程：.py 源码 -> 编译为 .pyc 字节码 -> 由 CPython（官方主流解释器）逐条解释执行虚拟机指令。
         *    - 性能现状：由于纯解释器开销大、缺乏全动态的全局高级 JIT 优化（虽然有 PyPy 等分支，但兼容性存在限制），
         *      纯 Python 代码的计算性能通常比 Java 慢 10 到 50 倍以上。
         *    - 特殊情况：Python 在科学计算与 AI 领域性能极高，是因为其顶层虽然是 Python，
         *      但底层依赖的库（NumPy、PyTorch、TensorFlow）核心全部是用 C、C++ 或 CUDA 编写并编译为本地机器码执行的。
         */
        System.out.println("2. 执行机制：Java 依靠 JIT 编译器在运行期将热点字节码编译为本地机器码，性能远高于纯解释执行的 Python。");
    }

    /**
     * 维度三：并发模型与多线程支持
     *
     * 面试核心考点：GIL（全局解释器锁）与多核 CPU 利用率。
     */
    public static void explainConcurrencyModel() {
        /*
         * 1. Java 的并发模型：
         *    - 原生多线程：Java 中的 Thread 映射为操作系统内核级别的物理线程（1:1 线程模型）。
         *    - 多核支持：多线程能够在多核心 CPU 上实现真正的并行计算（Parallelism），无任何全局锁限制。
         *    - 虚拟线程：Java 21 引入了虚拟线程（M:N 协程调度），单机可以支撑百万级高并发网络 I/O 连接。
         *    - 生态支撑：java.util.concurrent 拥有世界上最完善的并发工具包（CountDownLatch, Semaphore, ConcurrentHashMap 等）。
         *
         * 2. Python 的并发模型（以官方 CPython 为例）：
         *    - GIL 限制：CPython 内部存在著名的全局解释器锁（Global Interpreter Lock，GIL）。
         *      GIL 强制规定：在任何给定的微秒时刻，一个 Python 进程内只允许一个线程在解释器中执行字节码！
         *    - 痛点：即使拥有 64 核 CPU，纯 Python 的多线程也无法进行多核并行计算；多线程仅对 I/O 密集型有效。
         *    - 应对方案：Python 在多核计算时通常采用“多进程（multiprocessing）”或基于协程的异步 I/O（asyncio）。
         */
        System.out.println("3. 并发模型：Java 无全局锁，能充分利用多核 CPU 原生并发；Python（CPython）受 GIL 限制，CPU密集型多线程无法并行。");
    }

    /**
     * 维度四：语法风格、开发效率与工程化能力
     *
     * 面试核心考点：代码严谨度 vs 原型开发速度。
     */
    public static void explainSyntaxAndEngineering() {
        /*
         * 1. Java 的工程化哲学：
         *    - 面向对象正统：必须以类（Class）为组织单位，结构严谨，强调设计模式（Design Patterns）。
         *    - 语法相对严谨冗长：必须有显式的包结构、访问修饰符（public/private）、异常捕获机制。
         *    - 工程化优势：极度规范的代码风格使得超大型团队（数百人同时开发一个庞大系统）能够建立标准化流程，
         *      代码的可读性、可维护性和规范性极高。
         *
         * 2. Python 的极简哲学：
         *    - 核心思想：Life is short, you need Python（人生苦短，我用 Python）。
         *    - 语法极简：强制使用缩进表示代码块，语法糖极其丰富，动态特性与元编程能力极强。
         *    - 开发效率：实现同样一个算法或功能，Python 的代码行数通常只有 Java 的 1/3 到 1/5，
         *      非常适合快速验证业务逻辑和敏捷原型开发。
         */
        System.out.println("4. 工程协同：Java 注重规范、大型系统可维护性与架构严密；Python 注重极简表达与极致开发效率。");
    }

    /**
     * 维度五：典型应用领域与企业生态划分
     *
     * 面试核心考点：技术选型如何根据业务场景做权衡。
     */
    public static void explainApplicationDomains() {
        /*
         * 1. Java 的主战场：
         *    - 企业级大型微服务后端（Spring Boot / Spring Cloud）。
         *    - 互联网高并发、高可用核心交易链路（如电商秒杀、金融支付系统）。
         *    - 大数据基础设施与流批处理平台（Apache Kafka, Apache Flink, Apache Spark）。
         *    - 安卓系统应用原生底层开发。
         *
         * 2. Python 的主战场：
         *    - 人工智能、深度学习与大语言模型（PyTorch, TensorFlow, HuggingFace）。
         *    - 科学计算与金融量化分析（NumPy, Pandas, SciPy, Matplotlib）。
         *    - 网络数据抓取与爬虫（Scrapy, BeautifulReport）。
         *    - 自动化测试与系统运维脚本编写。
         */
        System.out.println("5. 应用场景：Java 统治企业级高并发后端与大数据生态；Python 统治人工智能、数据科学与自动化领域。");
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
