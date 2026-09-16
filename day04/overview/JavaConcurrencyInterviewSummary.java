package overview;

/**
 * Java 并发编程面试题全景梳理与终极大厂答辩汇总
 *
 * 包含用户提出的全部 21 道并发核心面试题逐字精准解析与知识图谱串联：
 * 1.  多线程
 * 2.  Java的内存模型(JMM)介绍一下
 * 3.  java多线程是什么？需要注意什么？
 * 4.  java里面的线程和操作系统的线程一样吗？
 * 5.  使用多线程要注意哪些问题?
 * 6.  保证数据的一致性有哪些方案呢？
 * 7.  线程的创建方式有哪些?
 * 8.  怎么启动线程？
 * 9.  如何停止一个线程的运行?
 * 10. 调用 interrupt 是如何让线程抛出异常的？
 * 11. Java线程的状态有哪些?
 * 12. sleep 和 wait的区别是什么?
 * 13. sleep会释放cpu吗?
 * 14. blocked和waiting有啥区别
 * 15. wait 状态下的线程如何进行恢复到 running 状态?
 * 16. notify 和 notifyAll的区别?
 * 17. notify 选择哪个线程?
 * 18. 不同的线程之间如何通信?
 * 19. 线程间通信方式有哪些?
 * 20. 如何停止一个线程?
 * 21. Go的协程和Java 的线程有啥区别？
 */
public class JavaConcurrencyInterviewSummary {

    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("            Java 并发编程核心面试题全景总览 (day04 综合复盘)                              ");
        System.out.println("==========================================================================================");

        printAnswer(1, "多线程与概念",
                "多线程是指单个进程内并发执行多个执行路径，共享堆和方法区但拥有私有 PC 寄存器与栈。其核心在于压榨多核算力并提升高吞吐能力。");

        printAnswer(2, "Java的内存模型(JMM)介绍一下",
                "JMM 是屏蔽硬件差异的跨平台内存规范，定义了主内存（共享）与工作内存（私有），通过 Happens-Before 原则及内存屏障规范可见性、原子性与有序性。");

        printAnswer(3, "java多线程是什么？需要注意什么？",
                "多线程是并发执行单位；需注意线程安全（原子/可见/有序）、活跃性（死锁/活锁/饥饿）、性能损耗（频繁上下文切换）及资源泄露（ThreadLocal 泄露、无界线程池 OOM）。");

        printAnswer(4, "java里面的线程和操作系统的线程一样吗？",
                "现代主流 HotSpot JVM 下是一样的！Java 平台线程与 OS 内核线程是 1:1 映射（Linux 下为 pthread，Windows 为 Win32 线程）。Java 21 虚拟线程则重构为 M:N 协程调度模型。");

        printAnswer(5, "使用多线程要注意哪些问题?",
                "防范死锁（破除循环等待、按序加锁、tryLock 超时）、防范线程泄露（线程池显式关闭、ThreadLocal.remove）、合理评估核心线程数（CPU 密集型 N+1，IO 密集型估算阻塞比）。");

        printAnswer(6, "保证数据的一致性有哪些方案呢？",
                "1.互斥锁（synchronized/ReentrantLock）；2.无锁CAS（AtomicInteger/LongAdder）；3.读写分离（ReadWriteLock/StampedLock）；4.volatile状态标记；5.线程私有（ThreadLocal）；6.不可变对象；7.分布式锁与分布式事务。");

        printAnswer(7, "线程的创建方式有哪些?",
                "表象有 4 种：继承 Thread、实现 Runnable、实现 Callable+FutureTask、线程池 ThreadPoolExecutor（加 Java 21 虚拟线程）。本质底层只有一种：通过 new Thread 并在操作系统内核创建原生线程。");

        printAnswer(8, "怎么启动线程？",
                "必须调用 Thread.start()，通过 JNI start0() 请求 OS 创建新线程并入队就绪调度；严禁直接调 run()（仅普通方法同步调用）；严禁重复 start()（threadStatus!=0 抛出 IllegalThreadStateException）。");

        printAnswer(9, "如何停止一个线程的运行? / 如何停止一个线程?",
                "严禁使用废弃的 stop()/suspend()（破坏数据一致性或引发死锁）；必须采用【协作式机制】：利用 volatile 自定义退出标志位，或使用 Thread.interrupt() 配合 InterruptedException 两阶段安全终止。");

