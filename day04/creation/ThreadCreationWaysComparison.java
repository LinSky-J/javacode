package creation;

import java.util.concurrent.*;

/**
 * Java 线程创建方式全解与底层本质追问
 *
 * 面试原题：
 * - 线程的创建方式有哪些?
 *
 * 核心考点：
 * 1. 表面上的常见创建方式：继承 Thread、实现 Runnable、实现 Callable + FutureTask、线程池 ExecutorService、Java 21 虚拟线程
 * 2. 深度追问：“创建线程在 Java 本质上有几种方式？”
 * 3. 为什么阿里开发手册严禁显式 new Thread() 以及严禁使用 Executors 默认线程池工厂？
 * 4. 各创建方式优缺点与选型指南
 */
public class ThreadCreationWaysComparison {

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】Java 线程的创建方式及底层本质剖析");
        System.out.println("================================================================================");
        explainCreationEssence();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战演示 1】继承 Thread 类");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateThreadClass();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战演示 2】实现 Runnable 接口（解耦任务与线程）");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateRunnableInterface();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战演示 3】实现 Callable 接口 + FutureTask（有返回值与异常抛出）");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateCallableFuture();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战演示 4】自定义 ThreadPoolExecutor 线程池管理");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateThreadPool();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【阿里规范深度】为什么生产严禁直接 new Thread 与严禁 Executors 默认工厂？");
        System.out.println("--------------------------------------------------------------------------------");
        explainProductionBestPractices();
    }

    /**
     * 本质追问解答
     */
    public static void explainCreationEssence() {
        System.out.println("大厂高频追问：'Java 中创建线程到底有几种方式？'");
        System.out.println("金牌应答：");
        System.out.println("1. 表象上看有 4 种（继承 Thread、实现 Runnable、实现 Callable、利用线程池）。");
        System.out.println("2. 但从 JVM 底层架构与源码本质来看，【只有一种方式】——那就是【构造系统 Thread 实例并调用 JNI start0()】！");
        System.out.println("   - Runnable 和 Callable 只是定义了【待执行的任务单元（Task）】，它们本身没有操作系统线程实体；");
        System.out.println("   - Callable 最终通过 FutureTask 适配为 Runnable；");
        System.out.println("   - 线程池（ThreadPoolExecutor）内部的 Worker 线程，归根结底是通过 ThreadFactory.newThread() 实例化 Thread；");
        System.out.println("   - 最终都是通过 Thread 类的 start0() 本地调用，由操作系统内核分配真实物理线程执行。");
    }

    /**
     * 方式 1：继承 Thread
     */
    static class CustomThread extends Thread {
        @Override
        public void run() {
            System.out.printf("   [Way 1-Thread] 线程名: %s, ID: %d 正在执行继承 Thread 任务\n",
                    getName(), getId());
        }
    }

    private static void demonstrateThreadClass() throws InterruptedException {
        CustomThread t1 = new CustomThread();
        t1.setName("CustomThread-Demo");
        t1.start();
        t1.join();
        System.out.println("   -> 评价：受限 Java 单继承；线程逻辑与任务内容高度耦合。");
    }

    /**
     * 方式 2：实现 Runnable
     */
    private static void demonstrateRunnableInterface() throws InterruptedException {
        Runnable task = () -> {
            System.out.printf("   [Way 2-Runnable] 线程名: %s 执行 Runnable 任务，实现任务与执行器解耦\n",
                    Thread.currentThread().getName());
        };
        Thread t2 = new Thread(task, "RunnableThread-Demo");
        t2.start();
        t2.join();
        System.out.println("   -> 评价：推荐方案，避免单继承限制；同一个 Runnable 实例可被多个 Thread 共享。");
    }

    /**
     * 方式 3：实现 Callable + FutureTask
     */
    private static void demonstrateCallableFuture() throws Exception {
        Callable<Integer> callableTask = () -> {
            System.out.printf("   [Way 3-Callable] 线程名: %s 正在计算复杂算式...\n",
                    Thread.currentThread().getName());
            Thread.sleep(50);
            return 42 * 10;
        };

        // FutureTask 实现了 RunnableFuture (Runnable + Future)
        FutureTask<Integer> futureTask = new FutureTask<>(callableTask);
        Thread t3 = new Thread(futureTask, "CallableThread-Demo");
        t3.start();

        // 阻塞获取结果，支持设置超时时间
        Integer result = futureTask.get(1, TimeUnit.SECONDS);
        System.out.println("   [Way 3-Callable] 成功获取异步计算结果: " + result);
        System.out.println("   -> 评价：具备返回值，且能捕获抛出受检异常（Checked Exception）。");
    }

    /**
     * 方式 4：标准线程池 ExecutorService
     */
    private static void demonstrateThreadPool() throws Exception {
        // 标准自定义线程池，避免资源耗尽
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,
                4,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                new ThreadFactory() {
                    private int count = 1;
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "CustomPool-Worker-" + count++);
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        try {
            Future<String> future = executor.submit(() -> {
                return "Worker 处理任务完成并返回业务数据";
            });
            System.out.println("   [Way 4-ThreadPool] 线程池任务输出: " + future.get());
        } finally {
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
        System.out.println("   -> 评价：企业级核心方式；线程复用降低创建销毁开销、有效控制并发水位。");
    }

    /**
     * 阿里规范实战指南
     */
    public static void explainProductionBestPractices() {
        System.out.println("1. 为什么严禁显式 new Thread()？");
        System.out.println("   - 随意 new Thread 无法控制并发数量，瞬间高并发会导致创建数万个线程，占满内存诱发 OOM；");
        System.out.println("   - 线程缺乏统一命名与监控，线上问题排查困难；频繁创建与销毁系统线程浪费 CPU。");
        System.out.println("2. 为什么严禁使用 Executors 工具类创建默认线程池？");
        System.out.println("   - FixedThreadPool / SingleThreadPool：底层使用无界队列 LinkedBlockingQueue (容量 Integer.MAX_VALUE)，");
        System.out.println("     任务堆积会无休止消耗堆内存，最终引发 java.lang.OutOfMemoryError: Java heap space。");
        System.out.println("   - CachedThreadPool：允许创建的最大线程数为 Integer.MAX_VALUE，当请求激增时会疯狂创建线程，");
        System.out.println("     导致 java.lang.OutOfMemoryError: unable to create new native thread，甚至瘫痪整个操作系统！");
        System.out.println("3. 生产标准：必须通过 ThreadPoolExecutor 显式构造，指定有界队列（如 ArrayBlockingQueue）和拒绝策略。");
    }
}
