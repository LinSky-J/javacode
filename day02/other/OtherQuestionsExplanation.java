package other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目综合解答与实战演练：
 * 1. 有一个学生类，想按照分数排序，再按学号排序，应该怎么做?
 * 2. Native方法解释一下
 * 3. Java 进程是怎么跟操作系统交互的？
 */
public class OtherQuestionsExplanation {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("问题 1: 有一个学生类，想按照分数排序，再按学号排序，应该怎么做?");
        System.out.println("================================================================================");
        explainAndDemonstrateStudentSorting();

        System.out.println("\n================================================================================");
        System.out.println("问题 2: Native方法解释一下");
        System.out.println("================================================================================");
        explainNativeMethods();

        System.out.println("\n================================================================================");
        System.out.println("问题 3: Java 进程是怎么跟操作系统交互的？");
        System.out.println("================================================================================");
        explainOsInteraction();
    }

    /**
     * 问题 1 深度解答与多维度实战演练
     */
    private static void explainAndDemonstrateStudentSorting() {
        System.out.println("【理论解答】");
        System.out.println("排序有两种核心设计途径：");
        System.out.println("1. 实现 Comparable<Student> 接口（自然排序）：");
        System.out.println("   - 侵入实体类，适合全局唯一且固定的默认排序规则。");
        System.out.println("2. 使用 Comparator<Student> 接口（策略模式 / 定制排序）：");
        System.out.println("   - 不修改实体类，解耦业务，支持多种不同的排序策略（推荐方式）。");
        System.out.println("   - 避免使用直接相减 (s2.getScore() - s1.getScore())，应使用 Double.compare 与 Long.compare，防止精度丢失或溢出。");

        // 构造测试数据集合（包含相同分数，验证二级学号排序）
        List<Student> students = new ArrayList<>();
        students.add(new Student(1004, "赵六", 95.0));
        students.add(new Student(1001, "张三", 88.5));
        students.add(new Student(1002, "李四", 95.0));
        students.add(new Student(1005, "孙七", 72.0));
        students.add(new Student(1003, "王五", 88.5));

        System.out.println("\n[原始学生列表]:");
        students.forEach(s -> System.out.println("  " + s));

        // 方案 A: 使用自定义独立 Comparator 类（面向对象/传统方式）
        List<Student> listA = new ArrayList<>(students);
        Collections.sort(listA, new StudentScoreComparator());
        System.out.println("\n[方案 A - 自定义 StudentScoreComparator 排序结果（分数降序，学号升序）]:");
        listA.forEach(s -> System.out.println("  " + s));

        // 方案 B: 使用 Java 8+ Comparator 链式组合器（现代推荐方式）
        List<Student> listB = new ArrayList<>(students);
        listB.sort(
                Comparator.comparingDouble(Student::getScore)
                          .reversed() // 分数降序
                          .thenComparingLong(Student::getId) // 学号升序
        );
        System.out.println("\n[方案 B - Java 8 Comparator.comparingDouble().reversed().thenComparingLong() 结果]:");
        listB.forEach(s -> System.out.println("  " + s));

        // 方案 C: 使用 Stream API 生成新的已排序列表（无副作用函数式编程）
        List<Student> listC = students.stream()
                .sorted(Comparator.comparingDouble(Student::getScore).reversed()
                                  .thenComparingLong(Student::getId))
                .collect(Collectors.toList());
        System.out.println("\n[方案 C - Stream.sorted() 流式排序结果]:");
        listC.forEach(s -> System.out.println("  " + s));
    }

    /**
     * 问题 2 深度解答与代码示例
     */
    private static void explainNativeMethods() {
        System.out.println("【理论解答】");
        System.out.println("1. 概念定义：");
        System.out.println("   native 是 Java 语言的一个关键字修饰符。被 native 修饰的方法只有声明，没有 Java 方法体。");
        System.out.println("   具体逻辑由底层非 Java 语言（如 C/C++、汇编）实现，编译为动态链接库（Windows .dll / Linux .so）。");
        System.out.println("2. 核心桥梁：JNI（Java Native Interface）");
        System.out.println("   调用路径: Java 代码 -> JNI 环境规范 -> C/C++ 动态链接库 -> OS 内核 / 硬件寄存器。");
        System.out.println("3. 为什么需要 Native 方法？");
        System.out.println("   - 硬件级特权操作：JVM 沙箱无法直接访问硬件中断、寄存器、特定驱动。");
        System.out.println("   - 极致计算性能：音视频编解码（FFmpeg）、高性能密码学、图形加速库（OpenGL）。");
        System.out.println("   - 操作系统底层交互：JVM 启动、线程挂起/恢复、系统高精度时间、内存页分配。");
        System.out.println("   - 复用既有 C/C++ 库生态。");
        System.out.println("4. 两种注册机制：");
        System.out.println("   - 静态注册：依据命名规则（Java_全包名_类名_方法名）在加载时通过符号表解析查找。");
        System.out.println("   - 动态注册：在 C 的 JNI_OnLoad 回调中通过 (*env)->RegisterNatives 显式绑定，效率更高。");
        System.out.println("5. 代价与风险：");
        System.out.println("   - 丧失跨平台性，需针对不同系统分别编译。");
        System.out.println("   - 脱离 JVM GC 管控，C 堆内存泄漏不可自动回收；若发生段错误（SIGSEGV）将导致整个 JVM 进程直接崩溃。");

        System.out.println();
        NativeMethodDemo.displayJdkNativeExamples();
    }

    /**
     * 问题 3 深度解答与运行时架构演练
     */
    private static void explainOsInteraction() {
        System.out.println("【理论解答】");
        System.out.println("Java 进程与操作系统的底层交互包含五大核心维度：");
        System.out.println("1. 进程模型与系统调用（System Call）：");
        System.out.println("   - JVM 是运行在操作系统用户态（Ring 3）的一个普通进程（java.exe）。");
        System.out.println("   - 任何涉及硬件资源的操作（写磁盘、发网络报文、取系统时间、分配大块内存）均需发起系统调用。");
        System.out.println("   - CPU 触发软中断/快速系统调用（syscall 指令），从用户态陷入内核态（Ring 0），由 OS 执行特权操作后返回。");
        System.out.println("2. 线程调度模型：");
        System.out.println("   - 平台线程（1:1 模型）：每个 Java Thread 对应一个内核线程（Linux task_struct），由 OS 内核调度器（CFS）分配时间片。");
        System.out.println("   - 虚拟线程（M:N 模型，Java 21）：JVM 在用户态实现轻量级调度器，海量协程复用少量内核载体线程，消除频繁进出内核态的开销。");
        System.out.println("3. 内存管理与缺页异常（Page Fault）：");
        System.out.println("   - JVM 启动时通过 mmap / VirtualAlloc 向 OS 申请连续的虚拟地址空间。");
        System.out.println("   - 只有首次写入对象数据时，硬件 MMU 触发缺页异常（Page Fault），OS 内核才分配物理内存页帧（Page Frame）。");
        System.out.println("   - DirectByteBuffer / JNI 申请堆外内存，支持直接内存访问（DMA）实现网卡/磁盘零拷贝传输。");
        System.out.println("4. I/O 多路复用机制：");
        System.out.println("   - Java NIO 的 Selector 底层直接对接 OS 的多路复用 API：Linux epoll、Windows IOCP、macOS kqueue。");
        System.out.println("   - 单个线程监听成千上万个 Socket 文件描述符（FD）的读写就绪事件。");
        System.out.println("5. 操作系统信号与停机（Signal & Shutdown）：");
        System.out.println("   - 操作系统向 Java 进程发送 POSIX 信号（如 SIGTERM 正常终止，SIGINT 控制台中断，SIGQUIT 线程快照）。");
        System.out.println("   - JVM 捕获信号后依次执行 Runtime.getRuntime().addShutdownHook() 注册的钩子，释放系统资源后平滑退出。");

        System.out.println();
        OsInteractionArchitecture.displayProcessOsMetrics();
    }
}
