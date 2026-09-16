package interrupt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 如何停止一个线程的运行？优雅终止最佳实践与禁忌
 *
 * 面试原题：
 * - 如何停止一个线程的运行?
 * - 如何停止一个线程?
 *
 * 核心考点：
 * 1. 为什么 Thread.stop() / suspend() / resume() 被废弃？（锁强行释放致数据损坏、不释放锁死锁）
 * 2. 停止线程的核心设计思想：协作式机制 (Cooperative)，而非抢占式强杀
 * 3. 两种主流停止方案：
 *    - 方案 A：volatile 自定义退出标志位（适合非阻塞轮询任务）
 *    - 方案 B：Thread 中断响应机制 interrupt()（适合兼顾阻塞与计算任务的标准范式）
 * 4. 两阶段终止模式 (Two-Phase Termination Pattern)
 * 5. 线程池的优雅停机三部曲 (shutdown -> awaitTermination -> shutdownNow)
 */
public class ThreadStopAndGracefulShutdown {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】如何停止一个线程的运行？");
        System.out.println("================================================================================");
        explainStopMechanismsAndPitfalls();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战范式 1】基于 volatile 标志位的优雅停止（非阻塞场景）");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateVolatileFlagStop();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战范式 2】基于 interrupt() 中断机制的标准停止（兼顾阻塞与运行）");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateInterruptGracefulStop();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【生产级范式 3】线程池优雅关闭三部曲 (Graceful Shutdown)");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateThreadPoolGracefulShutdown();
    }

    /**
     * 理论核心剖析
     */
    public static void explainStopMechanismsAndPitfalls() {
        System.out.println("1. 为什么不能使用 Thread.stop() 强行停止？");
        System.out.println("   - stop() 方法被官方明确标记为 @Deprecated（废弃）；");
        System.out.println("   - 危害：无论线程执行到哪一行指令，stop() 都会直接强杀线程，并立刻强行释放该线程持有的所有 Monitor 锁！");
        System.out.println("   - 灾难场景：假设线程正在进行银行账户转账操作（A 账户已扣款，B 账户尚未加款），此时被 stop()，");
        System.out.println("     锁被强制释放，其他线程读取该账户就会看到数据处于完全破损的不一致状态（Data Corruption），极难排查！");
        System.out.println("2. 为什么不能使用 suspend() 与 resume()？");
        System.out.println("   - suspend() 挂起线程时【不会释放任何持有的锁】！");
        System.out.println("   - 如果唤醒线程需要先获得这把锁才能调用 resume()，两个线程将立刻陷入死锁！");
        System.out.println("3. 正确停止线程的核心原则 ——【协作式 (Cooperative)】：");
        System.out.println("   - 在 Java 中，没有外部力量可以安全地立刻强杀一个线程；");
        System.out.println("   - 外部只能给目标线程发送一个'终止请求信号'，由目标线程自己在合适的位置检查信号、释放资源并平滑退出。");
    }

    /**
     * 方式 1：volatile 标志位
     */
    static class VolatileWorker extends Thread {
        private volatile boolean running = true;

        public void cancel() {
            this.running = false;
        }

        @Override
        public void run() {
            long count = 0;
            while (running) {
                count++;
                // 模拟业务处理
            }
            System.out.printf("   [VolatileWorker] 感知到 running=false，执行清理收尾工作后安全退出，总步数: %d\n", count);
        }
    }

    private static void demonstrateVolatileFlagStop() throws InterruptedException {
        VolatileWorker worker = new VolatileWorker();
        worker.start();
        Thread.sleep(50);
        worker.cancel();
        worker.join();
        System.out.println("   -> 局限性说明：如果线程内部调用了 sleep()/wait() 或阻塞 I/O，无法执行到检查 running 变量的指令，会导致无法停止！");
    }

    /**
     * 方式 2：使用 interrupt() 的标准中断模式（两阶段终止）
     */
    static class InterruptibleWorker extends Thread {
        @Override
        public void run() {
            System.out.println("   [InterruptibleWorker] 启动运行...");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // 模拟周期性处理与可中断阻塞
                    System.out.println("   [InterruptibleWorker] 正在执行任务批次，准备休眠 500ms...");
                    Thread.sleep(500); // 阻塞点
                }
            } catch (InterruptedException e) {
                System.out.println("   [InterruptibleWorker] 在阻塞状态捕获到 InterruptedException 中断信号！");
            } finally {
                // 第二阶段：释放资源、关闭连接、持久化状态
                System.out.println("   [InterruptibleWorker] 进入 finally 块，成功释放数据库连接与文件句柄，优雅终止完成。");
            }
        }
    }

    private static void demonstrateInterruptGracefulStop() throws InterruptedException {
        InterruptibleWorker worker = new InterruptibleWorker();
        worker.start();
        Thread.sleep(100);
        System.out.println("   [MainThread] 发出 worker.interrupt() 中断请求！");
        worker.interrupt();
        worker.join();
        System.out.println("   -> 评价：工业级标准模式，无论线程正在计算还是在阻塞（sleep/wait），均能可靠感知并响应。");
    }

    /**
     * 方式 3：线程池优雅关闭三部曲
     */
    public static void demonstrateThreadPoolGracefulShutdown() {
        System.out.println("线上生产线程池关闭标准范式（三部曲）：");
        ExecutorService pool = Executors.newFixedThreadPool(2);

        // 提交几个简单任务
        pool.submit(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        });

        // 步骤 1：调用 shutdown()，停止接收新任务，已提交的任务与队列中任务继续执行
        pool.shutdown();
        System.out.println("   步骤 1: pool.shutdown() 已调用，拒绝新任务接收");

        try {
            // 步骤 2：设置等待超时时间，等待现有任务平稳执行完毕
            if (!pool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                System.out.println("   步骤 2: 等待超时，执行 pool.shutdownNow() 强制中断所有未完成任务");
                // 步骤 3：超时未结束则调用 shutdownNow() 尝试发送 interrupt()，并获取未执行任务列表
                pool.shutdownNow();
            } else {
                System.out.println("   步骤 2: 所有任务在规定超时时间内顺利执行完毕并安全终止！");
            }
        } catch (InterruptedException ex) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
