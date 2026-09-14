package other;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;

/**
 * Java 进程与操作系统交互深度架构分析
 *
 * 核心架构与交互通道剖析：
 * 1. 进程身份：
 *    - JVM 本身只是运行在操作系统用户态（User Space, Ring 3）的一个普通宿主进程（如 java.exe）。
 *    - 操作系统为该进程分配独立的虚拟地址空间、进程 ID（PID）、环境变量和文件描述符表（FD Table）。
 * 2. 系统调用（System Call，Syscall）：
 *    - 用户态与内核态隔离（Ring 3 vs Ring 0）。
 *    - 当 Java 需要操作硬件资源（磁盘读写、网卡收发、创建线程、获取系统时钟）时，Java 无法越权操作。
 *    - 交互路径：Java 代码 -> JVM C++ 核心层 -> C 标准运行时库（glibc / msvcrt）
 *               -> CPU 特权指令（syscall / sysenter） -> 陷入 OS 内核态（Kernel Space） -> 驱动/硬件。
 * 3. 线程交互模型：
 *    - 传统平台线程（1:1 模型）：每个 Java Thread 底层对应一个真实的操作系统内核线程（Linux pthread / task_struct）。
 *      线程状态、优先级和 CPU 时间片调度完全交由 OS 内核调度器（如 Linux CFS）负责。
 *    - 现代虚拟线程（M:N 模型，Java 21+）：JVM 在用户态自主调度轻量级虚拟线程，复用少量的操作系统载体线程，
 *      大幅减少了用户态到内核态的上下文切换（Context Switch）损耗。
 * 4. 内存交互机制：
 *    - 堆内存预留与提交：JVM 启动时通过 mmap() / VirtualAlloc() 向操作系统预留连续的虚拟地址空间。
 *    - 缺页异常（Page Fault）：只有当 JVM 真正向内存页写入对象数据时，MMU（内存管理单元）触发缺页中断，
 *      操作系统内核才会真正分配物理内存页帧（Page Frame）并更新页表映射。
 *    - 堆外内存与零拷贝：DirectByteBuffer / JNI 申请的堆外内存直接位于 C 堆空间，
 *      可直接用于网卡/磁盘的 DMA（直接内存访问），规避 JVM 堆内到堆外的额外内存拷贝。
 * 5. I/O 多路复用交互：
 *    - Java NIO 的 Selector 底层直接对接操作系统的 I/O 多路复用系统调用：
 *      Linux 下映射为 epoll (epoll_create, epoll_ctl, epoll_wait)；
 *      Windows 下映射为 IOCP / select；macOS 下映射为 kqueue。
 * 6. 操作系统信号与生命周期管理：
 *    - 信号通信：操作系统通过软中断向 JVM 进程发送 POSIX 信号（如 SIGINT、SIGTERM、SIGQUIT、SIGSEGV）。
 *    - 优雅关机：JVM 内部注册了 Signal Handler，捕获到 SIGTERM 后触发用户通过
 *      Runtime.getRuntime().addShutdownHook() 注册的清理任务，平滑释放系统句柄。
 */
public class OsInteractionArchitecture {

    /**
     * 演示获取当前 JVM 进程在操作系统层面的运行时指标
     */
    public static void displayProcessOsMetrics() {
        System.out.println("--- JVM 进程与操作系统交互指标实测 ---");

        // 1. 获取当前 Java 进程在操作系统中的唯一 PID
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        String pid = processName.split("@")[0];
        System.out.println("1. 操作系统进程 ID (PID): " + pid);

        // 2. 获取操作系统名称与架构
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println("2. 宿主操作系统: " + osName + " (" + osArch + "), 可用逻辑核心数: " + availableProcessors);

        // 3. 获取操作系统分配给 JVM 进程的内存指标
        long totalMemory = Runtime.getRuntime().totalMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        System.out.println(String.format("3. JVM 虚拟内存分配: 初始总提交=%.2f MB, 最大上限=%.2f MB, 当前空闲=%.2f MB",
                totalMemory / (1024.0 * 1024.0),
                maxMemory / (1024.0 * 1024.0),
                freeMemory / (1024.0 * 1024.0)));

        // 4. 堆外内存（Direct Memory）与系统零拷贝交互
        int directBufferSize = 1024 * 1024; // 1MB 堆外内存
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(directBufferSize);
        directBuffer.putInt(123456);
        directBuffer.flip();
        System.out.println("4. 分配操作系统堆外内存 (DirectByteBuffer): 1MB, 绕过 JVM 堆由 OS DMA 直接读写, 读取验证: " + directBuffer.getInt());
    }
}
