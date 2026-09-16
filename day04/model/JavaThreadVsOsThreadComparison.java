package model;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Java 线程与操作系统线程底层关系深度剖析
 *
 * 面试原题：
 * - java里面的线程和操作系统的线程一样吗？
 *
 * 核心考点：
 * 1. 线程模型的三大流派：1:1 (内核级线程)、M:1 (用户级线程/绿色线程)、M:N (混合/协程模型)
 * 2. Java 线程发展历史：JDK 1.1 绿色线程 -> JDK 1.3 起 1:1 内核级映射 -> Java 21 虚拟线程 M:N 演进
 * 3. 1:1 模型的优势与代价（上下文切换 Context Switch 开销、默认 1MB 栈内存开销）
 * 4. HotSpot 源码链路剖析：Thread.start() -> start0() -> JVM_StartThread -> os::create_thread -> pthread_create
 */
public class JavaThreadVsOsThreadComparison {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】Java 线程与操作系统内核线程关系深度解析");
        System.out.println("================================================================================");
        explainThreadModelEvolution();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【HotSpot 源码链路】Java 线程是如何一步步变成操作系统原生线程的？");
        System.out.println("--------------------------------------------------------------------------------");
        explainHotSpotThreadCreationChain();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战观测】当前 JVM 运行时的平台线程与 OS 交互状态观测");
        System.out.println("--------------------------------------------------------------------------------");
        inspectCurrentJvmThreads();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【成本对比】传统 1:1 平台线程 vs Java 21 虚拟线程 (Loom)");
        System.out.println("--------------------------------------------------------------------------------");
        comparePlatformVsVirtualThreadCosts();
    }

    /**
     * 线程模型演进史深度讲解
     */
    public static void explainThreadModelEvolution() {
        System.out.println("1. 总体定性回答：");
        System.out.println("   - 在现代主流 JVM（如 Oracle HotSpot / OpenJDK）中，Java 的平台线程（Platform Thread）");
        System.out.println("     本质上与操作系统的原生内核级线程是【1:1 完全对应】的！");
        System.out.println("   - 在 Linux 操作系统中，每一个 Java 线程直接对应一个 POSIX 线程 (pthread_create)；");
        System.out.println("   - 在 Windows 操作系统中，直接对应一个 Win32 线程 (CreateThread)。");
        System.out.println();
        System.out.println("2. Java 线程架构的三代演进：");
        System.out.println("   (1) 第一代：早期绿色线程（Green Threads，JDK 1.1 及以前）：");
        System.out.println("       * 采用 M:1 用户态模型，由 JVM 自己实现调度器，操作系统内核完全感知不到多线程。");
        System.out.println("       * 致命缺陷：无法利用多核 CPU 算力；一旦某个线程发起阻塞 I/O，整个 JVM 进程全部停摆。");
        System.out.println("   (2) 第二代：1:1 内核级线程模型（JDK 1.3 至今的默认标准）：");
        System.out.println("       * 委托操作系统内核进行线程调度、分配 CPU 时间片、优先级处理和抢占式中断。");
        System.out.println("       * 优点：真正的并行计算，稳定性极高，成熟完备。");
        System.out.println("       * 缺点：内存重（默认栈 1MB）、创建慢、上下文切换需陷进内核态（保存寄存器/程序计数器/TLB 失效），");
        System.out.println("              单机支撑线程数通常在几千级别，无法应对 C1000K 高并发 I/O 场景。");
        System.out.println("   (3) 第三代：虚拟线程（Virtual Threads / Project Loom，Java 21 LTS 正式发布）：");
        System.out.println("       * 回归并在现代架构下重塑 M:N 调度模型！数百万个轻量级虚拟线程由 ForkJoinPool 调度映射到少量的操作系统载体线程（Carrier Thread）。");
        System.out.println("       * 遇到 I/O 阻塞自动卸载（Continuation.yield），仅耗费几百字节内存，重塑 Java 高并发模型。");
    }

    /**
     * HotSpot 源码层面创建线程的调用链路剖析
     */
    public static void explainHotSpotThreadCreationChain() {
        System.out.println("Java Thread 启动时的 HotSpot C++ 源码调用轨迹：");
        System.out.println("   1. Java 应用层：new Thread(runnable).start();");
        System.out.println("   2. JDK 源码层：Thread.start() -> native start0();");
        System.out.println("   3. JVM 核心入口：jvm.cpp 中的 JVM_StartThread 方法；");
        System.out.println("   4. HotSpot 线程对象：new JavaThread(&thread_entry, stack_size);");
        System.out.println("   5. 操作系统平台适配：os::create_thread(this, thr_type, stack_size);");
        System.out.println("      - 在 Linux 下（os_linux.cpp）：调用 POSIX API pthread_create();");
        System.out.println("      - 在 Windows 下（os_windows.cpp）：调用 Win32 API _beginthreadex();");
        System.out.println("   6. 操作系统内核创建内核调度实体 (Kernel Thread / Task)，分配内核栈与 Task Struct；");
        System.out.println("   7. JVM 调用 os::start_thread(this)，子线程正式进入操作系统就绪队列，等待分配 CPU 执行 run()。");
    }

    /**
     * 运行时观测当前 JVM 内的线程信息
     */
    public static void inspectCurrentJvmThreads() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        System.out.println("当前 JVM 活跃线程总数: " + threadMXBean.getThreadCount());
        System.out.println("峰值线程数: " + threadMXBean.getPeakThreadCount());
        System.out.println("守护线程数: " + threadMXBean.getDaemonThreadCount());

        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
        System.out.println("部分关键系统级线程清单：");
        for (ThreadInfo info : threadInfos) {
            System.out.printf("   - 线程 ID: %-3d | 状态: %-13s | 名称: %s\n",
                    info.getThreadId(), info.getThreadState(), info.getThreadName());
        }
        System.out.println("【结论】：可以看到包括 GC 线程、Finalizer、Signal Dispatcher、Attach Listener 等，");
        System.out.println("         全部直接由底层 OS 调度分配系统资源。");
    }

    /**
     * 平台线程与虚拟线程资源消耗对比
     */
    public static void comparePlatformVsVirtualThreadCosts() {
        System.out.println("核心指标对比表：");
        System.out.printf("%-18s | %-28s | %-28s\n", "对比维度", "传统 Java 平台线程 (1:1)", "Java 21 虚拟线程 (M:N)");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-18s | %-28s | %-28s\n", "调度主体", "操作系统内核 (OS Kernel)", "JVM 调度器 (ForkJoinPool)");
        System.out.printf("%-18s | %-28s | %-28s\n", "默认栈内存", "约 1MB (-Xss 可配置)", "初始数百字节 (堆上动态伸缩)");
        System.out.printf("%-18s | %-28s | %-28s\n", "创建耗时", "约 1毫秒 (需陷入内核态)", "约 1微秒 (纯堆对象分配)");
        System.out.printf("%-18s | %-28s | %-28s\n", "上下文切换开销", "1~2微秒 (寄存器+TLB换入换出)", "数十纳秒 (纯用户态指针切换)");
        System.out.printf("%-18s | %-28s | %-28s\n", "单机最大容量", "数千至上万级别 (受内存制约)", "百万至千万级别 (受堆内存上限)");
        System.out.printf("%-18s | %-28s | %-28s\n", "适合场景", "CPU 密集型计算任务", "高并发 I/O 阻塞型网络服务");
    }
}
