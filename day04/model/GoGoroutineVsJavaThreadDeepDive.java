package model;

/**
 * Go 语言协程 (Goroutine) 与 Java 传统线程 (Thread) 深度对比
 *
 * 面试原题：
 * - Go的协程和Java 的线程有啥区别？
 *
 * 核心考点：
 * 1. 调度模型架构：Go 的 GMP 调度模型 vs Java 传统 1:1 内核调度模型
 * 2. 栈内存分配与动态扩缩容机制（2KB 动态连续栈 vs 1MB 固定系统栈）
 * 3. 上下文切换开销（用户态寄存器切换 vs 内核态陷阱与 TLB 刷新）
 * 4. 并发通信哲学：Go 的 CSP (Channel 管道通信) vs Java 的共享内存锁机制
 * 5. Java 21 虚拟线程 (Virtual Thread / Loom) 对比 Go 协程的异同
 */
public class GoGoroutineVsJavaThreadDeepDive {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】Go 协程 (Goroutine) vs Java 平台线程 (Thread) 深度对比");
        System.out.println("================================================================================");
        explainCoreDifferences();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【调度模型深剖】Go 的 GMP 调度模型原理与工作机制");
        System.out.println("--------------------------------------------------------------------------------");
        explainGoGmpModel();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【核心维度对比表】全方位全生命周期参数对照");
        System.out.println("--------------------------------------------------------------------------------");
        printComparisonTable();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【现代演进】Java 21 虚拟线程 (Loom) 与 Go 协程的同与不同");
        System.out.println("--------------------------------------------------------------------------------");
        explainJavaVirtualThreadVsGoGoroutine();
    }

    /**
     * 核心区别高度提炼
     */
    public static void explainCoreDifferences() {
        System.out.println("在互联网大厂高并发系统设计与面试中，Go 协程与 Java 传统线程有 5 大本质区别：");
        System.out.println("1. 概念实体与调度主体不同：");
        System.out.println("   - Go Goroutine：是纯用户态的轻量级执行实体，完全由 Go Runtime 调度器管理，操作系统内核完全无感知。");
        System.out.println("   - Java 传统线程：是操作系统内核线程（1:1 模型），由 OS 内核调度器统一分配时间片和上下文调度。");
        System.out.println("2. 栈内存初始开销与扩容机制：");
        System.out.println("   - Go Goroutine：起步只需约 2KB 栈空间，采用动态连续栈机制（Contiguous Stack），按需自动扩容收缩（最大可达 1GB）。");
        System.out.println("   - Java 传统线程：启动即固定分配默认 1MB 栈内存（-Xss），无法动态缩容，调用栈深则容易 StackOverflowError。");
        System.out.println("3. 调度与上下文切换代价：");
        System.out.println("   - Go Goroutine：在用户态执行，仅保存 3 个核心寄存器（PC, SP, DX），切换耗时约几十纳秒（十几 CPU 周期）。");
        System.out.println("   - Java 传统线程：切换必须通过系统调用陷入内核态（Ring 3 -> Ring 0），需保存整套 CPU 寄存器并导致 TLB 缓存失效，耗时 1~2 微秒。");
        System.out.println("4. 并发通信哲学：");
        System.out.println("   - Go 哲学：'Do not communicate by sharing memory; instead, share memory by communicating.'（CSP 模型，优先用 Channel 管道通信）。");
        System.out.println("   - Java 哲学：'基于共享内存与互斥锁'（Shared Memory + Monitor / Synchronized / Lock / CAS）。");
        System.out.println("5. 并发承载规模：");
        System.out.println("   - Go 单机可轻松并发调度上百万个 Goroutine。");
        System.out.println("   - Java 传统线程单机达到几千个就会发生严重内存挤占与 CPU 上下文切换颠簸（Thashing）。");
    }

    /**
     * Go 著名的 GMP 调度模型深度解析
     */
    public static void explainGoGmpModel() {
        System.out.println("Go 语言之所以具备极致的并发性能，归功于其经典的 GMP 调度模型：");
        System.out.println("1. GMP 三大核心结构：");
        System.out.println("   - G (Goroutine)：协程实体，保存栈信息、程序计数器 PC、绑定的任务函数和当前状态（_Gwaiting, _Grunnable 等）。");
        System.out.println("   - M (Machine)：操作系统内核线程实体，由 OS 调度，是物理 CPU 执行指令的真实载体。");
        System.out.println("   - P (Processor)：逻辑处理器，管理一组 Goroutine 的上下文资源与本地队列，数量通常等于 CPU 核心数（GOMAXPROCS）。");
        System.out.println("2. GMP 高性能调度机制：");
        System.out.println("   - 本地运行队列 (Local Run Queue)：每个 P 维护一个容量为 256 的本地队列，无锁高效弹出执行。");
        System.out.println("   - 工作窃取 (Work Stealing)：当某个 P 的本地队列执行完毕，会随机从其他 P 的本地队列窃取一半的 G 来执行，实现负载均衡。");
        System.out.println("   - 系统调用剥离 (Handoff P)：当 M 执行 G 发起阻塞的系统调用（如读写磁盘/网络）时，M 与 P 立即解绑，");
        System.out.println("     P 会带着其余所有的 G 去寻找空闲的 M 或创建新 M 继续执行，保证 CPU 算力不被阻塞浪费。");
        System.out.println("   - 网络多路复用器 (NetPoller)：网络 I/O 阻塞的 G 会挂载到 epoll/kqueue 的 NetPoller 模块中，就绪后再放回 P 的队列。");
        System.out.println("   - 抢占式调度 (Preemption)：sysmon 监控线程会标记运行超过 10ms 的 G，利用协作检查点或信号机制（SIGURG）实现抢占。");
    }

    /**
     * 打印两者的多维度对比表格
     */
    public static void printComparisonTable() {
        System.out.printf("%-18s | %-30s | %-30s\n", "对比维度", "Go 协程 (Goroutine)", "Java 传统线程 (Platform Thread)");
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("%-18s | %-30s | %-30s\n", "调度主体", "Go Runtime (用户空间)", "OS 内核 (Kernel Space)");
        System.out.printf("%-18s | %-30s | %-30s\n", "调度模型", "M:N 混合模型 (GMP)", "1:1 内核映射模型");
        System.out.printf("%-18s | %-30s | %-30s\n", "初始栈内存", "约 2KB (动态连续栈弹性伸缩)", "约 1MB (固定栈空间，不可伸缩)");
        System.out.printf("%-18s | %-30s | %-30s\n", "上下文切换开销", "约 10~50 纳秒 (纯用户态)", "约 1~2 微秒 (用户态与内核态切换)");
        System.out.printf("%-18s | %-30s | %-30s\n", "单机承载量", "数十万到百万级别", "数千级别 (过万易 OOM 或高负载死机)");
        System.out.printf("%-18s | %-30s | %-30s\n", "核心通信机制", "Channel 管道 (CSP 理论)", "共享内存变量 + synchronized / Lock");
        System.out.printf("%-18s | %-30s | %-30s\n", "并发控制关键字", "go 关键字、select", "Thread, Runnable, synchronized");
    }

    /**
     * Java 21 虚拟线程与 Go 协程的区别
     */
    public static void explainJavaVirtualThreadVsGoGoroutine() {
        System.out.println("Java 21 引入了虚拟线程 (Virtual Thread / Project Loom)，在架构理念上对标了 Go 协程：");
        System.out.println("1. 相同点：");
        System.out.println("   - 都采用 M:N 用户态调度；内存开销都仅数百字节；单机都支持百万并发；遇阻塞 I/O 都自动卸载挂起。");
        System.out.println("2. 关键差异点：");
        System.out.println("   - 编程思维差异：Go 鼓励 CSP 模式与 Channel 异步编程；Java 虚拟线程坚持传统的【Thread-per-Request 同步阻塞编码心智】，");
        System.out.println("     开发者写着传统的阻塞代码，JVM 在底层自动转换为非阻塞异步调度，零改动无缝兼容庞大的旧有生态。");
        System.out.println("   - 载体与调度机制：Java 虚拟线程底层基于 ForkJoinPool 作为载体线程池，底层使用 Continuation 保存/恢复调用栈；");
        System.out.println("   - 线程 Pin 机制（固定宿主）：Java 虚拟线程如果在 synchronized 块中或 native 本地方法中发生阻塞，");
        System.out.println("     目前仍会导致底层 Carrier Thread 被固定（Pinned），无法卸载（官方推荐使用 ReentrantLock 替代）。");
    }
}
