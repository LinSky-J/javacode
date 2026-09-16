package communication;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Java 线程间通信 (Inter-Thread Communication) 全套方案与实战演示
 *
 * 面试原题：
 * - 不同的线程之间如何通信?
 * - 线程间通信方式有哪些?
 *
 * 核心考点：
 * 1. 线程通信本质：基于共享内存架构（Shared Memory）与同步协作协调
 * 2. 线程通信的 8 大经典方案体系：
 *    - 方案 1：volatile 内存可见性状态共享
 *    - 方案 2：内置监视器等待唤醒机制 (synchronized + wait / notify)
 *    - 方案 3：JUC 显式锁与多 Condition 精准条件唤醒 (ReentrantLock + Condition)
 *    - 方案 4：线程安全阻塞队列 (BlockingQueue 生产者-消费者模式)
 *    - 方案 5：倒计数闭锁 (CountDownLatch 任务汇聚等待)
 *    - 方案 6：循环栅栏 (CyclicBarrier 多线程步调一致)
 *    - 方案 7：线程生命周期归并 (Thread.join)
 *    - 方案 8：内存管道流传输 (PipedInputStream / PipedOutputStream)
 * 3. 各方案性能、灵活性与工业级生产选型推荐
 */
public class InterThreadCommunicationPatterns {

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】Java 线程间通信的本质与 8 大通信方式全景");
        System.out.println("================================================================================");
        explainCommunicationPrinciples();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战演示 1】ReentrantLock + Condition 精准双向唤醒通信");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateLockConditionPattern();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战演示 2】BlockingQueue 高内聚解耦通信");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateBlockingQueuePattern();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战演示 3】CountDownLatch 协同汇聚通信");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateCountDownLatchPattern();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战演示 4】PipedStream 管道数据流跨线程传输");
        System.out.println("--------------------------------------------------------------------------------");
        demonstratePipedStreamPattern();
    }

    /**
     * 理论核心剖析
     */
    public static void explainCommunicationPrinciples() {
        System.out.println("1. 不同的线程之间如何通信？通信的底层本质是什么？");
        System.out.println("   - 【通信本质】：操作系统的进程之间地址空间完全独立，必须依靠操作系统内核提供的 IPC 机制");
        System.out.println("     （如管道、套接字 Socket、共享内存段）；而 Java 同一进程内的所有线程【天然共享堆内存与方法区】。");
        System.out.println("   - 因此，Java 线程间通信的本质就是【基于共享内存的读写】，配合 JVM/操作系统提供的同步与等待唤醒原语");
        System.out.println("     （JMM 内存屏障、LockSupport、Futex），实现安全有序的信息交互与状态协同。");
        System.out.println();
        System.out.println("2. Java 线程间通信 8 大主流方式总结：");
        System.out.println("   (1) volatile 标志位：利用 JMM 可见性，由一个线程写、多个线程读，实现轻量级状态广播（如停止指令）。");
        System.out.println("   (2) synchronized + wait() / notify()：经典基于 ObjectMonitor 的等待唤醒机制。");
        System.out.println("   (3) ReentrantLock + Condition：支持多个条件队列（如 notEmpty 与 notFull），实现精准定向单播唤醒。");
        System.out.println("   (4) BlockingQueue（阻塞队列）：工业级生产者-消费者首选，解耦任务生产与消费，内置自动阻塞唤醒。");
        System.out.println("   (5) CountDownLatch（闭锁）：允许一个或多个线程等待其他一组线程完成各自任务（一等多）。");
        System.out.println("   (6) CyclicBarrier（循环栅栏）：让一组线程到达一个屏障点被阻塞，直到最后一个线程到达，屏障才开门（多等齐）。");
        System.out.println("   (7) Thread.join()：主线程或父线程等待子线程执行结束，底层基于 wait() 阻塞主线程直至子线程死亡。");
        System.out.println("   (8) PipedInputStream / PipedOutputStream：在两个线程之间建立管道，以 I/O 流形式直接写入与读取字节数据。");
        System.out.println("   (补充) Exchanger：专用于两个线程之间以双向配对的方式对等交换数据；CompletableFuture 异步回调链式通信。");
    }

    /**
     * 演示 1：ReentrantLock + Condition 精准条件唤醒
     */
    static class ConditionAlternatingPrinter {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition conditionA = lock.newCondition();
        private final Condition conditionB = lock.newCondition();
        private boolean isTurnA = true;

        public void printA() {
            lock.lock();
            try {
                while (!isTurnA) {
                    conditionA.await();
                }
                System.out.print("[A]");
                isTurnA = false;
                conditionB.signal(); // 精准只唤醒等待中的 B 线程！
            } catch (InterruptedException ignored) {
            } finally {
                lock.unlock();
            }
        }

        public void printB() {
            lock.lock();
            try {
                while (isTurnA) {
                    conditionB.await();
                }
                System.out.print("[B] ");
                isTurnA = true;
                conditionA.signal(); // 精准只唤醒等待中的 A 线程！
            } catch (InterruptedException ignored) {
            } finally {
                lock.unlock();
            }
        }
    }

    private static void demonstrateLockConditionPattern() throws InterruptedException {
        ConditionAlternatingPrinter printer = new ConditionAlternatingPrinter();
        System.out.print("   两个线程交替精准通信打印 A 与 B: ");
        Thread tA = new Thread(() -> {
            for (int i = 0; i < 5; i++) printer.printA();
        });
        Thread tB = new Thread(() -> {
            for (int i = 0; i < 5; i++) printer.printB();
        });
        tA.start();
        tB.start();
        tA.join();
        tB.join();
        System.out.println("\n   -> 特性：Condition 相比 Object.wait/notify 支持精准唤醒目标角色，避免 notifyAll 的惊群开销。");
    }

    /**
     * 演示 2：BlockingQueue 阻塞队列通信
     */
    private static void demonstrateBlockingQueuePattern() throws InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);

        Thread producer = new Thread(() -> {
            try {
                String[] msgs = {"Order-101", "Order-102", "Order-103"};
                for (String msg : msgs) {
                    queue.put(msg); // 队列满自动阻塞
                    System.out.println("   [Producer] 成功投递消息至队列: " + msg);
                    Thread.sleep(30);
                }
            } catch (InterruptedException ignored) {}
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    String msg = queue.take(); // 队列空自动阻塞
                    System.out.println("   [Consumer] 成功从队列消费到消息: " + msg);
                }
            } catch (InterruptedException ignored) {}
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        System.out.println("   -> 评价：最符合现代架构工程实践的线程间解耦通信方式。");
    }

    /**
     * 演示 3：CountDownLatch 汇聚通信
     */
    private static void demonstrateCountDownLatchPattern() throws InterruptedException {
        int taskCount = 3;
        CountDownLatch latch = new CountDownLatch(taskCount);

        for (int i = 1; i <= taskCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    Thread.sleep(50 * id);
                    System.out.printf("   [Worker-%d] 子任务数据准备就绪，发出完成信号\n", id);
                } catch (InterruptedException ignored) {
                } finally {
                    latch.countDown(); // 减少计数
                }
            }).start();
        }

        System.out.println("   [MainThread] 等待所有子线程上报就绪信号...");
        latch.await();
        System.out.println("   [MainThread] 成功收到所有子任务完成通信，开始执行聚合计算！");
    }

    /**
     * 演示 4：PipedStream 内存管道字节流通信
     */
    private static void demonstratePipedStreamPattern() throws Exception {
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream); // 将管道输入输出流绑定连接

        Thread writer = new Thread(() -> {
            try {
                String payload = "Hello from WriterThread via Pipe!";
                outputStream.write(payload.getBytes());
                outputStream.close();
            } catch (Exception ignored) {}
        }, "Pipe-Writer");

        Thread reader = new Thread(() -> {
            try {
                byte[] buffer = new byte[128];
                int len = inputStream.read(buffer); // 阻塞读取
                String received = new String(buffer, 0, len);
                System.out.println("   [Pipe-Reader] 成功从管道流中接收到跨线程报文: " + received);
                inputStream.close();
            } catch (Exception ignored) {}
        }, "Pipe-Reader");

        writer.start();
        reader.start();
        writer.join();
        reader.join();
    }
}
