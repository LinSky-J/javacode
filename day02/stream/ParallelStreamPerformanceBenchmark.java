package day02.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * 并行流性能压测与线程安全避坑演示工具类。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class ParallelStreamPerformanceBenchmark {

    /**
     * 压测单线程顺序流 vs 多线程并行流在千万级计算下的性能差距。
     */
    public static void benchmarkSequentialVsParallel(long n) {
        System.out.println("   [压测规模] 数据量: 1 到 " + n + " 的密集数学运算与过滤求和");

        // 1. 顺序流（Sequential Stream）
        long startSeq = System.currentTimeMillis();
        long sumSeq = LongStream.rangeClosed(1, n)
                .filter(x -> x % 2 == 0)
                .map(x -> x * 2)
                .sum();
        long timeSeq = System.currentTimeMillis() - startSeq;
        System.out.println("   -> 顺序流 (Sequential) 耗时: " + timeSeq + " ms, 运算结果: " + sumSeq);

        // 2. 并行流（Parallel Stream，底层依托 ForkJoinPool.commonPool）
        long startPar = System.currentTimeMillis();
        long sumPar = LongStream.rangeClosed(1, n)
                .parallel()
                .filter(x -> x % 2 == 0)
                .map(x -> x * 2)
                .sum();
        long timePar = System.currentTimeMillis() - startPar;
        System.out.println("   -> 并行流 (Parallel)   耗时: " + timePar + " ms, 运算结果: " + sumPar);
    }

    /**
     * 现场演示并行流最致命的线程安全“踩坑”陷阱：
     * 在 parallelStream 中使用非线程安全容器（如普通 ArrayList）导致数据丢失与下表越界！
     */
    public static void demonstrateThreadSafetyTrap(int elementsCount) {
        System.out.println("\n   [并行流踩坑实录] 尝试将 " + elementsCount + " 个数据通过 parallelStream().forEach() 塞入普通 ArrayList:");

        // 错误写法：在多线程中并发向非线程安全的 ArrayList 写入
        List<Integer> unsafeList = new ArrayList<>();
        try {
            LongStream.rangeClosed(1, elementsCount)
                    .parallel()
                    .forEach(i -> unsafeList.add((int) i));
        } catch (Exception e) {
            System.out.println("   [发生异常]: " + e.getClass().getSimpleName() + " (并发扩容竞争导致数组下标越界！)");
        }
        System.out.println("   预期列表长度: " + elementsCount + "，实际 unsafeList 长度: " + unsafeList.size() +
                (unsafeList.size() != elementsCount ? " [严重警告：发生并发数据丢失！]" : ""));

        // 正确写法 1：使用标准 Stream 终端收集器 collect(Collectors.toList())
        List<Long> safeListByCollect = LongStream.rangeClosed(1, elementsCount)
                .parallel()
                .boxed()
                .collect(Collectors.toList());
        System.out.println("   [标准修复方案 1] 使用 collect(Collectors.toList()): 最终长度=" + safeListByCollect.size() + " (100% 线程安全)");

        // 正确写法 2：使用线程安全同步集合
        List<Integer> safeSynchronizedList = Collections.synchronizedList(new ArrayList<>());
        LongStream.rangeClosed(1, elementsCount)
                .parallel()
                .forEach(i -> safeSynchronizedList.add((int) i));
        System.out.println("   [标准修复方案 2] 使用 Collections.synchronizedList: 最终长度=" + safeSynchronizedList.size() + " (100% 线程安全)");
    }
}
