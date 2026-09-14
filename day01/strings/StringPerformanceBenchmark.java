package strings;

/**
 * 辅助压测与多线程安全性验证工具类。
 * 用于现场演示 String 拼接、StringBuffer 与 StringBuilder 在单线程耗时及多线程并发下的关键差异。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class StringPerformanceBenchmark {

    /**
     * 单线程大批量循环拼接性能对比测试。
     */
    public static void benchmarkConcatenation(int iterations) {
        System.out.println("   [压测开始] 循环次数: " + iterations + " 次");

        // 1. String '+' 循环拼接测试
        long startStr = System.currentTimeMillis();
        String s = "";
        for (int i = 0; i < iterations; i++) {
            s += "a";
        }
        long timeStr = System.currentTimeMillis() - startStr;
        System.out.println("   1. String 循环 '+' 拼接耗时: " + timeStr + " ms (产生大量瞬时垃圾对象，触发频繁GC)");

        // 2. StringBuffer 拼接测试（线程安全，带锁同步开销）
        long startBuffer = System.currentTimeMillis();
        StringBuffer sbBuffer = new StringBuffer();
        for (int i = 0; i < iterations; i++) {
            sbBuffer.append("a");
        }
        long timeBuffer = System.currentTimeMillis() - startBuffer;
        System.out.println("   2. StringBuffer append 耗时: " + timeBuffer + " ms (带 synchronized 监视器锁开销)");

        // 3. StringBuilder 拼接测试（非线程安全，无锁极速）
        long startBuilder = System.currentTimeMillis();
        StringBuilder sbBuilder = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            sbBuilder.append("a");
        }
        long timeBuilder = System.currentTimeMillis() - startBuilder;
        System.out.println("   3. StringBuilder append 耗时: " + timeBuilder + " ms (无锁开销，单线程性能最优)");
    }

    /**
     * 多线程并发操作下的数据安全性测试。
     */
    public static void testConcurrencySafety(int threadCount, int operationsPerThread) {
        System.out.println("   [并发安全测试] 启动 " + threadCount + " 个线程，每个线程 append " + operationsPerThread + " 次字符");
        int expectedTotalLength = threadCount * operationsPerThread;

        // 测试 StringBuffer 并发追加
        StringBuffer buffer = new StringBuffer();
        Thread[] bufferThreads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            bufferThreads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    buffer.append("x");
                }
            });
            bufferThreads[i].start();
        }
        for (Thread t : bufferThreads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        }

        // 测试 StringBuilder 并发追加
        StringBuilder builder = new StringBuilder();
        Thread[] builderThreads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            builderThreads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    builder.append("y");
                }
            });
            builderThreads[i].start();
        }
        for (Thread t : builderThreads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        }

        System.out.println("   [预期最终字符串总长度]: " + expectedTotalLength);
        System.out.println("   -> StringBuffer 实际长度: " + buffer.length() + " (完全吻合预期，synchronized 保证线程安全)");
        System.out.println("   -> StringBuilder 实际长度: " + builder.length() +
                (builder.length() == expectedTotalLength ?
                        " (偶然一致，但缺乏内存可见性与原子性保护)" :
                        " (出现数据丢失与长度偏差！证明线程非安全)"));
    }
}
