package java21;

/**
 * Java 21 虚拟线程（Virtual Threads - Project Loom）底层模型与架构演进分析。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class VirtualThreadArchitecture {

    /**
     * 架构对比：传统平台线程 (Platform Thread) vs Java 21 虚拟线程 (Virtual Thread)
     */
    public static void explainThreadingEvolution() {
        System.out.println("   [1. 传统平台线程模型（1:1 模型）的根本瓶颈]:");
        System.out.println("      - 映射机制：Java 中的一个 Thread 直接对应一个操作系统内核物理线程。");
        System.out.println("      - 内存开销：每个平台线程默认需要预分配 1MB 的独立栈内存。");
        System.out.println("      - 调度成本：线程切换需要经过 OS 内核态与用户态上下文切换（CPU 寄存器、页表刷新），开销极大。");
        System.out.println("      - 瓶颈上限：单台服务器通常仅能支撑几千个线程，一旦超出就引发内存耗尽或严重线程颠簸。");

        System.out.println("\n   [2. Java 21 虚拟线程模型（M:N 协程调度模型）的革命性突破]:");
        System.out.println("      - 内存微量：一个虚拟线程在堆内存仅占用几百字节，单机支持轻松创建 100 万+ 虚拟线程！");
        System.out.println("      - 调度机制：虚拟线程由 JVM 完全在用户态调度，挂载（Mount）在少量的载体线程（Carrier Thread）上执行。");
        System.out.println("      - 遇到阻塞 I/O（如网络读取、DB查询、Thread.sleep）：");
        System.out.println("        虚拟线程会自动从载体线程上卸载（Unmount）并将调用栈保存在堆中，载体线程立刻去执行其他任务！");
        System.out.println("      - I/O 就绪后：由 JVM 重新调度一个空闲载体线程将其挂载（Mount）并恢复栈帧继续向下执行！");
        System.out.println("      - 革命性价值：开发人员不需要再写反人类的响应式反应堆异步代码（WebFlux/RxJava），");
        System.out.println("        直接使用最直观易懂的传统阻塞式同步代码，即可直接获得与异步框架相同的百万级超高吞吐！");
    }
}
