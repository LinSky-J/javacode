package aqs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * AQS (AbstractQueuedSynchronizer) 架构全貌、CAS 与 AQS 关系及手写可重入公平锁
 *
 * 面试原题：
 * - 介绍一下AQS
 * - CAS 和 AQS 有什么关系?
 * - 如何用 AQS 实现一个可重入的公平锁?
 *
 * 核心考点：
 * 1. AQS 核心三要素：volatile int state、双向 CLH 变体同步队列 (Node)、模版方法模式
 * 2. CAS 与 AQS 的共生依赖关系（state 变更、队尾 enq 入队 CAS、waitStatus 修改）
 * 3. 手写实现基于 AQS 的 CustomReentrantFairLock（实现可重入与 hasQueuedPredecessors 公平性判定）
 * 4. 多线程实测验证自定义锁的互斥性、嵌套可重入与公平排队机制
 */
public class AqsArchitectureAndCustomFairLock {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】AQS (AbstractQueuedSynchronizer) 核心架构全貌剖析");
        System.out.println("================================================================================");
        explainAqsArchitecture();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【面试核心答辩】CAS 和 AQS 的深层关系是什么？");
        System.out.println("--------------------------------------------------------------------------------");
        explainCasAndAqsRelationship();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战手写 AQS 锁】基于 AQS 实现可重入公平锁并在多线程下实测验证");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateCustomReentrantFairLock();
    }

    /**
     * AQS 核心架构系统阐述
     */
    public static void explainAqsArchitecture() {
        System.out.println("1. 什么是 AQS？");
        System.out.println("   - AQS 全称 AbstractQueuedSynchronizer（抽象队列同步器），是 J.U.C 并发包的核心基石；");
        System.out.println("   - ReentrantLock、Semaphore、CountDownLatch、ReentrantReadWriteLock 等核心组件均基于 AQS 构建。");
        System.out.println("2. AQS 核心架构三大要素：");
        System.out.println("   (1) volatile int state（同步状态）：");
        System.out.println("       * 基于 volatile 保证内存可见性，通过 getState()、setState()、compareAndSetState() 访问与修改；");
        System.out.println("       * 在 ReentrantLock 中表示锁持有计数与重入次数；在 Semaphore 中表示可用信号量许可数；在 CountDownLatch 中表示倒计数。");
        System.out.println("   (2) CLH 变体双向同步队列 (FIFO)：");
        System.out.println("       * 未竞争到锁的线程被封装成 Node 节点，通过双向链表（head、tail 指针）维护；");
        System.out.println("       * 线程在获取锁失败后挂起（LockSupport.park），持锁线程释放锁后唤醒后继节点（LockSupport.unpark）。");
        System.out.println("   (3) 模版方法设计模式 (Template Method Pattern)：");
        System.out.println("       * AQS 将排队、入队、挂起、唤醒等极其复杂的并发控制封装为通用模版；");
        System.out.println("       * 具体的同步组件只需继承 AQS 并重写特定的 5 个钩子方法：");
        System.out.println("         tryAcquire (独占加锁)、tryRelease (独占释放)、tryAcquireShared (共享获取)、tryReleaseShared (共享释放)、isHeldExclusively。");
    }

    /**
     * CAS 与 AQS 关系解析
     */
    public static void explainCasAndAqsRelationship() {
        System.out.println("大厂高频追问：'CAS 和 AQS 到底是什么关系？'");
        System.out.println("核心解答：");
        System.out.println("1. CAS 是 AQS 的底层原子基石：");
        System.out.println("   - 同步状态变更：AQS 在抢锁与释放锁时，修改 state 必须依赖 compareAndSetState(expect, update)，即底层 CAS 指令；");
        System.out.println("   - 节点并发入队：当多线程争用失败同时涌入同步队列时，AQS 在 enq(Node) 方法中通过 for(;;) 自旋 + compareAndSetTail 确保队尾节点原子设置；");
        System.out.println("   - 节点状态流转：修改节点 waitStatus（如前驱标记为 SIGNAL）同样依赖 CAS 原语。");
        System.out.println("2. AQS 是 CAS 缺陷的终极工程化解决方案：");
        System.out.println("   - 单纯的 CAS 只能进行死循环自旋，当争用激烈时会导致 CPU 占用 100%；");
        System.out.println("   - AQS 巧妙结合了 CAS 与操作系统线程挂起机制：尝试 CAS 失败后，将线程封装进 CLH 队列并使用 LockSupport.park 挂起，");
        System.out.println("     从根本上解决了 CAS 盲目空转消耗 CPU 的致命痛点！");
    }

    /**
     * 3. 手写基于 AQS 的可重入公平锁
     */
    public static class CustomReentrantFairLock implements Lock {

        // 内部静态同步器，继承 AQS
        private static class Sync extends AbstractQueuedSynchronizer {

            // 重写独占式获取锁（公平逻辑）
            @Override
            protected boolean tryAcquire(int acquires) {
                final Thread current = Thread.currentThread();
                int c = getState();

                if (c == 0) {
                    // 【核心公平逻辑】：调用 hasQueuedPredecessors() 判断队列中是否有排在自己前面的线程
                    // 只有当没有前驱排队节点时，才允许 CAS 争抢锁！
                    if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
                        setExclusiveOwnerThread(current); // 标记锁持有者为当前线程
                        return true;
                    }
                } else if (current == getExclusiveOwnerThread()) {
                    // 【可重入逻辑】：锁已被占用，但持有者正是当前线程，允许重入！
                    int nextc = c + acquires;
                    if (nextc < 0) {
                        throw new Error("Maximum lock count exceeded");
                    }
                    setState(nextc); // 单线程重入无需 CAS
                    return true;
                }
                return false;
            }

            // 重写独占式释放锁
            @Override
            protected boolean tryRelease(int releases) {
                if (Thread.currentThread() != getExclusiveOwnerThread()) {
                    throw new IllegalMonitorStateException("当前线程并非锁持有者，无法释放！");
                }
                int c = getState() - releases;
                boolean free = false;
                if (c == 0) {
                    free = true;
                    setExclusiveOwnerThread(null); // 完全释放，清空持有者
                }
                setState(c); // 只有完全释放返回 true，AQS 才会唤醒后继等待节点
                return free;
            }

            @Override
            protected boolean isHeldExclusively() {
                return getExclusiveOwnerThread() == Thread.currentThread();
            }

            Condition newCondition() {
                return new ConditionObject();
            }
        }

        private final Sync sync = new Sync();

        @Override
        public void lock() {
            sync.acquire(1); // 调用 AQS 模版方法
        }

        @Override
        public void unlock() {
            sync.release(1); // 调用 AQS 模版方法
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        @Override
        public boolean tryLock() {
            return sync.tryAcquire(1);
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(time));
        }

        @Override
        public Condition newCondition() {
            return sync.newCondition();
        }
    }

    /**
     * 实操验证自定义可重入公平锁
     */
    private static void demonstrateCustomReentrantFairLock() throws InterruptedException {
        CustomReentrantFairLock fairLock = new CustomReentrantFairLock();

        // 1. 验证可重入性
        System.out.println("步骤 A：验证主线程嵌套获取锁 (可重入机制):");
        fairLock.lock();
        try {
            System.out.println("   [MainThread] 第 1 次获取锁成功！");
            fairLock.lock();
            try {
                System.out.println("   [MainThread] 第 2 次重入锁成功！未被自身阻塞！");
            } finally {
                fairLock.unlock();
                System.out.println("   [MainThread] 第 2 次锁释放完毕！");
            }
        } finally {
            fairLock.unlock();
            System.out.println("   [MainThread] 第 1 次锁释放完毕，锁完全空闲！");
        }

        // 2. 验证多线程公平排队互斥
        System.out.println("\n步骤 B：启动 3 个子线程，验证互斥保护与公平排队:");
        for (int i = 1; i <= 3; i++) {
            final int id = i;
            new Thread(() -> {
                fairLock.lock();
                try {
                    System.out.printf("   [Worker-%d] 按照排队顺序成功持有锁！执行业务 50ms...\n", id);
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                } finally {
                    fairLock.unlock();
                    System.out.printf("   [Worker-%d] 释放锁完毕\n", id);
                }
            }, "Worker-" + id).start();
            Thread.sleep(10); // 确保严格顺序进入 AQS 等待队列
        }

        Thread.sleep(300);
        System.out.println("【结论】：手写 CustomReentrantFairLock 成功复现了可重入与 AQS 公平队列排队调度！");
    }
}
