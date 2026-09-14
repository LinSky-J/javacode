package set;

/**
 * Java Set 面试题全景深度剖析与实战演练
 *
 * 逐字保留面试问题：
 * 1. Java 集合中 List 和 Set区别是什么？
 * 2. 如何对Set排序?
 * 3. Set集合有什么特点？如何实现key无重复的?
 * 4. 有序的Set是什么？记录插入顺序的集合是什么?
 */
public class SetInterviewQuestionsExplanation {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("问题 1: Java 集合中 List 和 Set区别是什么？");
        System.out.println("================================================================================");
        explainListVsSet();

        System.out.println("\n================================================================================");
        System.out.println("问题 2: 如何对Set排序?");
        System.out.println("================================================================================");
        explainSetSorting();

        System.out.println("\n================================================================================");
        System.out.println("问题 3: Set集合有什么特点？如何实现key无重复的?");
        System.out.println("================================================================================");
        explainSetDeduplication();

        System.out.println("\n================================================================================");
        System.out.println("问题 4: 有序的Set是什么？记录插入顺序的集合是什么?");
        System.out.println("================================================================================");
        explainOrderedAndInsertionOrderSets();
    }

    private static void explainListVsSet() {
        //1. java集合中List和Set的区别是什么？
        System.out.println("【理论解答】");
        System.out.println("1. 重复性：List 允许重复元素，允许多个 null；Set 元素唯一不重复，HashSet/LinkedHashSet 最多一个 null，TreeSet 禁存 null。");
        System.out.println("2. 顺序性：List 保证插入顺序；Set 总体无序（HashSet 无序，TreeSet 大小排序，LinkedHashSet 记录插入顺序）。");
        System.out.println("3. 访问机制：List 支持基于整数下标的随机访问 get(i)；Set 无索引概念，只能通过迭代器或 contains() 检索。");
        System.out.println("4. 底层结构：List 直接基于 Object[] 数组或双向链表；Set 底层全部包装对应的 Map（HashSet 包装 HashMap，TreeSet 包装 TreeMap）。");
        System.out.println("5. 应用场景：List 适用于有序数据序列与分页对象；Set 适用于去重、黑白名单过滤与数学集合操作（并集、交集、差集）。");

        System.out.println();
        ListVsSetComparison.demonstrateListVsSet();
    }

    //2.如何对Set进行排序
    private static void explainSetSorting() {
        System.out.println("【理论解答】");
        System.out.println("对 Set 集合排序有四大核心策略：");
        System.out.println("1. 策略一（转为 List 排序）：使用 new ArrayList<>(set) 转换为 List，调用 Collections.sort(list) 或 list.sort()。");
        System.out.println("2. 策略二（TreeSet 自动排序）：使用基于红黑树的 TreeSet，通过实现 Comparable 接口或构造传入 Comparator 自动保持有序。");
        System.out.println("3. 策略三（Stream API 流式排序并收集为 LinkedHashSet）：");
        System.out.println("   set.stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new));");
        System.out.println("   注意：必须收集为 LinkedHashSet 以保留排序结果，若收集回普通 HashSet 排序结果会再次被打乱。");
        System.out.println("4. 策略四（构造器导入）：直接使用 new TreeSet<>(originalSet) 初始化。");

        System.out.println();
        SetSortingStrategies.demonstrateSetSorting();
    }

    //Set集合有什么特点？如何事项key无重复的？
    private static void explainSetDeduplication() {
        System.out.println("【理论解答】");
        System.out.println("1. Set 集合的核心特点：元素唯一不可重复、无整数下标索引、底层依托 Map 键集合实现。");
        System.out.println("2. HashSet 如何实现 Key 无重复的？（源码级三层比对机制）：");
        System.out.println("   - HashSet 内部包装了 HashMap，元素存为 Map 的 Key，Value 共享全局静态对象 PRESENT。");
        System.out.println("   - 调用 add(e) 即调用 map.put(e, PRESENT)。");
        System.out.println("   - 在 putVal() 内部，先计算 hash = (h = key.hashCode()) ^ (h >>> 16)，再根据 (n - 1) & hash 定位数组桶。");
        System.out.println("   - 若桶内有节点，执行关键校验：");
        System.out.println("     if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))");
        System.out.println("     * 第 1 步：比较两对象的 hash 整数值是否相等；");
        System.out.println("     * 第 2 步：若 hash 相等，比较内存引用地址 k == key 是否同一对象；");
        System.out.println("     * 第 3 步：若引用不同，最后调用 key.equals(k) 校验业务值是否逻辑相等。");
        System.out.println("   - 若判定重复，put() 返回旧值 PRESENT，add() 返回 false 拒绝重复写入，实现去重。");
        System.out.println("3. 为什么必须同时重写 hashCode 和 equals？");
        System.out.println("   - 若漏写 hashCode：两相同对象哈希码不同，落入不同数组槽位，无法触发 equals 比对，去重失效！");
        System.out.println("   - 若漏写 equals：两相同对象哈希码相同落入同桶，但 Object.equals() 比较内存地址返回 false，去重同样失效！");
        System.out.println("4. TreeSet 的去重机制：不依赖 hashCode() 和 equals()，完全由 compareTo() 或 compare() == 0 决定！");

        System.out.println();
        SetDeduplicationPrinciple.demonstrateDeduplicationMechanisms();
    }
    //4.有序的Set是什么？记录插入顺序的结合是什么？
    private static void explainOrderedAndInsertionOrderSets() {
        System.out.println("【理论解答】");
        System.out.println("必须区分【大小排序顺序（Sorted Order）】与【插入物理顺序（Insertion Order）】：");
        System.out.println("1. 有序的 Set 是什么？—— TreeSet（SortedSet / NavigableSet 体系）");
        System.out.println("   - 底层为红黑树（TreeMap 实现），元素时刻按照自然大小（Comparable）或定制比较器（Comparator）保持有序。");
        System.out.println("   - 支持丰富导航操作：first()、last()、lower()、higher()、subSet() 范围截取，时间复杂度稳定 O(log n)。");
        System.out.println("   - 高并发场景下对应的线程安全实现为 ConcurrentSkipListSet（基于跳表）。");
        System.out.println("2. 记录插入顺序的集合是什么？—— LinkedHashSet（以及 List 体系）");
        System.out.println("   - 在 Set 中专指 LinkedHashSet：");
        System.out.println("     * 底层基于 LinkedHashMap 实现（哈希表 + 双向链表）；");
        System.out.println("     * 在哈希桶节点间维护一条双向链表，迭代遍历时严格按照节点插入的先后时间顺序输出；");
        System.out.println("     * 兼具 HashSet 的 O(1) 去重定位性能与 List 的保序性。");
        System.out.println("   - 在广义集合中：List（ArrayList/LinkedList）与 LinkedHashMap 也严格记录插入顺序；");
        System.out.println("   - 线程安全场景：CopyOnWriteArraySet（基于写时复制，自动去重且保留插入顺序）。");

        System.out.println();
        OrderedSetAndInsertionOrderExplorer.demonstrateOrderedAndInsertionSets();
    }
}