        printAnswer(10, "调用 interrupt 是如何让线程抛出异常的？",
                "interrupt() 本身只设置中断标志位为 true；当线程处于 sleep/wait/join 阻塞时，JVM 通过 ParkEvent::unpark 强行唤醒该线程，底层 native 检查到中断后，先清除标志位（置为 false），然后在 Java 栈帧构造并抛出 InterruptedException。");

        printAnswer(11, "Java线程的状态有哪些?",
                "Thread.State 定义的 6 大状态：NEW（新建）、RUNNABLE（运行中/就绪排队）、BLOCKED（等待 Monitor 锁）、WAITING（无限期主动等待）、TIMED_WAITING（超时等待）、TERMINATED（死亡）。");

        printAnswer(12, "sleep 和 wait的区别是什么?",
                "所属类：Thread 静态方法 vs Object 实例方法；释放锁：sleep 抱锁入睡不放锁，wait 立即释放持有锁；环境：wait 必须在 synchronized 内调用，否则抛 IllegalMonitorStateException；唤醒：sleep 自动醒，wait 需同一对象 notify。");

        printAnswer(13, "sleep会释放cpu吗?",
                "绝对会释放 CPU！让出 CPU 调度时间片给其他线程或让 CPU 节能空闲；混淆原因在于其【不释放锁】，但 CPU 调度权是完全让出的。");

        printAnswer(14, "blocked和waiting有啥区别?",
                "BLOCKED 是被动等待 synchronized Monitor 锁（位于 EntryList 队列）；WAITING 是主动调用 wait/join/park 释放 CPU 和锁等待条件通知（位于 WaitSet 队列）。ReentrantLock 争锁失败进入的其实是 WAITING 而非 BLOCKED。");

        printAnswer(15, "wait 状态下的线程如何进行恢复到 running 状态?",
                "四步演进：WAITING（在 WaitSet 挂起）-> 收到 notify 后移入 EntryList 变为 BLOCKED -> 争抢到 Monitor 锁后变为 RUNNABLE -> 获取 CPU 时间片后恢复 running。");

        printAnswer(16, "notify 和 notifyAll的区别?",
                "notify 只随机唤醒 WaitSet 中的一个线程（易发生信号丢失或生产者/消费者相互等待死锁）；notifyAll 唤醒 WaitSet 中的全部线程移入 EntryList 竞争锁，更安全可靠，官方推荐。");

        printAnswer(17, "notify 选择哪个线程?",
                "JVM 规范定义为【完全非确定性/任意选择 (arbitrary)】；HotSpot 底层由参数 Knob_Notify 控制（移至 EntryList 头/尾或 _cxq），在不同版本与平台可能呈现类似 LIFO 行为，但业务代码切忌依赖任何顺序假设。");

        printAnswer(18, "不同的线程之间如何通信? / 线程间通信方式有哪些?",
                "通信本质：同一进程内各线程天然共享堆内存与元空间，通信基于共享内存配合同步与唤醒原语。8 大方案：volatile、synchronized wait/notify、Lock Condition、BlockingQueue、CountDownLatch、CyclicBarrier、Thread.join、PipedStream。");

        printAnswer(19, "Go的协程和Java 的线程有啥区别？",
                "模型：Go 是 M:N 混合用户态调度（GMP 模型），Java 传统是 1:1 内核调度；内存：Go 协程初始 2KB 连续弹性栈，Java 线程固定 1MB；开销：Go 上下文切换几十纳秒（仅存几个寄存器），Java 需陷入内核态耗时 1~2 微秒；哲学：Go 优先 Channel 管道通信 (CSP)，Java 传统基于共享内存加锁。");

        System.out.println("==========================================================================================");
        System.out.println("模块归属提示：各专项详细深度代码与实操演示，可查看 day04 各子包对应类！");
        System.out.println("==========================================================================================");
    }

    private static void printAnswer(int id, String question, String answer) {
        System.out.printf("【题目 %02d】%s\n", id, question);
        System.out.println("   核心结论：" + answer);
        System.out.println();
    }
}
