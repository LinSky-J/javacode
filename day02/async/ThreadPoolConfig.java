package day02.async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义业务专用线程池配置。
 *
 * 核心考点：
 * 为什么生产环境中严禁使用 CompletableFuture 默认的 ForkJoinPool.commonPool()？
 * 答：默认公共池是全 JVM 共享的，且核心线程数受限于 CPU 核心数。
 * 一旦有任务执行了慢网络 I/O，会造成公共池线程迅速耗尽，拖垮整个服务的所有异步任务。
 * 生产环境必须针对不同业务模块显式传入自定义的 ThreadPoolExecutor，实现【线程资源隔离】。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class ThreadPoolConfig {

    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 8;
    private static final int QUEUE_CAPACITY = 100;
    private static final long KEEP_ALIVE_SECONDS = 60L;

    private static final ExecutorService BUSINESS_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_SECONDS,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY),
            new CustomThreadFactory("async-biz-worker-"),
            new ThreadPoolExecutor.CallerRunsPolicy() // 队列满后由调用者线程直接兜底执行，防止丢数据
    );

    public static ExecutorService getBusinessExecutor() {
        return BUSINESS_EXECUTOR;
    }

    /**
     * 自定义线程工厂，为线程设置明确命名前缀，极大便利线上 Arthas/JStack 排查死锁与高 CPU 问题
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger threadIndex = new AtomicInteger(1);

        public CustomThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + threadIndex.getAndIncrement());
            t.setDaemon(false);
            return t;
        }
    }
}
