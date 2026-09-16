package threadpool;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ==============================================================================================
 * 面试专题：线程池生命周期、优雅停机与任务撤回机制 (ThreadPoolLifecycleAndTaskCancellation)
 * ==============================================================================================
 * 本类针对大厂面试中关于“shutdown 与 shutdownNow 区别、优雅停机、已提交任务撤回机制”的高频考点进行源码级剖析：
 *
 * 11. 线程池中 shutdown()，shutdownNow() 这两个方法有什么作用?
 * 12. 提交给线程池中的任务可以被撤回吗？
 *
 * ==============================================================================================
 * 一、shutdown() 与 shutdownNow() 的核心作用与区别
 * ==============================================================================================
 * 1. shutdown() —— 温和关闭（继续消化存量）：
 *    - 状态迁移：将线程池状态从 RUNNING 切换为 SHUTDOWN。
 *    - 接收新任务：立即停止接收新任务，后续提交的任务会直接触发饱和拒绝策略。
 *    - 存量任务处理：
 *      - 正在执行的任务：继续执行完毕，不受任何影响。
 *      - 工作队列中的任务：继续保留在队列中，线程池中的工作线程会继续将队列中的任务全部消费完毕。
 *    - 线程中断行为：只调用 interruptIdleWorkers()，仅中断阻塞在 getTask() 上等待任务的空闲线程。
 *    - 返回值：void。
 *
 * 2. shutdownNow() —— 强制关闭（排空队列并中断）：
 *    - 状态迁移：将线程池状态直接切换为 STOP。
 *    - 接收新任务：立即停止接收新任务。
 *    - 存量任务处理：
 *      - 正在执行的任务：尝试向所有正在运行任务的工作线程发出中断信号（调用 Thread.interrupt()）。
 *      - 工作队列中的任务：调用 drainQueue() 将工作队列中所有尚未执行的任务全部清空拔出！
 *    - 返回值：返回包含所有未执行任务的列表 List<Runnable>！
 *      【企业级价值】：调用方可以拿到这批未执行的任务，做持久化落库、重投 MQ 削峰、或落盘记录补偿日志。
 *
 * 3. 企业级“优雅停机 (Graceful Shutdown)”标准防御模板：
 *    - 行业标准三步走：
 *      ① 先调用 shutdown() 停止接单；
 *      ② 调用 awaitTermination(timeout) 给予一段合理的等待时间消化排队任务；
 *      ③ 若超时仍未结束，则退化调用 shutdownNow() 强制打断，并收集未完成任务进行补偿持久化。
 *
 * ==============================================================================================
 * 二、提交给线程池中的任务可以被撤回吗？
 * ==============================================================================================
 * 【结论】：完全可以撤回！通常有以下三种核心实现方式：
 *
 * 1. 方式一：通过 Future.cancel(boolean mayInterruptIfRunning) 撤回（最常用）
 *    - 场景 A：任务还在阻塞队列中排队（尚未开始执行）：
 *      - 此时无论是 cancel(false) 还是 cancel(true)，该 FutureTask 内部状态直接流转为 CANCELLED。
 *      - 当 Worker 从队列中取出该任务执行时，发现状态已经是 CANCELLED，直接跳过 run() 逻辑，任务被彻底撤回！
 *    - 场景 B：任务正在执行中：
 *      - 若传入 mayInterruptIfRunning = true：会直接调用执行该任务的工作线程的 interrupt() 发出中断信号！
 *        如果任务内部有响应中断的代码（例如抛出 InterruptedException 或检测 isInterrupted()），任务会提前终止退出！
 *      - 若传入 mayInterruptIfRunning = false：不会向线程发送中断，任务会继续执行完，但该 Future 会标记为取消，
 *        后续任何线程调用 future.get() 都会直接抛出 CancellationException。
 *    - 场景 C：任务已经执行完毕：
 *      - cancel() 返回 false，撤回失败。
 *
 * 2. 方式二：通过 ThreadPoolExecutor.remove(Runnable task) 物理撤回
 *    - 机制：直接从线程池底层维护的 BlockingQueue 中调用 remove(task) 方法将该任务节点物理剔除！
 *    - 适用：尚未开始执行（仍在队列排队）的任务。成功返回 true，被移除的任务永远不会被执行。
 *
 * 3. 方式三：配合 purge() 清理队列中已取消的僵尸 FutureTask
 *    - 当大量任务被 cancel() 后，虽然任务不会执行，但在很多队列实现中仍暂留节点占用内存。
 *    - 调用 pool.purge() 可以主动遍历队列，物理清理所有已被标记为 cancelled 的 FutureTask，释放内存。
 *
 * @author Java面试题通关指南
 */
public class ThreadPoolLifecycleAndTaskCancellation {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println(" 1. 深度对比 shutdown() 与 shutdownNow() 的执行差异与优雅停机模板");
        System.out.println("======================================================================");
        testShutdownVsShutdownNow();

        System.out.println("\n======================================================================");
        System.out.println(" 2. 任务撤回机制实测：Future.cancel(false) 队列排队阶段撤回");
        System.out.println("======================================================================");
        testTaskCancellationBeforeExecution();

        System.out.println("\n======================================================================");
        System.out.println(" 3. 任务撤回机制实测：Future.cancel(true) 运行中响应中断强行撤回");
        System.out.println("======================================================================");
        testTaskCancellationDuringExecutionWithInterrupt();

