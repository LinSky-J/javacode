package map;

/**
 * Java Map 面试题全景深度剖析与实战演练
 *
 * 逐字保留全部面试问题：
 * 1. 如何对map进行快速遍历?
 * 2. HashMap实现原理介绍一下?
 * 3. HashMap链表发生转换后为什么不用平衡二叉树?
 * 4. 了解的哈希冲突解决方法有哪些?
 * 5. HashMap是线程安全的吗?
 * 6. 在 Java 的 hashmap 中 get一个元素的过程是怎样的?
 * 7. hashmap的put过程介绍一下
 * 8. HashMap的put(key,val)和get(key)过程
 * 9. hashmap 调用get方法一定安全吗?
 * 10. HashMap一般用什么做Key？为啥String适合做Key呢?
 * 11. 为什么HashMap要用红黑树而不是平衡二叉树?
 * 12. hashmap key可以为null吗?
 * 13. 重写HashMap的equal和hashcode方法需要注意什么?
 * 14. 重写HashMap的equal方法不当会出现什么问题?
 * 15. 列举HashMap在多线程下可能会出现的问题?
 * 16. HashMap的扩容机制介绍一下
 * 17. HashMap的大小为什么是2的n次方大小呢?
 * 18. 往hashmap存20个元素，会扩容几次?
 * 19. 说说hashmap的负载因子
 * 20. Hashmap和Hashtable有什么不一样的？Hashmap一般怎么用?
 * 21. ConcurrentHashMap怎么实现的？
 * 22. 分段锁怎么加锁的?
 * 23. 分段锁是可重入的吗?
 * 24. 已经用了synchronized，为什么还要用CAS呢？
 * 25. ConcurrentHashMap用了悲观锁还是乐观锁?
 * 26. HashTable 底层实现原理是什么？
 * 27. HashTable线程安全是怎么实现的？
 * 28. hashtable 和 concurrentHashMap有什么区别
 * 29. 说一下HashMap和Hashtable、ConcurrentMap的区别
 */
public class MapInterviewQuestionsExplanation {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("模块 1: Map 遍历方式与性能对比");
        System.out.println("包含原题：- 如何对map进行快速遍历?");
        System.out.println("================================================================================");
        explainMapTraversal();

        System.out.println("\n================================================================================");
        System.out.println("模块 2: HashMap 底层原理、put/get 详细执行时序与冲突解决");
        System.out.println("包含原题：");
        System.out.println("- HashMap实现原理介绍一下?");
        System.out.println("- 了解的哈希冲突解决方法有哪些?");
        System.out.println("- 在 Java 的 hashmap 中 get一个元素的过程是怎样的?");
        System.out.println("- hashmap的put过程介绍一下");
        System.out.println("- HashMap的put(key,val)和get(key)过程");
        System.out.println("- hashmap key可以为null吗?");
        System.out.println("================================================================================");
        explainHashMapInternalsAndPutGet();

        System.out.println("\n================================================================================");
        System.out.println("模块 3: 红黑树 vs 平衡二叉树 (AVL 树)");
        System.out.println("包含原题：");
        System.out.println("- HashMap链表发生转换后为什么不用平衡二叉树?");
        System.out.println("- 为什么HashMap要用红黑树而不是平衡二叉树?");
        System.out.println("================================================================================");
        explainRedBlackTreeVsAvl();

        System.out.println("\n================================================================================");
        System.out.println("模块 4: HashMap Key 设计与 equals/hashCode 规范");
        System.out.println("包含原题：");
        System.out.println("- HashMap一般用什么做Key？为啥String适合做Key呢?");
        System.out.println("- 重写HashMap的equal和hashcode方法需要注意什么?");
        System.out.println("- 重写HashMap的equal方法不当会出现什么问题?");
        System.out.println("================================================================================");
        explainKeyDesignAndHashContract();

        System.out.println("\n================================================================================");
        System.out.println("模块 5: 扩容机制、2的n次方容量与负载因子");
        System.out.println("包含原题：");
        System.out.println("- HashMap的扩容机制介绍一下");
        System.out.println("- HashMap的大小为什么是2的n次方大小呢?");
        System.out.println("- 往hashmap存20个元素，会扩容几次?");
        System.out.println("- 说说hashmap的负载因子");
        System.out.println("================================================================================");
        explainResizeAndCapacity();

