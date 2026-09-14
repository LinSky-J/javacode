package set;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 题目：有序的Set是什么？记录插入顺序的集合是什么?
 *
 * 核心考点与理论剖析：
 *
 * 面试关键辨析：必须向面试官明确区分【两种截然不同的“有序”概念】：
 * 概念一：大小排序顺序（Sorted Order）—— 元素按照自然大小或比较器规则自动排列（如 1, 3, 5, 9）。
 * 概念二：插入发生顺序（Insertion Order）—— 元素按照代码被写入集合的先后时间顺序排列（先存先出）。
 *
 * 一、有序的 Set 是什么？—— TreeSet（SortedSet / NavigableSet 体系）
 * 1. 核心类：java.util.TreeSet。
 * 2. 接口体系：实现 NavigableSet 接口，继承自 SortedSet 接口。
 * 3. 底层实现：基于红黑树（Red-Black Tree，自平衡二叉查找树，复用 TreeMap 实现）。
 * 4. 排序规则：
 *    - 元素按照自然排序（Comparable）或构造时指定的定制比较器（Comparator）排列，增删查稳定在 O(log n)。
 * 5. 丰富的高级导航检索 API：
 *    - first() / last()：获取集合首尾极值；
 *    - lower(e) / higher(e)：查找严格小于/大于给定元素的最接近值；
 *    - floor(e) / ceiling(e)：查找小于等于/大于等于给定元素的最接近值；
 *    - subSet(from, to)：范围子集区间截取。
 * 6. 线程安全并发版本：
 *    - ConcurrentSkipListSet：基于跳表（SkipList）算法实现的高并发无锁有序 Set。
 *
 * 二、记录插入顺序的集合是什么？—— LinkedHashSet（及 List 体系）
 * 1. 针对 Set 集合：专指 java.util.LinkedHashSet。
 * 2. 底层架构原理（哈希表 + 双向链表）：
 *    - LinkedHashSet 继承自 HashSet，底层基于 LinkedHashMap 实现。
 *    - 空间布局：底层依然包含哈希数组（保证 O(1) 的去重和高速定位），同时为每个节点增加 before 和 after 引用；
 *    - 贯穿链表：在所有哈希节点之间额外维护了一条双向链表，记录每个元素插入的物理次序；
 *    - 遍历机制：迭代器遍历时，并非扫描哈希桶数组，而是直接顺着双向链表的 head 指针向 tail 顺序遍历，
 *      从而精确保留并还原了元素的插入次序（Insertion Order）。
 * 3. 针对广义 Collection 集合：
 *    - 单列列表体系：ArrayList、LinkedList 均天然根据整数索引严格记录插入顺序；
 *    - 双列映射体系：LinkedHashMap 记录键值对的插入顺序（或访问顺序）；
 *    - 线程安全集合：CopyOnWriteArraySet（底层基于 CopyOnWriteArrayList，具备去重并保留插入顺序的特征）。
 */
public class OrderedSetAndInsertionOrderExplorer {

    public static void demonstrateOrderedAndInsertionSets() {
        System.out.println("--- 有序 Set (TreeSet) 与 记录插入顺序集合 (LinkedHashSet) 实测 ---");

        // 1. 无序对比基准：HashSet（哈希散列，不保证任何顺序）
        Set<String> hashSet = new HashSet<>();
        hashSet.add("Node_30");
        hashSet.add("Node_10");
        hashSet.add("Node_50");
        hashSet.add("Node_20");
        System.out.println("1. HashSet (无序散列，遍历顺序与插入无关): " + hashSet);

        // 2. 有序的 Set：TreeSet（按元素大小/规则排序）
        TreeSet<Integer> treeSet = new TreeSet<>();
        treeSet.add(30);
        treeSet.add(10);
        treeSet.add(50);
        treeSet.add(20);
        System.out.println("2. 【有序的 Set】TreeSet (红黑树按大小排序): " + treeSet);
        System.out.println("   TreeSet 导航操作: 最小值 first()=" + treeSet.first() + ", 最大值 last()=" + treeSet.last());
        System.out.println("   小于 30 的严格最大值 lower(30)=" + treeSet.lower(30) + ", 大于 30 的严格最小值 higher(30)=" + treeSet.higher(30));
        System.out.println("   区间子集 subSet(10, true, 30, true)=" + treeSet.subSet(10, true, 30, true));

        // 3. 记录插入顺序的 Set：LinkedHashSet（严格保序）
        Set<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("First");
        linkedHashSet.add("Second");
        linkedHashSet.add("Third");
        linkedHashSet.add("Fourth");
        System.out.println("3. 【记录插入顺序的 Set】LinkedHashSet (双向链表保证插入顺序): " + linkedHashSet);

        // 4. 并发场景下的对应线程安全集合
        // A. 线程安全有序 Set: ConcurrentSkipListSet
        ConcurrentSkipListSet<Integer> skipListSet = new ConcurrentSkipListSet<>();
        skipListSet.add(88);
        skipListSet.add(12);
        skipListSet.add(45);
        System.out.println("4. 线程安全有序 Set (ConcurrentSkipListSet 基于跳表): " + skipListSet);

        // B. 线程安全记录插入顺序的 Set: CopyOnWriteArraySet
        CopyOnWriteArraySet<String> cowSet = new CopyOnWriteArraySet<>();
        cowSet.add("Order1");
        cowSet.add("Order2");
        cowSet.add("Order3");
        System.out.println("5. 线程安全记录插入顺序 Set (CopyOnWriteArraySet 写时复制): " + cowSet);
    }
}
