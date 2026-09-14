package day02.java21;

/**
 * 面试专题：Java 21 长期支持版（LTS）划时代核心新特性深度全景剖析。
 *
 * 本类对应面试核心题目：
 * 1. Java 21 新特性知道哪些?
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class Java21FeaturesExplanation {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("      Java 21 (LTS) 核心新特性全景解析 与 下一代企业并发架构演进      ");
        System.out.println("======================================================================");

        explainJava21Features();

        System.out.println("\n======================================================================");
        System.out.println("        Java 21 体系与架构解析完毕，请细读类中源码与详细注释          ");
        System.out.println("======================================================================");
    }

    /**
     * 核心题目：Java 21 新特性知道哪些?
     *
     * 面试核心考点：
     * 1. 明确背景：Java 21（2023年9月发布）是继 Java 8 和 Java 17 之后的划时代 LTS 长期支持版。
     * 2. 能够精准展开最核心的 5 大重磅工业级特性：
     *    - 虚拟线程（Virtual Threads - JEP 444）
     *    - 分代 ZGC（Generational ZGC - JEP 439）
     *    - switch 模式匹配（Pattern Matching for switch - JEP 441）
     *    - 记录模式解构（Record Patterns - JEP 440）
     *    - 有序集合框架（Sequenced Collections - JEP 431）
     * 3. 理解并发与垃圾回收底层的演进逻辑与性能提升。
     */
    public static void explainJava21Features() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("核心题目：Java 21 新特性知道哪些?");
        System.out.println("--------------------------------------------------");

        /*
         * 【特性一：虚拟线程（Virtual Threads - JEP 444，重中之重）】：
         * 1. 背景：Project Loom 历时数年研发的正式落地成果。
         * 2. 改变：颠覆了传统 Java 1:1 的 OS 内核平台线程绑定模式。
         *    提供了由 JVM 在用户空间调度的轻量级线程（M:N 调度）。
         * 3. 内存与并发量：
         *    - 平台线程：单个栈预分配 ~1MB，万级并发崩溃。
         *    - 虚拟线程：每个仅占几百字节（分配在堆上），单机可开启数百万个。
         * 4. 阻塞优化：遇到 Socket I/O、JDBC 查询、Thread.sleep 等阻塞操作时，
         *    虚拟线程会自动与底层的 Carrier 线程解除绑定（Unmount），待 I/O 事件准备完毕后再次挂载（Mount）恢复，
         *    使同步阻塞代码天然拥有异步非阻塞反应堆的高吞吐！
         * 5. API 语法示例：
         *    Thread.startVirtualThread(() -> { ... });
         *    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
         *        executor.submit(() -> doBusinessWork());
         *    }
         */
        System.out.println("1. [特性一：虚拟线程 Virtual Threads (JEP 444)]");
        VirtualThreadArchitecture.explainThreadingEvolution();

        /*
         * 【特性二：switch 模式匹配（Pattern Matching for switch - JEP 441）】：
         * 1. 背景：让 switch 从过去只能判断基本类型和枚举，跨越到能够对任意对象进行类型推导与卫语句判断。
         * 2. 语法特点：支持类型匹配，支持 when 条件子句，支持显式 null 处理。
         * 3. 语法示例：
         *    static String formatPattern(Object obj) {
         *        return switch (obj) {
         *            case Integer i -> String.format("整型: %d", i);
         *            case Long l    -> String.format("长整型: %d", l);
         *            case Double d  -> String.format("浮点型: %f", d);
         *            case String s when s.length() > 5 -> "超长字符串: " + s;
         *            case String s  -> "普通字符串: " + s;
         *            case null      -> "空指针安全处理";
         *            default        -> obj.toString();
         *        };
         *    }
         */
        System.out.println("\n2. [特性二：switch 模式匹配 Pattern Matching for switch (JEP 441)]");
        System.out.println("   - 彻底消除了深层繁重的 if-else 和 instanceof 强转代码块。");
        System.out.println("   - 支持 'case 类型 变量 when 表达式' 卫语句，支持原生 case null 防御 NPE。");

        /*
         * 【特性三：记录模式解构（Record Patterns - JEP 440）】：
         * 1. 作用：与 instanceof 和 switch 配合，直接对 Record 数据载体类进行一步解构赋值，
         *    无需再手动通过 point.x()、point.y() 逐个取值。
         * 2. 语法示例：
         *    record Point(int x, int y) {}
         *    if (obj instanceof Point(int x, int y)) {
         *        System.out.println("直接解构坐标点: x=" + x + ", y=" + y);
         *    }
         */
        System.out.println("\n3. [特性三：记录模式解构 Record Patterns (JEP 440)]");
        System.out.println("   - 允许嵌套解构复杂数据结构，极大强化了 Java 的函数式数据提取能力。");

        /*
         * 【特性四：分代 ZGC（Generational ZGC - JEP 439，企业级重磅）】：
         * 1. 背景：原 ZGC 是非分代低延迟垃圾收集器，虽然停顿时间在 1ms 内，但在高分配速率（High Allocation Rate）下 CPU 开销较大。
         * 2. 突破：结合“弱分代假说”（绝大多数对象都是朝生夕灭的），为 ZGC 引入了年轻代与老年代物理区分收集。
         * 3. 收益：
         *    - 极大减少内存分配停顿（Allocation Stalls）。
         *    - 大幅降低 GC 占用的后台 CPU 资源。
         *    - 在保持亚毫秒（< 1ms）最大停顿的前提下，成倍提升了吞吐量（Throughput）。
         * 4. JVM 启用参数：-XX:+UseZGC -XX:+ZGenerational
         */
        System.out.println("\n4. [特性四：分代 ZGC Generational ZGC (JEP 439)]");
        System.out.println("   - 解决超大规模企业堆（数十 GB 到数 TB）下兼顾'高吞吐'与'亚毫秒级（<1ms）超低停顿'的技术难题。");

        /*
         * 【特性五：有序集合体系（Sequenced Collections - JEP 431）】：
         * 1. 历史痛点：Java 集合框架历史悠久，但此前没有统一定义“有序集合”的顶层接口。
         *    例如：List 获取最后一个元素要 list.get(list.size() - 1)；
         *          Deque 是 deque.getLast()；
         *          LinkedHashSet 则只能通过迭代器遍历到尾部！
         * 2. 解决方案：引入了三大统一接口：
         *    - SequencedCollection（提供 getFirst, getLast, addFirst, addLast, reversed）
         *    - SequencedSet
         *    - SequencedMap
         */
        System.out.println("\n5. [特性五：有序集合体系 Sequenced Collections (JEP 431)]");
        System.out.println("   - 终结了集合框架 20 年来获取首尾元素 API 分裂的痛点，提供了统一的 getFirst() / getLast() / reversed()。");

        /*
         * 【特性六：结构化并发与作用域值（预览特性 Preview）】：
         * - 结构化并发（Structured Concurrency - JEP 453）：把由虚拟线程派生出的多任务组织成单一工作单元，
         *   任何一个子任务失败，自动取消其余子任务，杜绝并发线程泄露与孤儿任务。
         * - 作用域值（Scoped Values - JEP 446）：用于替代传统昂贵且易引起内存泄漏的 ThreadLocal，
         *   为大型虚拟线程池提供不可变、轻量级、跨线程共享数据的安全载体。
         */
        System.out.println("\n6. [特性六：结构化并发与作用域值 (JEP 453 & JEP 446)]");
        System.out.println("   - 打造企业级全链路高可靠并发范式，彻底替代易造成内存泄漏的 ThreadLocal。");
    }
}