        System.out.println("\n================================================================================");
        System.out.println("模块 6: HashMap 线程不安全隐患与多线程致命故障");
        System.out.println("包含原题：");
        System.out.println("- HashMap是线程安全的吗?");
        System.out.println("- hashmap 调用get方法一定安全吗?");
        System.out.println("- 列举HashMap在多线程下可能会出现的问题?");
        System.out.println("================================================================================");
        explainConcurrencyFailures();

        System.out.println("\n================================================================================");
        System.out.println("模块 7: HashTable 原理与三者全方位对比");
        System.out.println("包含原题：");
        System.out.println("- HashTable 底层实现原理是什么？");
        System.out.println("- HashTable线程安全是怎么实现的？");
        System.out.println("- Hashmap和Hashtable有什么不一样的？Hashmap一般怎么用?");
        System.out.println("- hashtable 和 concurrentHashMap有什么区别");
        System.out.println("- 说一下HashMap和Hashtable、ConcurrentMap的区别");
        System.out.println("================================================================================");
        explainHashTableAndComparison();

        System.out.println("\n================================================================================");
        System.out.println("模块 8: ConcurrentHashMap 深度原理、分段锁与 CAS+synchronized 协同");
        System.out.println("包含原题：");
        System.out.println("- ConcurrentHashMap怎么实现的？");
        System.out.println("- 分段锁怎么加锁的?");
        System.out.println("- 分段锁是可重入的吗?");
        System.out.println("- 已经用了synchronized，为什么还要用CAS呢？");
        System.out.println("- ConcurrentHashMap用了悲观锁还是乐观锁?");
        System.out.println("================================================================================");
        explainConcurrentHashMapDeepDive();
    }

    private static void explainMapTraversal() {
        System.out.println("【理论解答】");
        System.out.println("如何对 map 进行快速遍历？");
        System.out.println("1. 最快途径：使用 entrySet() 配合增强 for 循环或 Iterator。");
        System.out.println("   - 单次遍历直接获取 Node(Entry) 对象，同时拿到 Key 和 Value，时间复杂度为严格的 O(n)。");
        System.out.println("2. 最慢途径（反面陷阱）：使用 keySet() 并在循环内反查 map.get(key)。");
        System.out.println("   - 每一次 get 都要重新计算 hash 并遍历哈希桶/链表/红黑树，耗时成倍增加，大数据量下性能急剧下降。");
        System.out.println("3. 语法最简练：Java 8 的 map.forEach((k, v) -> ...)，底层基于 entrySet，兼具速度与高表现力。");

        System.out.println();
        MapTraversalPerformanceBenchmark.demonstrateTraversalMethods();
        MapTraversalPerformanceBenchmark.runBenchmark(100_000);
    }

    private static void explainHashMapInternalsAndPutGet() {
        System.out.println("【理论解答】");
        System.out.println("1. HashMap 实现原理：");
        System.out.println("   - JDK 1.8 采用 Node[] 数组 + 单向链表 + 红黑树架构；");
        System.out.println("   - 树化条件：链表长度 >= 8 且底层数组容量 >= 64；反树化条件：节点数 <= 6。");
        System.out.println("2. 哈希冲突四大解决方法：");
        System.out.println("   - 链地址法（拉链法，HashMap/Hashtable 采用）、开放定址法（ThreadLocalMap 线性探测）、再哈希法、公共溢出区法。");
        System.out.println("3. put(key, val) 过程：");
        System.out.println("   - 计算 hash = (h = key.hashCode()) ^ (h >>> 16)；");
        System.out.println("   - 根据 (n - 1) & hash 定位桶槽；桶空直接写入 Node；");
        System.out.println("   - 桶有冲突时，首节点匹配则覆盖；TreeNode 则插入红黑树；链表则尾插法遍历，若满 8 且表长 64 树化；");
        System.out.println("   - 最后 ++size > threshold 触发 resize()。");
        System.out.println("4. get(key) 过程：");
        System.out.println("   - 寻址后先直接比对首节点，首节点命中直接 O(1) 返回；");
        System.out.println("   - 首节点未命中：若是 TreeNode 则在红黑树内 O(log n) 查找；若是链表则顺着 next 线性遍历 O(k) 比对。");
        System.out.println("5. key 可以为 null 吗？");
        System.out.println("   - 可以！允许一个 null key，hash(null) 固定返回 0，永久存放在 table[0] 槽位。");

        System.out.println();
        HashMapInternalsAndPutGetFlow.demonstratePutGetFlowAndNullKey();
    }

    private static void explainRedBlackTreeVsAvl() {
        System.out.println("【理论解答】");
        System.out.println("为什么 HashMap 链表转换后用红黑树而不是平衡二叉树（AVL 树）？");
        System.out.println("1. 树高与查询性能：AVL 树是严格平衡树（高度差<=1），查询比较次数理论最少；但 HashMap 桶内只有十几个节点，");
        System.out.println("   红黑树与 AVL 树的查找比较次数最多只相差 1~2 次，纳秒级差距几乎可以忽略。");
        System.out.println("2. 自平衡调整维护开销：");
        System.out.println("   - AVL 树在频繁插入和删除时极易打破平衡，删除节点最坏需要 O(log n) 次连环旋转重平衡；");
        System.out.println("   - 红黑树是弱平衡树，通过节点颜色与弱平衡约束，插入最多只需 2 次旋转，删除最多只需 3 次旋转！");
        System.out.println("     其余绝大部分失衡仅需廉价的 O(1) 变色即可修复。");
        System.out.println("3. 总结：HashMap 作为增删查并重的高频动态容器，红黑树的综合性能折中表现远胜于 AVL 树。");

        System.out.println();
        HashMapRedBlackTreeVsAvlExplanation.displayComparisonTable();
    }

    private static void explainKeyDesignAndHashContract() {
        System.out.println("【理论解答】");
        System.out.println("1. HashMap 一般用什么做 Key？为啥 String 适合做 Key？");
        System.out.println("   - 最推荐使用不可变类 String、Integer 等；");
        System.out.println("   - String 适合的三大原因：");
        System.out.println("     A. 不可变性：String 是 final 的，创建后内部字符数组不可修改，哈希值绝对恒定不可篡改；");
        System.out.println("     B. 缓存哈希码：String 内部有 private int hash 字段，计算后永久缓存，后续调用直接返回，无需重复遍历字符；");
        System.out.println("     C. 严格规范重写了 equals 和 hashCode。");
        System.out.println("2. 使用可变对象做 Key 的致命后果：");
        System.out.println("   - 若对象存入后属性被修改，其 hashCode 改变，下次根据新哈希计算出错误的桶槽，");
        System.out.println("     导致存入的数据永远无法查出（返回 null），也无法删除，永久残留在 Map 中引发堆内存泄漏！");
        System.out.println("3. 重写 equals 和 hashCode 注意事项：");
        System.out.println("   - equals 相等，hashCode 必须相同；equals 比较的核心字段必须全部参与 hashCode 计算；");
        System.out.println("   - 漏写 hashCode 导致逻辑相等对象被误判为不同 Key 存入不同桶，get 返回 null；");
        System.out.println("   - 漏写 equals 导致相同桶内无法识别覆盖，重复元素堆积。");

        System.out.println();
        HashMapKeyDesignAndHashContract.demonstrateMutableKeyLeak();
    }

    private static void explainResizeAndCapacity() {
        System.out.println("【理论解答】");
        System.out.println("1. HashMap 的大小为什么是 2 的 n 次方？");
        System.out.println("   - 原因一：位运算替代取模。当 n 为 2^k 时，hash % n == hash & (n - 1)，位运算仅需 1 个时钟周期；");
        System.out.println("   - 原因二：散列分布均匀。n-1 的二进制低位全为 1，位与时可充分保留原 hash 的低位特征，避免桶位闲置和哈希冲突；");
        System.out.println("   - 原因三：扩容时只需检查最高位 hash & oldCap 是否为 0，为 0 原槽位不动，为 1 移动到 原槽位+oldCap，迁移极快。");
        System.out.println("2. 说说负载因子（Load Factor = 0.75）：");
        System.out.println("   - 空间与时间的黄金折中。若太大（如 1.0）冲突暴增链表变长；若太小（如 0.5）内存浪费 50% 且扩容频繁；");
        System.out.println("   - 根据泊松分布，0.75 下单个桶达到 8 个节点的概率低至千万分之六，兼顾空间与性能。");
        System.out.println("3. 往 hashmap 存 20 个元素，会扩容几次？");
        System.out.println("   - 默认无参构造 new HashMap<>()：");
        System.out.println("     * 第 1 次扩容：存入第 1 个元素触发惰性初始化，容量 0 -> 16，阈值 12；");
        System.out.println("     * 存入第 2~12 个元素：size <= 12，不扩容；");
        System.out.println("     * 第 2 次扩容：存入第 13 个元素，因 13 > 12，容量 16 翻倍至 32，阈值变为 24；");
        System.out.println("     * 存入第 14~20 个元素：size <= 24，不再扩容。");
        System.out.println("     * 结论：【共扩容 2 次】（1 次初始化分配 + 1 次真实翻倍扩容）。");

        System.out.println();
        HashMapResizeAndCapacityMechanism.demonstratePutting20Elements();
    }

    private static void explainConcurrencyFailures() {
        System.out.println("【理论解答】");
        System.out.println("1. HashMap 是线程安全的吗？");
        System.out.println("   - 绝对不是！无任何锁保护。");
        System.out.println("2. 调用 get 方法一定安全吗？");
        System.out.println("   - 不一定安全！");
        System.out.println("   - 并发写触发扩容时，读线程可能会读到正在迁移的脏数据或错误地返回 null；");
        System.out.println("   - 在 JDK 1.7 中并发扩容产生的环形链表会导致 get 方法死循环（CPU 100%）。");
        System.out.println("3. HashMap 在多线程下可能出现的故障：");
        System.out.println("   - 数据覆盖丢失（两线程同时发现槽位为空并并发写入）；");
        System.out.println("   - size 计数脏写丢失更新；");
        System.out.println("   - JDK 1.7 头插法扩容死循环与 CPU 100%（JDK 1.8 尾插法修复死循环）；");
        System.out.println("   - 迭代器 Fail-Fast 抛出 ConcurrentModificationException。");

        System.out.println();
        HashMapConcurrencyFailureModes.demonstrateConcurrentPutFailure();
    }

    private static void explainHashTableAndComparison() {
        System.out.println("【理论解答】");
        System.out.println("1. HashTable 底层原理与线程安全实现：");
        System.out.println("   - 底层为 Entry[] 数组 + 链表；在几乎所有方法上直接修饰 synchronized 实现同步；");
        System.out.println("   - 锁粒度为全表粗粒度对象锁，读读互斥、读写互斥，并发性能极差；");
        System.out.println("   - Key 和 Value 严禁为 null，否则直接抛 NPE；初始容量 11，扩容为 2n+1，除法取模寻址。");
        System.out.println("2. HashMap 和 Hashtable 有什么不一样？HashMap 一般怎么用？");
        System.out.println("   - 差异：线程安全（不安全 vs 全表锁）、Null 容忍（支持 1 个 null key vs 严禁 null）、扩容（16与2倍 vs 11与2n+1）；");
        System.out.println("   - HashMap 用法：单线程首选；根据预估数据量预分配容量 new HashMap<>(size / 0.75 + 1)；并发务必用 ConcurrentHashMap。");

        System.out.println();
        HashTableVsConcurrentHashMapDeepDive.demonstrateNullHandling();
        HashTableVsConcurrentHashMapDeepDive.displayMapEcosystemComparison();
    }

    private static void explainConcurrentHashMapDeepDive() {
        System.out.println("【理论解答】");
        System.out.println("1. ConcurrentHashMap 怎么实现的？");
        System.out.println("   - JDK 1.7：Segment[] 分段锁（继承 ReentrantLock）+ HashEntry[] 链表，默认并发度 16；");
        System.out.println("   - JDK 1.8+：彻底废弃 Segment，采用 Node[] 数组 + CAS + synchronized 锁桶节点 + volatile，锁粒度细化至单个桶。");
        System.out.println("2. 分段锁怎么加锁的？是可重入的吗？");
        System.out.println("   - 根据 key 哈希高位定位 Segment 下标，调用 segment.lock() 加锁；");
        System.out.println("   - 【绝对是可重入的】！因为 Segment 直接继承自 ReentrantLock。");
        System.out.println("3. 已经用了 synchronized，为什么还要用 CAS？");
        System.out.println("   - CAS 用于槽位为空时的“无冲突极速写入”：无需动用重量级互斥锁，几纳秒完成，零锁开销；");
        System.out.println("   - synchronized 用于发生哈希冲突时的“链表/红黑树复杂写”：加锁只锁当前桶首节点，不影响其他桶。");
        System.out.println("4. ConcurrentHashMap 用了悲观锁还是乐观锁？");
        System.out.println("   - 两者皆用，是两者的精妙融合体！");
        System.out.println("   - 乐观锁：读操作 get 完全无锁（volatile 可见性）；槽位为空时 CAS 写入；计数统计 CounterCell CAS 累加；");
        System.out.println("   - 悲观锁：发生哈希冲突时，使用 synchronized 锁定桶首节点。");
    }
}
