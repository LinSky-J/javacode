package async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 面试专题：CompletableFuture 异步非阻塞响应式编程高阶实战全解析。
 *
 * 本类对应面试核心题目：
 * 1. completableFuture怎么用的?
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class CompletableFutureUsageExplanation {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("      CompletableFuture 异步任务编排、多源聚合与异常降级全景实战      ");
        System.out.println("======================================================================");

        explainCompletableFutureUsage();

        System.out.println("\n======================================================================");
        System.out.println("     CompletableFuture 异步体系解析完毕，请细读类中源码与详细注释      ");
        System.out.println("======================================================================");

        // 关闭自定义线程池，保证测试进程正常退出
        ThreadPoolConfig.getBusinessExecutor().shutdown();
    }

    /**
     * 问题：completableFuture怎么用的?
     *
     * 面试核心考点：
     * 1. 概念背景：对比传统 Future.get() 阻塞等待的痛点，CompletableFuture 引入了事件驱动、非阻塞链式回调机制。
     * 2. 创建方式：supplyAsync（有返回值）与 runAsync（无返回值），务必强调传入自定义 Executor 线程池做资源隔离！
     * 3. 链式结果传递：thenApply（结果映射转换）、thenAccept（结果最终消费）、thenRun（仅做后续触发）。
     * 4. 任务组合编排：
     *    - thenCompose（串行依赖，避免双层泛型嵌套）。
     *    - thenCombine（两路并行任务协同，双双完成后合并结果）。
     * 5. 多源大聚合（大厂 BFF 网关核心）：allOf（全量等待并发请求聚合）、anyOf（最快响应竞速）。
     * 6. 异常与熔断降级：exceptionally、handle、whenComplete。
     */
    public static void explainCompletableFutureUsage() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("核心题目：completableFuture怎么用的?");
        System.out.println("--------------------------------------------------");

        ExecutorService executor = ThreadPoolConfig.getBusinessExecutor();
        String userId = "USER_8888";

        /*
         * 场景一：异步任务的创建与线程池隔离
         * - supplyAsync: 接收 Supplier<U>，带返回值。
         * - runAsync: 接收 Runnable，无返回值。
         * - 关键设计：必须传入业务线程池 executor，严禁使用无参的 ForkJoinPool.commonPool()。
         */
        System.out.println("1. [实战 1：异步启动任务与结果非阻塞转换（thenApply / thenAccept）]");
        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("   [" + Thread.currentThread().getName() + "] 正在异步生成初始订单号...");
            return "ORDER_NO_20260914";
        }, executor).thenApply(orderNo -> {
            // thenApply 接收上一步的 orderNo，并加工转换为带前缀的加密单号
            System.out.println("   [" + Thread.currentThread().getName() + "] 对订单号进行安全加签加密处理...");
            return "SIGNED_" + orderNo;
        });

        orderFuture.thenAccept(signedOrder -> {
            // thenAccept 消费最终结果，无返回值
            System.out.println("   [" + Thread.currentThread().getName() + "] 终态消费处理 -> 订单落库成功: " + signedOrder);
        }).join(); // join 阻塞等待该演示链路完成

        /*
         * 场景二：任务的组合依赖编排（thenCompose 串行依赖 vs thenCombine 并行合并）
         * - thenCompose: 任务 B 强依赖 任务 A 的输出结果。例如：先查用户信息，根据用户级别再查专属优惠。
         *   对比 map 与 flatMap：thenCompose 会自动把外层 CompletableFuture 扁平化展开！
         * - thenCombine: 任务 A 与 任务 B 互不干扰，并行并发执行，等两者全部完成后，执行合并函数 BiFunction。
         */
        System.out.println("\n2. [实战 2：两路并行任务并发执行并合并结果（thenCombine）]");
        long startTime = System.currentTimeMillis();

        CompletableFuture<Boolean> riskFuture = CompletableFuture.supplyAsync(
                () -> RemoteServiceMock.queryRiskScore(userId), executor);
        CompletableFuture<Double> couponFuture = CompletableFuture.supplyAsync(
                () -> RemoteServiceMock.queryAvailableCoupon(userId), executor);

        // 两路网络请求并行发出，随后通过 thenCombine 合并结果
        CompletableFuture<String> checkoutFuture = riskFuture.thenCombine(couponFuture, (riskPassed, couponAmount) -> {
            System.out.println("   [" + Thread.currentThread().getName() + "] 合并两路并发查询结果: 风控审核=" + riskPassed + ", 优惠金额=" + couponAmount);
            if (riskPassed) {
                return "允许结算，本单立减 ￥" + couponAmount;
            } else {
                return "风控拦截，拒绝下单";
            }
        });
        System.out.println("   结算审核结论: " + checkoutFuture.join() +
                " (两路耗时 40ms 与 60ms 并行执行，总耗时仅: " + (System.currentTimeMillis() - startTime) + " ms)");

        /*
         * 场景三：高并发 BFF 网关多服务聚合（CompletableFuture.allOf）
         * - 电商 App 首页/详情页需要同时向 5~10 个独立微服务拉取数据（商品详情、库存、评论、店铺、优惠券、推荐列表）。
         * - 传统单线程串行：耗时 = 50ms + 40ms + 60ms = 150ms。
         * - allOf 并发聚合：耗时 = max(50ms, 40ms, 60ms) = 约 60ms！吞吐量成倍提升！
         */
        System.out.println("\n3. [实战 3：企业级网关多源聚合（CompletableFuture.allOf）]");
        long allOfStart = System.currentTimeMillis();

        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> RemoteServiceMock.queryUserInfo(userId), executor);
        CompletableFuture<Boolean> f2 = CompletableFuture.supplyAsync(() -> RemoteServiceMock.queryRiskScore(userId), executor);
        CompletableFuture<Double> f3 = CompletableFuture.supplyAsync(() -> RemoteServiceMock.queryAvailableCoupon(userId), executor);

        // allOf 接收任意多个 Future，返回一个新的 CompletableFuture<Void>，所有任务完成后它才触发完成
        CompletableFuture.allOf(f1, f2, f3).join();
        System.out.println("   全部微服务接口并发聚合完毕，总耗时: " + (System.currentTimeMillis() - allOfStart) + " ms");
        System.out.println("   -> 聚合结果 1: " + f1.join());
        System.out.println("   -> 聚合结果 2: 风控状态=" + f2.join());
        System.out.println("   -> 聚合结果 3: 优惠券抵扣=" + f3.join());

        /*
         * 场景四：异常捕获与熔断降级（exceptionally 与 handle）
         * - 传统多线程中异常难以跨线程捕捉，通常静默吞掉。
         * - exceptionally: 发生异常时触发，返回一个安全的降级兜底默认值（Fallback）。
         * - handle: 无论是否发生异常都会执行，接收 (result, throwable) 双入参，用于全能监控与自定义兜底。
         */
        System.out.println("\n4. [实战 4：异步异常捕获与服务熔断降级（exceptionally / handle）]");
        CompletableFuture<String> creditFuture = CompletableFuture.supplyAsync(
                () -> RemoteServiceMock.queryExternalCreditReport(userId), executor
        ).exceptionally(ex -> {
            System.out.println("   [捕获到远程异步调用异常]: " + ex.getMessage());
            System.out.println("   [触发容灾熔断降级策略] 返回本地缓存兜底征信报告...");
            return "本地兜底征信数据：信用良好（白名单用户）";
        });

        System.out.println("   最终获取的征信结果: " + creditFuture.join());
    }
}
