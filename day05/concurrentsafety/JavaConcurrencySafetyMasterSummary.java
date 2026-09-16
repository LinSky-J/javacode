package concurrentsafety;

/**
 * 并发安全、锁体系与 JUC 核心原理全景总览与大厂面试答辩金句汇编
 *
 * 覆盖本轮提出的全部 32 道核心并发深度考题：
 * 01. juc包下你常用的类?
 * 02. 怎么保证多线程安全?
 * 03. Java中有哪些常用的锁，在什么场景下使用？
 * 04. 怎么在实践中用锁的？
 * 05. Java并发工具你知道哪些?
 * 06. CountDownLatch 是做什么的讲一讲？
 * 07. synchronized和reentrantlock及其应用场景?
 * 08. 除了用synchronized，还有什么方法可以实现线程同步?
 * 09. synchronized锁静态方法和普通方法区别?
 * 10. synchronized和reentrantlock区别？
 * 11. 怎么理解可重入锁?
 * 12. synchronized 支持重入吗？如何实现的?
 * 13. syncronized锁升级的过程讲一下
 * 14. JVM对Synchornized的优化?
 * 15. 介绍一下AQS
 * 16. CAS 和 AQS 有什么关系?
 * 17. 如何用 AQS 实现一个可重入的公平锁?
 * 18. Threadlocal作用，原理，具体里面存的key value是啥，会有什么问题，如何解决?
 * 19. 悲观锁和乐观锁的区别?
 * 20. Java中想实现一个乐观锁，都有哪些方式?
 * 21. CAS 有什么缺点?
 * 22. 为什么不能所有的锁都用CAS?
 * 23. CAS 有什么问题，Java是怎么解决的?
 * 24. voliatle关键字有什么作用?
 * 25. 指令重排序的原理是什么?
 * 26. volatile可以保证线程安全吗?
 * 27. volatile和sychronized比较?
 * 28. 什么是公平锁和非公平锁?
 * 29. 非公平锁吞吐量为什么比公平锁大？
 * 30. Synchronized是公平锁吗?
 * 31. ReentrantLock是怎么实现公平锁的？
 * 32. 什么情况会产生死锁问题？如何解决?
 */
public class JavaConcurrencySafetyMasterSummary {

    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("          Java 并发安全、锁机制与 JUC 深度复盘全景图谱 (day05 综合答辩讲稿)               ");
        System.out.println("==========================================================================================");

        printAnswer(1, "juc包下你常用的类?",
                "原子类(AtomicInteger/LongAdder)、显式锁(ReentrantLock/ReadWriteLock/StampedLock)、并发容器(ConcurrentHashMap/CopyOnWriteArrayList)、阻塞队列(ArrayBlockingQueue)、协作器(CountDownLatch/Semaphore)、线程池(ThreadPoolExecutor/CompletableFuture)。");

        printAnswer(2, "怎么保证多线程安全?",
                "三大核心方案：1.互斥排他(synchronized/ReentrantLock临界区保护)；2.无锁CAS(Atomic原子类/乐观并发)；3.避免共享与数据隔离(final不可变对象、ThreadLocal线程私有副本)。");

        printAnswer(3, "Java中有哪些常用的锁，在什么场景下使用？",
                "synchronized(简单业务互斥)、ReentrantLock(需超时/中断/公平性复杂控制)、ReentrantReadWriteLock(读多写极少本地缓存)、StampedLock(超高频读、支持乐观读)、Atomic(纯数值计数)。");

        printAnswer(4, "怎么在实践中用锁的？",
                "5大黄金原则：1.减小锁粒度(仅锁核心计算，严禁锁内RPC/IO)；2.读写分离提升读吞吐；3.分段槽化分散单点竞争(LongAdder)；4.优先用tryLock(timeout)防死锁；5.严禁锁常量池对象(String/Integer缓存)。");

        printAnswer(5, "Java并发工具你知道哪些?",
                "CountDownLatch(一等多/多等一)、CyclicBarrier(多线程循环步调一致)、Semaphore(限流与许可证控制)、Exchanger(双线程数据对等交换)、Phaser(多阶段任务分治同步)。");

        printAnswer(6, "CountDownLatch 是做什么的讲一讲？",
                "基于 AQS 共享锁实现，通过构造函数初始化 state 计数值。子线程执行 countDown() 用 CAS 将 state 减 1，减至 0 时唤醒 await() 阻塞的线程。用于汇聚等待或发令枪并发，是一次性不可复用的。");