        System.out.println("\n======================================================================");
        System.out.println(" 4. 任务撤回机制实测：ThreadPoolExecutor.remove(Runnable) 物理队列剔除");
        System.out.println("======================================================================");
        testTaskPhysicalRemoveFromQueue();
    }

    /**
     * 演示一：对比 shutdown() 与 shutdownNow()
     */
    public static void testShutdownVsShutdownNow() throws InterruptedException {
        // 创建一个核心为1，最大为1，队列容量为2的线程池
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                r -> new Thread(r, "shutdown-test-worker")
        );

        // 提交3个任务：任务1执行中，任务2和3在队列中排队
        pool.execute(() -> {
            System.out.println("  [任务1-执行中] 开始模拟耗时操作 (预计2000ms)...");
            try {
                Thread.sleep(2000);
                System.out.println("  [任务1-执行中] 正常执行结束！");
            } catch (InterruptedException e) {
                System.out.println("  [任务1-执行中] 收到中断信号被强制打断退出！");
            }
        });
        pool.execute(() -> System.out.println("  [任务2-排队中] 执行成功"));
        pool.execute(() -> System.out.println("  [任务3-排队中] 执行成功"));

        Thread.sleep(100); // 确保任务1已开始，任务2/3已入队

        System.out.println(">> 调用 shutdownNow() 强制停止，并捕获被清空的未执行任务列表...");
        List<Runnable> droppedTasks = pool.shutdownNow();

        System.out.printf(">> shutdownNow() 返回被遗弃/未执行任务数: %d%n", droppedTasks.size());
        for (int i = 0; i < droppedTasks.size(); i++) {
            System.out.printf("   - 未执行任务 [%d] 可落库补偿持久化: %s%n", i + 1, droppedTasks.get(i).getClass().getSimpleName());
        }

        // 尝试提交新任务，必然被拒绝
        try {
            pool.execute(() -> System.out.println("新任务"));
        } catch (Exception e) {
            System.out.println(">> 关闭后再次提交新任务，成功触发拒绝异常: " + e.getClass().getSimpleName());
        }

        // 等待线程彻底终结
        pool.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 演示二：通过 Future.cancel 撤回尚未执行的任务
     */
    public static void testTaskCancellationBeforeExecution() throws InterruptedException {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> new Thread(r, "cancel-worker")
        );

        // 先提交一个长任务占住唯一的核心线程
        pool.submit(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        });

        // 提交待撤回的任务到队列中
        Future<String> pendingTaskFuture = pool.submit(() -> {
            System.out.println("  [如果看到这行输出，说明撤回失败了！]");
            return "SUCCESS";
        });

        System.out.println(">> 任务已提交并在队列排队中，立即发起撤回: future.cancel(false)...");
        boolean cancelResult = pendingTaskFuture.cancel(false);
        System.out.printf(">> 撤回结果: %b | 是否已取消: %b%n", cancelResult, pendingTaskFuture.isCancelled());

        try {
            pendingTaskFuture.get();
        } catch (CancellationException ce) {
            System.out.println(">> 成功验证：获取已撤回任务结果抛出预期的 CancellationException 异常！");
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 演示三：任务正在执行中，通过 cancel(true) 结合线程中断信号撤回正在运行的任务
     */
    public static void testTaskCancellationDuringExecutionWithInterrupt() throws InterruptedException {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> new Thread(r, "interrupt-worker")
        );

        Future<?> runningFuture = pool.submit(() -> {
            System.out.println("  [运行中任务] 任务已启动，正在执行死循环或长时间计算...");
            int loopCount = 0;
            while (true) {
                // 关键点：规范的并发任务必须响应中断标志位！
                if (Thread.currentThread().isInterrupted()) {
                    System.out.printf("  [运行中任务] 收到中断信号！优雅清理资源，提前退出任务循环 (已迭代 %d 次)%n", loopCount);
                    break;
                }
                loopCount++;
                try {
                    Thread.sleep(50); // 模拟阶段性工作
                } catch (InterruptedException e) {
                    System.out.println("  [运行中任务] 在阻塞等待阶段捕获到 InterruptedException，提前撤回退出！");
                    Thread.currentThread().interrupt(); // 保持中断标志
                    break;
                }
            }
        });

        Thread.sleep(120); // 确保任务已经进入运行状态

        System.out.println(">> 发起正在运行中任务的强行撤回: future.cancel(true)...");
        boolean cancelled = runningFuture.cancel(true);
        System.out.printf(">> cancel(true) 执行结果: %b%n", cancelled);

        Thread.sleep(200); // 观察任务中断响应输出

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * 演示四：通过 ThreadPoolExecutor.remove(Runnable) 从队列中物理剔除排队任务
     */
    public static void testTaskPhysicalRemoveFromQueue() throws InterruptedException {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> new Thread(r, "remove-worker")
        );

        // 占住核心线程
        pool.execute(() -> {
            try {
                Thread.sleep(600);
            } catch (InterruptedException ignored) {
            }
        });

        Runnable queuedTask = () -> System.out.println("  [该任务已被物理移除，绝不会被执行]");

        pool.execute(queuedTask);
        System.out.printf("[提交后] 队列排队任务数: %d%n", pool.getQueue().size());

        // 从队列中物理移除
        boolean removed = pool.remove(queuedTask);
        System.out.printf(">> 调用 pool.remove(queuedTask) 物理移除结果: %b | 移除后队列大小: %d%n",
                removed, pool.getQueue().size());

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.SECONDS);
    }
}
