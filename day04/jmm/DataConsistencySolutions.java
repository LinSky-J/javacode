package jmm;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 保证多线程数据一致性的全套技术方案与实战对比
 *
 * 面试原题：
 * - 保证数据的一致性有哪些方案呢？
 *
 * 方案全景：
 * 1. 互斥锁机制（悲观锁）：synchronized 关键字、ReentrantLock 显式锁
 * 2. 乐观并发与无锁机制（CAS 原语）：Atomic 原子类、LongAdder 分段累加器
 * 3. 读写分离锁：ReentrantReadWriteLock、StampedLock 乐观读
 * 4. 内存可见性与防重排：volatile 关键字
 * 5. 线程数据隔离（避免共享）：ThreadLocal、不可变对象模式 (Immutable Pattern)
 * 6. 并发容器：ConcurrentHashMap、CopyOnWriteArrayList、BlockingQueue
 * 7. 分布式系统一致性：分布式锁 (Redis Redisson / Zookeeper)、分布式事务 (2PC / TCC / MQ 最终一致性)
 */
public class DataConsistencySolutions {

    private static final int THREAD_COUNT = 20;
    private static final int OPERATIONS_PER_THREAD = 100_000;

    // 1. synchronized 方案
    private static int syncCounter = 0;
    private static final Object SYNC_LOCK = new Object();

    // 2. ReentrantLock 方案
    private static int lockCounter = 0;
    private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();

    // 3. CAS 原子类方案
    private static final AtomicInteger ATOMIC_COUNTER = new AtomicInteger(0);

