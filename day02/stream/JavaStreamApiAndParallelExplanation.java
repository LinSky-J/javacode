package day02.stream;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 面试专题：Java Stream 流式计算核心 API 体系及并行流（Parallel Stream）架构与实战解析。
 *
 * 本类对应面试核心题目：
 * 1. Java中stream的API介绍一下
 * 2. Stream流的并行API是什么?
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaStreamApiAndParallelExplanation {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("       Java Stream 流式处理全景体系 与 并行流底层机制深度解析          ");
        System.out.println("======================================================================");

        explainStreamApi();
        explainParallelStream();

        System.out.println("\n======================================================================");
        System.out.println("        Stream 流式计算与并行架构解析完毕，请细读类中源码与详细注释    ");
        System.out.println("======================================================================");
    }

    /**
     * 问题一：Java中stream的API介绍一下
     *
     * 面试核心考点：
     * 1. 结构化定义：Stream 不是数据结构，不存储数据，而是用于计算的数据管道（Pipeline）。
     * 2. 三大核心特性：无存储性、不改变源数据（函数式不变性）、惰性求值（Lazy Evaluation）。
     * 3. 流操作三大完整生命周期阶段（流创建、中间操作、终端操作）。
     * 4. 高频核心操作实战（filter/map/flatMap/distinct/sorted/peek/collect/groupingBy/reduce）。
     */
    public static void explainStreamApi() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题一：Java中stream的API介绍一下");
        System.out.println("--------------------------------------------------");

        /*
         * 【Stream 操作的三大完整生命周期】：
         *
         * 1. 阶段一：数据源与流的创建（Stream Creation）
         *    - 集合创建：collection.stream() 或 collection.parallelStream()
         *    - 数组创建：Arrays.stream(array)
         *    - 静态方法：Stream.of("a", "b", "c")、Stream.iterate()、Stream.generate()
         *    - I/O 读取：Files.lines(Paths.get(...))
         *
         * 2. 阶段二：中间操作（Intermediate Operations）
         *    - 特点：所有中间操作都是【惰性求值（Lazy）】的。单纯调用中间操作不会执行任何运算，
         *      只有当终端操作触发时，所有操作才会被整合成一条流水线一并处理。
         *    - 核心分类：
         *      * 筛选切片：filter(Predicate), distinct(), limit(long), skip(long)
         *      * 转换映射：map(Function), flatMap(Function，将多个流拍平为一个流)
         *      * 排序调试：sorted(Comparator), peek(Consumer，不中断流的中间窥视，适合日志排查)
         *
         * 3. 阶段三：终端操作（Terminal Operations）
         *    - 特点：触发整个流水线的真正遍历计算，计算完成后该 Stream 即宣告关闭失效，再次使用会抛出 IllegalStateException。
         *    - 核心分类：
         *      * 匹配查找：allMatch, anyMatch, noneMatch, findFirst, findAny
         *      * 聚合归纳：count(), max(), min(), reduce(BinaryOperator)
         *      * 遍历收集：forEach(Consumer), collect(Collectors.toList() / toMap() / groupingBy() 等)
         */

        List<ProductOrder> orders = Arrays.asList(
                new ProductOrder("ORD_101", "ELECTRONICS", 4999.0, 1, "PAID"),
                new ProductOrder("ORD_102", "BOOKS", 88.0, 2, "PAID"),
                new ProductOrder("ORD_103", "ELECTRONICS", 299.0, 3, "PAID"),
                new ProductOrder("ORD_104", "CLOTHING", 450.0, 1, "UNPAID"),
                new ProductOrder("ORD_105", "BOOKS", 120.0, 1, "CANCELLED"),
                new ProductOrder("ORD_106", "ELECTRONICS", 8999.0, 1, "PAID")
        );

        System.out.println("1. [实战 1：filter + map + sorted + limit 链式流水线]");
        List<String> topPaidElectronics = orders.stream()
                .filter(o -> "PAID".equals(o.getStatus()))                  // 过滤只看已支付
                .filter(o -> "ELECTRONICS".equals(o.getCategory()))         // 过滤数码家电
                .sorted((o1, o2) -> Double.compare(o2.getTotalAmount(), o1.getTotalAmount())) // 按总金额降序
                .limit(2)                                                   // 取最高的前 2 笔
                .map(o -> o.getOrderId() + ": ￥" + o.getTotalAmount())       // 提取字段映射为字符串
                .collect(Collectors.toList());
        System.out.println("   已支付数码产品金额前2名: " + topPaidElectronics);

        System.out.println("\n2. [实战 2：flatMap 扁平化映射应用]");
        List<List<String>> nestedTags = Arrays.asList(
                Arrays.asList("Java", "Spring"),
                Arrays.asList("Redis", "Kafka", "Java")
        );
        List<String> uniqueTags = nestedTags.stream()
                .flatMap(List::stream) // 将每个内层 List 展开成独立的 Stream，合并为一个大 Stream
                .distinct()           // 去重
                .collect(Collectors.toList());
        System.out.println("   多维嵌套集合经 flatMap 扁平化去重后: " + uniqueTags);

        System.out.println("\n3. [实战 3：Collectors.groupingBy 高级分组与聚合统计]");
        Map<String, List<ProductOrder>> groupByCategory = orders.stream()
                .collect(Collectors.groupingBy(ProductOrder::getCategory));
        System.out.println("   按商品类目分组统计:");
        groupByCategory.forEach((cat, list) -> System.out.println("     类目 [" + cat + "] -> 包含 " + list.size() + " 个订单"));

        DoubleSummaryStatistics stats = orders.stream()
                .filter(o -> "PAID".equals(o.getStatus()))
                .collect(Collectors.summarizingDouble(ProductOrder::getTotalAmount));
        System.out.printf("   已支付订单统计汇总 -> 笔数: %d, 总金额: ￥%.2f, 平均金额: ￥%.2f, 最大单笔: ￥%.2f\n",
                stats.getCount(), stats.getSum(), stats.getAverage(), stats.getMax());

        System.out.println("\n4. [实战 4：reduce 归约计算已支付订单总流水]");
        Optional<Double> totalRevenue = orders.stream()
                .filter(o -> "PAID".equals(o.getStatus()))
                .map(ProductOrder::getTotalAmount)
                .reduce(Double::sum);
        System.out.println("   通过 reduce 汇总总销售额: ￥" + totalRevenue.orElse(0.0));
    }

    /**
     * 问题二：Stream流的并行API是什么?
     *
     * 面试核心考点：
     * 1. 语法 API：Collection.parallelStream() 或 stream.parallel()。
     * 2. 底层架构：依托 Java 7 的 Fork/Join 框架与 ForkJoinPool.commonPool() 公共线程池（Work-Stealing 算法）。
     * 3. 元素分割器：Spliterator 及其 trySplit() 分治原理（哪些数据源切分高效，哪些低效）。
     * 4. 企业级生产避坑指南（多线程安全陷阱、全 JVM 共享池阻塞风险、小数据量负优化）。
     */
    public static void explainParallelStream() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题二：Stream流的并行API是什么?");
        System.out.println("--------------------------------------------------");

        /*
         * 【并行流核心机制深度解析】：
         *
         * 1. 核心 API 表现形式：
         *    - 从集合直接创建：List.parallelStream()
         *    - 从普通流无缝转换：Stream.parallel()（对应地，可以通过 .sequential() 转回串行流）。
         *
         * 2. 底层底层架构原理（Fork/Join 框架）：
         *    - 分治切割：利用数据源实现的 Spliterator（可分割迭代器），通过 trySplit() 递归对半切分为子任务。
         *    - 线程池调度：默认托管于全局共享的【ForkJoinPool.commonPool()】中执行。
         *      该池的核心线程数默认为：Runtime.getRuntime().availableProcessors() - 1（充分打满 CPU 核心）。
         *    - 工作窃取机制（Work-Stealing）：空闲的工作线程会主动从其他忙碌线程的双端队列尾部“窃取”子任务来执行，
         *      最大化减少 CPU 空转，提升并行吞吐。
         *
         * 3. 数据源切分效率差异：
         *    - 极佳：ArrayList、数组、IntStream.range。因为它们在内存中是连续存储的，支持 O(1) 按索引精准对半切分。
         *    - 极差：LinkedList、Stream.iterate。因为没有索引，切分必须进行 O(n) 的前置链表遍历，并行切分开销极大。
         *
         * 4. 生产环境三大致命使用陷阱（面试防踩坑必答）：
         *    - 陷阱一：线程安全性（Stateful operations）。
         *      在 parallelStream 的循环体内部绝对不能操作非线程安全的容器（如普通 ArrayList/HashMap），
         *      必须使用 collect(Collectors.toList()) 或并发容器，否则发生数据丢失和下标越界！
         *    - 陷阱二：全 JVM 公共池阻塞。
         *      ForkJoinPool.commonPool() 是全虚拟机所有并行流共享的。
         *      严禁在并行流中执行任何耗时的 I/O 阻塞操作（如远程 HTTP RPC 调用、慢数据库查询），
         *      否则会迅速耗尽整个应用的工作线程，导致全系统的并行流全部瘫痪！
         *    - 陷阱三：小数据量负优化。
         *      任务切分、线程上下文切换、跨 CPU 缓存同步、结果合并的开销往往大于计算本身，数据量较小时串行更快。
         */

        System.out.println("1. [性能实测：千万级数据运算在顺序流 vs 并行流下的耗时对比]");
        ParallelStreamPerformanceBenchmark.benchmarkSequentialVsParallel(10_000_000L);

        System.out.println("\n2. [线程安全实测：演示在 parallelStream 中操作普通集合的数据竞争与解决方案]");
        ParallelStreamPerformanceBenchmark.demonstrateThreadSafetyTrap(10_000);

        System.out.println("\n3. [企业生产选型准则总结]:");
        System.out.println("   - CPU 密集型且数据量超大（数十万以上）且数据源结构优良（如数组/ArrayList）：强力推荐使用 parallelStream。");
        System.out.println("   - 涉及网络 I/O、磁盘读写、或需要精确控制线程隔离与超时：坚决不用 parallelStream，改用 CompletableFuture 自定义独立线程池。");
    }
}