        printAnswer(7, "synchronized和reentrantlock及其应用场景?",
                "synchronized 是 JVM 原生关键字，语法清爽自动释放，适合常规简单同步；ReentrantLock 是 API 显式锁，提供公平锁、可中断加锁、tryLock 超时等高级功能，适合高争用与复杂协同控制。");

        printAnswer(8, "除了用synchronized，还有什么方法可以实现线程同步?",
                "1.ReentrantLock/Condition；2.CAS 原子类；3.Semaphore 信号量；4.CountDownLatch/CyclicBarrier 协作器；5.BlockingQueue 阻塞队列；6.Thread.join；7.ThreadLocal 数据隔离。");

        printAnswer(9, "synchronized锁静态方法和普通方法区别?",
                "静态方法锁的是 Class 对象(Target.class)，全 JVM 该类所有实例共享一把锁，彼此互斥；普通方法锁的是 this 实例对象，仅同一个对象实例的同步方法互斥，不同实例互不阻塞。");

        printAnswer(10, "synchronized和reentrantlock区别？",
                "层次：JVM 指令 vs JUC API；释放：自动释放 vs 必须 finally 手动 unlock；灵活性：不支持中断/超时 vs 支持中断/tryLock超时；公平性：仅非公平 vs 支持公平与非公平；条件队列：单一 wait/notify vs 多个 Condition。");

        printAnswer(11, "怎么理解可重入锁?",
                "同一线程如果已经获得了某把锁，可以再次进入该锁保护的其他同步代码块而不会被自己阻塞。核心意义在于防止同一线程在方法递归或继承调用时出现【自身死锁】。");

        printAnswer(12, "synchronized 支持重入吗？如何实现的?",
                "支持！HotSpot 底层 ObjectMonitor 维护了 _owner 指针和 _recursions 计数器。线程获取锁时若 _owner 是自己，直接将 _recursions++；退出时 _recursions--，减至 0 时才真正释放锁。");

        printAnswer(13, "syncronized锁升级的过程讲一下?",
                "依据对象头 Mark Word 状态单向升级：无锁(001) -> 偏向锁(101，记录线程ID零CAS重入) -> 轻量级锁(000，线程栈建 Lock Record，CAS竞争自旋) -> 重量级锁(010，自旋超限膨胀，指针指向 ObjectMonitor，线程挂起进入内核态)。");

        printAnswer(14, "JVM对Synchornized的优化?",
                "1.自适应自旋(根据历史动态决定自旋周期)；2.锁消除(基于 JIT 逃逸分析消除不逃逸对象的锁)；3.锁粗化(将循环内多次加锁扩大到整个循环体外部)；4.偏向锁与轻量级锁分级膨胀。");

        printAnswer(15, "介绍一下AQS?",
                "AbstractQueuedSynchronizer 是 JUC 锁体系的核心基石。三要素：1.volatile int state 同步状态；2.双向 CLH 变体同步队列(Node 封装线程挂起唤醒)；3.模版方法模式(暴露 tryAcquire/tryRelease 等 5 个钩子)。");

        printAnswer(16, "CAS 和 AQS 有什么关系?",
                "CAS 是 AQS 的底层原子基石(AQS 状态 state 变更、enq 尾节点无锁并发入队均基于 CAS)；AQS 则是 CAS 的工程化调度框架(解决了 CAS 盲目自旋打满 CPU 的痛点，结合 LockSupport 挂起线程)。");

        printAnswer(17, "如何用 AQS 实现一个可重入的公平锁?",
                "继承 AQS 重写 tryAcquire：state==0 时必须先调用 hasQueuedPredecessors()，队列有前驱直接放弃抢锁排队，无前驱才 CAS 获取并 setExclusiveOwnerThread；若持有者是当前线程则 state+=acquires 允许重入。");

        printAnswer(18, "Threadlocal作用，原理，具体里面存的key value是啥，会有什么问题，如何解决?",
                "作用：线程隔离与全链路传递。原理：Thread 持有 ThreadLocalMap。Entry 存储：Key 是 WeakReference<ThreadLocal>，Value 是强引用对象。问题：线程池下 Key 被 GC 变 null，但 Value 强引用无法释放造成内存泄露与脏数据。解决：try-finally 必须手动 remove()！");