    // 4. 分段累加器方案
    private static final LongAdder LONG_ADDER = new LongAdder();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】保证并发数据一致性的完整技术体系");
        System.out.println("================================================================================");
        explainConsistencySolutions();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【基准测试与一致性验证】多线程并发自增数据一致性与耗时对比");
        System.out.println("--------------------------------------------------------------------------------");
        benchmarkConsistencyMechanisms();
    }

    /**
     * 系统阐述保证数据一致性的方案
     */
    public static void explainConsistencySolutions() {
        System.out.println("在 Java 并发与分布式体系中，保证数据一致性从单机到分布式主要有以下七大层级方案：");
        System.out.println("1. 悲观排他互斥锁（Pessimistic Locking）：");
        System.out.println("   - synchronized：JVM 原生内置锁，JDK 6 后历经偏向锁、轻量级锁、自旋锁到重量级锁的自适应膨胀，");
        System.out.println("     支持自动加锁与异常释放，结合 JMM 保证原子性、可见性、有序性。");
        System.out.println("   - ReentrantLock：基于 AQS（AbstractQueuedSynchronizer）的 API 层显式锁，支持公平锁/非公平锁、");
        System.out.println("     尝试非阻塞获取锁 tryLock()、可中断获取锁 lockInterruptibly()、超时等待与多个 Condition 条件队列。");
        System.out.println("2. 乐观并发与无锁机制（Optimistic Locking / Lock-Free CAS）：");
        System.out.println("   - 基于硬件 CPU 级别的原子比较并交换指令（cmpxchg），无线程挂起与上下文切换开销。");
        System.out.println("   - AtomicInteger / AtomicReference：适合低争用场景；需注意 ABA 问题（可用 AtomicStampedReference 解决）。");
        System.out.println("   - LongAdder / Striped64：高并发写激烈争用下的首选，内部采用 Cell[] 分段累加槽思想，分散锁竞争，吞吐量极高。");
        System.out.println("3. 读写分离锁（Read-Write Locks）：");
        System.out.println("   - ReentrantReadWriteLock：读共享、写独占，适合读多写极少（如配置项读取、本地缓存）场景。");
        System.out.println("   - StampedLock（Java 8+）：引入乐观读模式（Optimistic Read），读操作不阻塞写，写操作不阻塞乐观读。");
        System.out.println("4. 内存可见性与防重排标记：");
        System.out.println("   - volatile：修饰单一状态标记、状态机转换或单例模式中的 DCL 双重检查锁（防止指令重排序造成空指针）。");
        System.out.println("5. 数据私有化与不可变模式（消除共享源头）：");
        System.out.println("   - ThreadLocal：每个线程拥有独立的变量副本，彻底消除多线程数据竞争（如 SimpleDateFormat、DB Connection）。");
        System.out.println("   - 不可变模式 (Immutable Pattern)：变量均用 final 修饰，初始化后只读不可修改（如 String、Record）。");
        System.out.println("6. 工业级并发容器：");
        System.out.println("   - ConcurrentHashMap（分段 Node 锁 + CAS）、CopyOnWriteArrayList（写时复制）、ArrayBlockingQueue 等。");
        System.out.println("7. 分布式环境一致性方案（跨多 JVM 实例）：");
        System.out.println("   - 分布式互斥锁：基于 Redis（Redisson RedLock）或 ZooKeeper 临时顺序节点，控制跨节点资源排他访问。");
        System.out.println("   - 分布式事务：强一致性两阶段提交 (2PC/XA)、柔性最终一致性 TCC、Saga 事务模式、基于可靠消息队列的最终一致性。");
    }

    /**
     * 运行基准测试验证各方案数据一致性
     */
    public static void benchmarkConsistencyMechanisms() throws InterruptedException {
        int totalExpected = THREAD_COUNT * OPERATIONS_PER_THREAD;
        System.out.printf("测试并发规模：%d 个并发线程，每个线程累加 %d 次，预期最终结果 = %d\n\n",
                THREAD_COUNT, OPERATIONS_PER_THREAD, totalExpected);

        // 1. synchronized 测试
        long startSync = System.currentTimeMillis();
        CountDownLatch latchSync = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    synchronized (SYNC_LOCK) {
                        syncCounter++;
                    }
                }
                latchSync.countDown();
            }).start();
        }
        latchSync.await();
        long costSync = System.currentTimeMillis() - startSync;
        System.out.printf("[synchronized 方案] 结果 = %d | 数据一致性: %s | 耗时: %d ms\n",
                syncCounter, (syncCounter == totalExpected ? "PASSED" : "FAILED"), costSync);

        // 2. ReentrantLock 测试
        long startLock = System.currentTimeMillis();
        CountDownLatch latchLock = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    REENTRANT_LOCK.lock();
                    try {
                        lockCounter++;
                    } finally {
                        REENTRANT_LOCK.unlock();
                    }
                }
                latchLock.countDown();
            }).start();
        }
        latchLock.await();
        long costLock = System.currentTimeMillis() - startLock;
        System.out.printf("[ReentrantLock 方案] 结果 = %d | 数据一致性: %s | 耗时: %d ms\n",
                lockCounter, (lockCounter == totalExpected ? "PASSED" : "FAILED"), costLock);

        // 3. AtomicInteger (CAS) 测试
        long startAtomic = System.currentTimeMillis();
        CountDownLatch latchAtomic = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    ATOMIC_COUNTER.incrementAndGet();
                }
                latchAtomic.countDown();
            }).start();
        }
        latchAtomic.await();
        long costAtomic = System.currentTimeMillis() - startAtomic;
        System.out.printf("[AtomicInteger CAS 方案] 结果 = %d | 数据一致性: %s | 耗时: %d ms\n",
                ATOMIC_COUNTER.get(), (ATOMIC_COUNTER.get() == totalExpected ? "PASSED" : "FAILED"), costAtomic);

        // 4. LongAdder (分段累加) 测试
        long startAdder = System.currentTimeMillis();
        CountDownLatch latchAdder = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    LONG_ADDER.increment();
                }
                latchAdder.countDown();
            }).start();
        }
        latchAdder.await();
        long costAdder = System.currentTimeMillis() - startAdder;
        System.out.printf("[LongAdder 分段累加方案] 结果 = %d | 数据一致性: %s | 耗时: %d ms\n",
                LONG_ADDER.sum(), (LONG_ADDER.sum() == totalExpected ? "PASSED" : "FAILED"), costAdder);

        System.out.println("\n【架构选型结论】：");
        System.out.println("   - 计数器场景：高争用首选 LongAdder（分段槽思想消除总线锁风暴）；低争用选 AtomicInteger。");
        System.out.println("   - 业务临界区：简单临界区选 synchronized（代码清爽、JIT 逃逸分析与锁粗化锁消除优化）；");
        System.out.println("                 复杂同步控制（超时、尝试、可中断、多条件变量）选 ReentrantLock。");
    }
}
