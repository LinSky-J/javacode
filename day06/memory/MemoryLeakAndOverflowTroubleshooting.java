package memory;

import java.util.ArrayList;
import java.util.List;

/**
 * 内存泄漏与内存溢出深度剖析、JVM 五大 OOM 场景与企业级排查 SOP 实战
 *
 * 涵盖面试核心题目：
 * 1. 内存泄漏和内存溢出的理解?
 * 2. jvm内存结构有哪几种内存溢出的情况?
 * 3. 遇到过堆溢出的情况吗？如何解决?
 * 4. 栈溢出的情况呢?
 * 5. 有具体的内存泄漏和内存溢出的例子么请举例及解决方案?
 */
public class MemoryLeakAndOverflowTroubleshooting {

    // 经典反面案例 1：静态集合类导致的内存泄漏根源
    private static final List<Object> LEAK_CONTAINER = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("      内存泄漏 vs 内存溢出、JVM 各区域 OOM 场景与排障 SOP 深度解析      ");
        System.out.println("======================================================================");

        explainLeakVsOverflowDifference();
        explainJvmMemoryOomCategories();
        demonstrateStackOverflowError();
        demonstrateMemoryLeakScenario();
        explainHeapDumpAndTroubleshootingSop();

        System.out.println("======================================================================");
        System.out.println("               内存泄漏、内存溢出与排障解决方案解析完成               ");
        System.out.println("======================================================================");
    }

    /**
     * 问题 1：内存泄漏和内存溢出的理解?
     *
     * 概念、区别与因果关系。
     */
    public static void explainLeakVsOverflowDifference() {
        System.out.println("\n--- 1. 内存泄漏（Memory Leak）与 内存溢出（Memory Overflow）深度对比 ---");
        System.out.println("一、内存泄漏（Memory Leak）：");
        System.out.println("   - 定义：程序在申请内存后，对象已经【不再被实际业务逻辑所使用】，");
        System.out.println("     但由于仍然存在一条有效的【GC Roots 强引用链】指向该对象，导致垃圾收集器（GC）无法对其进行回收。");
        System.out.println("   - 形象比喻：家里的水龙头持续滴水，虽然水池没满，但水一直在悄悄流失。");

        System.out.println("\n二、内存溢出（Memory Overflow / OutOfMemoryError）：");
        System.out.println("   - 定义：程序在申请内存时，JVM 没有足够的可用物理或虚拟内存空间供其分配对象实例，从而抛出 OutOfMemoryError。");
        System.out.println("   - 形象比喻：水池里的水彻底满溢出来，直接瘫痪。");

        System.out.println("\n三、两者的辩证因果关系（核心面试要点）：");
        System.out.println("   1. 内存泄漏是引起内存溢出的最常见【诱因之一】：持续的内存泄漏会导致可用堆空间越来越少，最终必将酿成内存溢出。");
        System.out.println("   2. 内存溢出【不一定】是由内存泄漏引起的：");
        System.out.println("      - 例如：突发大促瞬间涌入百万请求，接口未分页一次性从数据库查出 100 万条订单数据生成报表；");
        System.out.println("      - 此时这些数据都是合法正在使用的，没有任何内存泄漏，纯粹是因为单次内存申请量超过了 JVM -Xmx 堆上限！");
    }

    /**
     * 问题 2：jvm内存结构有哪几种内存溢出的情况?
     *
     * 对应 JVM 各区域的 OOM 类型。
     */
    public static void explainJvmMemoryOomCategories() {
        System.out.println("\n--- 2. JVM 运行时各内存区域所发生的溢出类型全景 ---");
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.println("内存区域        | 溢出异常类型                                     | 典型触发诱因与场景");
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.println("Java 堆 (Heap)  | java.lang.OutOfMemoryError: Java heap space     | 对象持续增长无释放、超大数组分配、一次性全量读库");
        System.out.println("                | java.lang.OutOfMemoryError: GC overhead limit...| 98% 时间在做 GC 但回收不到 2% 堆空间");
        System.out.println("虚拟机栈/本地栈 | java.lang.StackOverflowError                    | 递归深度过深、循环引用、无限自我调用");
        System.out.println("                | java.lang.OutOfMemoryError: unable to create... | 线程创建过多，OS 内存不足无法分配新线程栈空间");
        System.out.println("元空间(Metaspace| java.lang.OutOfMemoryError: Metaspace           | CGLIB 频繁动态生成类、Groovy 脚本动态加载未卸载");
        System.out.println("直接内存/堆外   | java.lang.OutOfMemoryError: Direct buffer memory| NIO DirectByteBuffer/Netty 堆外内存分配超限且未回收");
        System.out.println("------------------------------------------------------------------------------------------------");
    }

    /**
     * 问题 4：栈溢出的情况呢?（现场实测无限递归并优雅捕获）
     */
    public static void demonstrateStackOverflowError() {
        System.out.println("\n--- 3. 栈溢出（StackOverflowError）真实发生机制与实测 ---");
        System.out.println("1. 栈溢出的根源：");
        System.out.println("   - 每个线程拥有独立的虚拟机栈，每个方法调用在栈中生成一个栈帧。");
        System.out.println("   - JVM 参数 -Xss（如 -Xss1m）规定了单个线程虚拟机栈的最大深度/空间上限。");
        System.out.println("   - 当方法调用链层级过深，新栈帧无法再压入栈时，JVM 直接抛出 java.lang.StackOverflowError！");

        System.out.println("2. [代码现场实测] 触发并捕获 StackOverflowError，统计溢出时的最大栈深度：");
        try {
            triggerRecursiveCall(1);
        } catch (StackOverflowError error) {
            System.out.println("   [成功捕获 StackOverflowError]");
            System.out.println("   错误原因: 方法调用栈层级超出 -Xss 限制。");
        }

        System.out.println("3. 栈溢出的生产常见场景与排查解决方案：");
        System.out.println("   场景 A：递归调用没有写好递归基（递归出口条件），或者递归出口条件永远无法满足；");
        System.out.println("     - 解决方案：检查递归代码，确保严格的递归终止条件；大型树形计算改用迭代算法（循环 + 显式栈/队列数据结构）。");
        System.out.println("   场景 B：对象循环嵌套序列化（如 Jackson/Fastjson 在双向关联实体类序列化时互相调用陷入死循环）；");
        System.out.println("     - 解决方案：在循环引用属性上增加 @JsonIgnore 或切断双向关联。");
        System.out.println("   场景 C：Spring 循环依赖使用构造器注入（导致 Bean 创建死循环）；");
        System.out.println("     - 解决方案：改用 Setter/Field 注入，或引入 @Lazy 懒加载解耦。");
    }

    private static void triggerRecursiveCall(int depth) {
        if (depth % 5000 == 0) {
            System.out.println("   当前递归调用栈深度已达: " + depth);
        }
        triggerRecursiveCall(depth + 1);
    }

    /**
     * 问题 5：有具体的内存泄漏和内存溢出的例子么请举例及解决方案?
     *
     * 案例 1：静态集合类导致的内存泄漏。
     * 案例 2：ThreadLocal 未 remove() 导致的内存泄漏。
     */
    public static void demonstrateMemoryLeakScenario() {
        System.out.println("\n--- 4. 具体内存泄漏典型案例剖析与修复策略 ---");

        System.out.println("案例一：静态集合类持续追加对象引发的内存泄漏");
        System.out.println("   - 错误代码模式：private static final List<Object> LEAK_CONTAINER = new ArrayList<>();");
        System.out.println("   - 泄漏原理：静态变量存储在类元数据中，生命周期与 ClassLoader / JVM 进程同生共死。");
        System.out.println("     不断向静态容器存入临时数据，GC Roots 强引用永远无法断开，对象永远无法被 GC 回收！");

        // 演示存入数据后主动清理的标准修复规范
        for (int i = 0; i < 5; i++) {
            LEAK_CONTAINER.add(new byte[1024 * 10]); // 模拟业务临时缓存
        }
        System.out.println("   [模拟存入临时数据] LEAK_CONTAINER 当前大小 = " + LEAK_CONTAINER.size());

        // 标准整改代码：使用完毕或定期执行清理
        LEAK_CONTAINER.clear();
        System.out.println("   [标准修复：执行 clear() 断开引用] 清理后容器大小 = " + LEAK_CONTAINER.size() + " (对象失去强引用，可被正常 GC 回收)");

        System.out.println("\n案例二：ThreadLocal 搭配线程池引发的内存泄漏（最隐蔽的生产事故点）");
        System.out.println("   - 错误代码模式：线程执行任务时 threadLocal.set(bigData)，任务结束后未调用 remove()。");
        System.out.println("   - 泄漏原理：线程池中的核心线程（Core Thread）是常驻存活、循环复用的；");
        System.out.println("     ThreadLocalMap 中的 Entry 的 Key 虽然是弱引用被 GC 置为 null，但 Value 是强引用，依然挂在常驻线程身上！");
        System.out.println("   - 标准修复规范（业界铁律）：");
        System.out.println("     ThreadLocal<UserContext> context = new ThreadLocal<>();");
        System.out.println("     try {");
        System.out.println("         context.set(currentUser);");
        System.out.println("         doBusinessOperation();");
        System.out.println("     } finally {");
        System.out.println("         context.remove(); // 必须在 finally 块中显式 remove！");
        System.out.println("     }");

        System.out.println("\n案例三：资源流未关闭导致堆外句柄泄漏（如 FileInputStream, Socket, Connection）");
        System.out.println("   - 标准修复规范：使用 Java 7+ 提供的 try-with-resources 语法糖自动调用 close()，杜绝泄漏。");
    }

    /**
     * 问题 3：遇到过堆溢出的情况吗？如何解决?
     *
     * 企业级排查排障全流程（SOP）：从快照配置、MAT 支配树分析、GC Roots 追溯到代码优化与参数调整。
     */
    public static void explainHeapDumpAndTroubleshootingSop() {
        System.out.println("\n--- 5. 堆内存溢出（Java heap space）企业级排障 SOP 黄金标准流程 ---");
        System.out.println("如果在生产环境中突发 java.lang.OutOfMemoryError: Java heap space，标准解决四步法：");
        System.out.println("步骤一：事前防御（开启 JVM 自动保留事故第一现场配置）");
        System.out.println("   - 在生产启动脚本中务必加入核心诊断参数：");
        System.out.println("     -XX:+HeapDumpOnOutOfMemoryError");
        System.out.println("     -XX:HeapDumpPath=/data/logs/jvm-heapdump.hprof");
        System.out.println("   - 当发生 OOM 崩溃瞬间，JVM 会自动将当前的完整堆内存打出 hprof 快照镜像！");

        System.out.println("\n步骤二：事中定位（使用专业工具加载分析堆快照）");
        System.out.println("   1. 工具选型：Eclipse MAT（Memory Analyzer Tool）、JProfiler、Arthas、JDK 自带 VisualVM。");
        System.out.println("   2. 关键视图 1：查看【Leak Suspects（内存泄漏疑点报告）】，MAT 会自动计算出占比最大的内存元凶。");
        System.out.println("   3. 关键视图 2：查看【Dominator Tree（支配树）】，按 Retained Heap（深堆大小，即释放该对象可回收的内存总量）倒序排序，找出占用几十兆或几百兆的大对象。");
        System.out.println("   4. 关键视图 3：追溯【Path to GC Roots】（排除 weak/soft/phantom 弱引用），查看究竟是哪条强引用链抓住了这些大对象导致无法释放。");

        System.out.println("\n步骤三：事后根治（对症下药修复代码与配置）");
        System.out.println("   - 情况 A：经分析属于【内存泄漏】");
        System.out.println("     - 定位到具体的业务代码，消除未释放的强引用（如补充调用 remove/clear、解除静态监听器、修改缓存框架引入 LRU 淘汰策略）。");
        System.out.println("   - 情况 B：经分析属于【突发高并发或大对象正常溢出】");
        System.out.println("     - 业务代码调优：避免一次性全量读取数据库（强制分页查库）、大文件上传下载使用流式处理（如 EasyExcel 替代传统 POI）；");
        System.out.println("     - 架构优化：增加流控降级（Sentinel 限流）、引入消息队列削峰填谷；");
        System.out.println("     - JVM 参数调整：在物理机资源允许的情况下，适当调大 -Xms 与 -Xmx（如从 2G 调整至 4G 或 8G），并设置 -Xms 与 -Xmx 相同大小，避免频繁堆伸缩。");
    }
}