        printAnswer(19, "悲观锁和乐观锁的区别?",
                "悲观锁认为冲突必然发生，每次读写均加排他锁阻止并发；乐观锁认为冲突极少发生，读不加锁，写时比对版本号或 CAS 校验，失败则重试。");

        printAnswer(20, "Java中想实现一个乐观锁，都有哪些方式?",
                "1.JUC Atomic 原子类(CAS)；2.数据实体添加 version 版本号字段；3.StampedLock 乐观读(tryOptimisticRead + validate)；4.数据库隐式 CAS 条件更新(WHERE id=? AND version=?)。");

        printAnswer(21, "CAS 有什么缺点?",
                "1.ABA 问题；2.高并发激烈竞争时长周期自旋消耗 CPU 100% 引发总线风暴；3.只能保证单一共享变量的原子操作。");

        printAnswer(22, "为什么不能所有的锁都用CAS?",
                "自旋假定临界区极短。当临界区耗时较长(复杂计算、IO/RPC)或并发争用极大时，多线程自旋对 CPU 的浪费远超内核态上下文切换挂起线程的微秒级开销，会导致系统直接瘫痪。");

        printAnswer(23, "CAS 有什么问题，Java是怎么解决的?",
                "ABA 问题 -> AtomicStampedReference 引入版本戳；长时间自旋 CPU 损耗 -> AQS 挂起机制或 LongAdder 分段累加槽；单变量限制 -> AtomicReference 封装复合不可变对象。");

        printAnswer(24, "voliatle关键字有什么作用?",
                "1.保证内存可见性(Lock 前缀指令强制刷新主存并触发 MESI 缓存失效)；2.禁止指令重排序(JMM 插入内存屏障)。");

        printAnswer(25, "指令重排序的原理是什么?",
                "编译器优化重排、处理器指令级并行流水线乱序重排、内存系统写缓冲 Store Buffer 重排。其目标是压榨 CPU 流水线性能，前提是遵循 as-if-serial 语义保证单线程执行结果不变。");

        printAnswer(26, "volatile可以保证线程安全吗?",
                "不能！线程安全需原子性、可见性、有序性三者齐备。volatile 仅保可见性与有序性，不保证复合操作(如 count++)的原子性，依然会发生更新丢失。");

        printAnswer(27, "volatile和sychronized比较?",
                "volatile 是轻量级变量修饰符，不阻塞线程，无原子性保障；synchronized 是重量级互斥锁，会阻塞挂起竞争线程，提供原子性、可见性与有序性完整保障。");

        printAnswer(28, "什么是公平锁和非公平锁?",
                "公平锁严格按照线程申请锁的绝对 FIFO 时间顺序排队；非公平锁允许新到达的线程插队先尝试抢一次锁，抢不到才排队。");

        printAnswer(29, "非公平锁吞吐量为什么比公平锁大？",
                "避免了线程从内核态唤醒挂起的微秒级延迟与调度开销。持锁线程释放时，正好处在运行态的新线程'搭便车'直接拿锁执行，成倍减少了上下文切换次数与 CPU 空闲等待。");

        printAnswer(30, "Synchronized是公平锁吗?",
                "不是！synchronized 天生就是非公平锁，新线程抢锁首先自旋争抢，失败才挂起进 EntryList。");

        printAnswer(31, "ReentrantLock是怎么实现公平锁的？",
                "FairSync 在 tryAcquire 中先调用 hasQueuedPredecessors() 检查 AQS 队列是否存在前驱等待节点，若有其他线程排在前面，则放弃插队，乖乖进入队尾。");

        printAnswer(32, "什么情况会产生死锁问题？如何解决?",
                "成因：同时满足互斥、请求与保持、不可剥夺、循环等待 4 大必要条件。解决：1.全局严格按资源 ID 升序加锁(破除循环等待)；2.tryLock(timeout) 超时放弃并随机退避重试(破除不可剥夺)；生产用 jstack / Arthas thread -b 诊断。");

        System.out.println("==========================================================================================");
        System.out.println("所有题目专项源码与可运行实操 Demo，均已在 day05 各子包中逐一实现并提交远程仓库！");
        System.out.println("==========================================================================================");
    }

    private static void printAnswer(int id, String question, String answer) {
        System.out.printf("【题目 %02d】%s\n", id, question);
        System.out.println("   核心金句：" + answer);
        System.out.println();
    }
}
