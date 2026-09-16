package threadlocal;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ThreadLocal 作用、底层原理、Entry 结构、内存泄露成因与生产解决方案
 *
 * 面试原题：
 * - Threadlocal作用，原理，具体里面存的key value是啥，会有什么问题，如何解决?
 *
 * 核心考点：
 * 1. ThreadLocal 核心价值：线程私有数据隔离与全链路调用上下文隐式传递 (TraceId / UserContext)
 * 2. 内部架构：Thread 内部持有 ThreadLocalMap，真正持有数据的是 Thread 而非 ThreadLocal 实例
 * 3. 存储实体：Entry 继承 WeakReference<ThreadLocal<?>>，Key 为弱引用，Value 为强引用
 * 4. 内存泄露根本成因：外部强引用断开后 Key 被 GC 清理为 null，但线程池线程长期存活导致 Value 强引用无法回收
 * 5. 线程池复用下的【脏数据污染】隐患实测
 * 6. 工业级避坑标准规范：try-finally 结构中必须显式调用 threadLocal.remove()
 */
public class ThreadLocalInternalsAndMemoryLeak {

    // 业务用户上下文模拟
    private static final ThreadLocal<String> USER_CONTEXT = new ThreadLocal<>();

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】ThreadLocal 作用、底层架构与 Key/Value 存储结构剖析");
        System.out.println("================================================================================");
        explainThreadLocalCorePrinciples();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战演示 1】ThreadLocal 跨方法上下文隐式传递与线程独立性");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateContextPropagation();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战隐患 2】线程池复用导致【脏数据污染】与【内存泄露】实测");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateThreadPoolDirtyDataAndLeakHazard();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【生产规范最佳实践】ThreadLocal 的标准安全使用范式");
        System.out.println("--------------------------------------------------------------------------------");
        explainProductionBestPractice();
    }

    /**
     * 理论核心剖析
     */
    public static void explainThreadLocalCorePrinciples() {
        System.out.println("1. ThreadLocal 的核心作用：");
        System.out.println("   (1) 线程数据隔离 (Thread Confinement)：让每个线程都拥有自己的专属变量副本，各玩各的，从源头消灭并发写冲突（如 SimpleDateFormat、DB Connection）；");
        System.out.println("   (2) 全链路上下文隐式传递 (Context Propagation)：在同一个线程处理请求的过程中（Controller -> Service -> Dao），");
        System.out.println("       无需在每一个方法签名上层层增加 userId、traceId 等参数，直接通过静态 ThreadLocal 随取随用。");
        System.out.println();
        System.out.println("2. ThreadLocal 的底层原理与存储结构：");
        System.out.println("   - 【纠正误解】：不是 ThreadLocal 内部维护一个大 Map 存所有的线程！");
        System.out.println("   - 【真实架构】：每一个 Thread 对象的内部，都拥有一个独享的成员变量：ThreadLocal.ThreadLocalMap threadLocals；");
        System.out.println("   - 数据实际存储在【当前运行线程对象自身】的 ThreadLocalMap 中，ThreadLocal 实例仅仅充当访问这块内存的【Key / 访问句柄】！");
        System.out.println();
        System.out.println("3. 里面存的 Key 和 Value 具体是什么？（大厂超高频必考细节点）：");
        System.out.println("   - ThreadLocalMap 内部维护了一个 Entry[] 数组，采用线性探测法 (Linear Probing) 解决 Hash 冲突；");
        System.out.println("   - Entry 的定义：static class Entry extends WeakReference<ThreadLocal<?>> { Object value; }");
        System.out.println("   - 【Key 是什么】：Key 是对 ThreadLocal 实例对象的【弱引用 (WeakReference)】！");
        System.out.println("   - 【Value 是什么】：Value 是用户实际存入的业务数据对象的【强引用 (Strong Reference)】！");
    }

    /**
     * 演示上下文传递
     */
    private static void demonstrateContextPropagation() throws InterruptedException {
        Thread workerA = new Thread(() -> {
            USER_CONTEXT.set("User_Alice_Token_123");
            simulateServiceLayer();
            USER_CONTEXT.remove(); // 用完清理
        }, "Worker-Alice");

        Thread workerB = new Thread(() -> {
            USER_CONTEXT.set("User_Bob_Token_456");
            simulateServiceLayer();
            USER_CONTEXT.remove(); // 用完清理
        }, "Worker-Bob");

        workerA.start();
        workerB.start();
        workerA.join();
        workerB.join();
    }

    private static void simulateServiceLayer() {
        // 模拟底层深层方法直接读取，无需显式传参
        String user = USER_CONTEXT.get();
        System.out.printf("   [%s] 在 Service 业务层成功隐式获取当前登录用户上下文: %s\n",
                Thread.currentThread().getName(), user);
    }

    /**
     * 演示线程池下的脏数据与内存泄露成因
     */
    private static void demonstrateThreadPoolDirtyDataAndLeakHazard() throws Exception {
        System.out.println("内存泄露根本成因剖析：");
        System.out.println("   1. 外部强引用断开：当业务代码将 ThreadLocal 变量置为 null 后，Entry 中的 Key 属于弱引用，下一次 GC 时 Key 会被垃圾回收变 null；");
        System.out.println("   2. Value 强引用依然存在：但是当前线程的 ThreadLocalMap 依然引用着 Entry，Entry.value 强引用着实际对象；");
        System.out.println("   3. 线程池长久存活：由于线程池核心线程长期常驻内存不销毁，导致一条存活强引用链：");
        System.out.println("      Thread -> ThreadLocalMap -> Entry -> Value (强引用对象)；");
        System.out.println("      只要线程不终结，这部分内存永久无法被 GC 回收，越积越多，最终直接打爆堆内存引发 OOM！");
        System.out.println();
        System.out.println("【实战模拟】：单线程池处理两次请求，如果不 remove() 会发生脏数据串读：");

        ExecutorService singlePool = Executors.newSingleThreadExecutor();

        // 模拟请求 1：登录用户 Charlie，但忘记 remove()
        singlePool.submit(() -> {
            USER_CONTEXT.set("User_Charlie (敏感VIP)");
            System.out.printf("   [Request-1] 线程 %s 处理请求完成，但由于失误【忘记调用 remove()】\n",
                    Thread.currentThread().getName());
        }).get();

        // 模拟请求 2：匿名游客访问，未设置 USER_CONTEXT
        singlePool.submit(() -> {
            String leakedUser = USER_CONTEXT.get();
            System.out.printf("   [Request-2 严重事故！] 匿名访客在线程 %s 中意外读取到了上一请求的残留数据: %s\n",
                    Thread.currentThread().getName(), leakedUser);
            USER_CONTEXT.remove(); // 亡羊补牢清理
        }).get();

        singlePool.shutdown();
    }

    /**
     * 生产安全规范
     */
    public static void explainProductionBestPractice() {
        System.out.println("【大厂阿里巴巴开发手册强制规范】：");
        System.out.println("使用 ThreadLocal 时，必须使用 try-finally 块，确保在 finally 中显式调用 remove()！");
        System.out.println("标准生产级模板代码：");
        System.out.println("   public void processRequest(Request req) {");
        System.out.println("       try {");
        System.out.println("           USER_CONTEXT.set(req.getUser());");
        System.out.println("           doBusiness(); // 执行核心业务逻辑");
        System.out.println("       } finally {");
        System.out.println("           USER_CONTEXT.remove(); // 关键！坚决杜绝脏数据污染与内存泄露！");
        System.out.println("       }");
        System.out.println("   }");
    }
}
