package async;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程工厂，为线程设置明确命名前缀，极大便利线上 Arthas/JStack 排查死锁与高 CPU 问题
 */
public class CustomThreadFactory implements ThreadFactory {

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
