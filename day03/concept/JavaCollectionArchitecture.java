package concept;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 题目：说说Java中的集合?
 *
 * 核心架构与体系深度剖析：
 * Java 集合框架（JCF）主要划分为两大核心派系：
 *
 * 一、Collection 接口体系（单列集合根接口）
 * 1. List 接口：元素有序（存储顺序与取出顺序一致）、有整数索引、允许重复元素。
 *    - ArrayList：底层为 Object[] 动态数组。连续内存，随机访问 O(1)，尾部追加 O(1)，中间增删需移动元素 O(n)。非线程安全。
 *    - LinkedList：底层为双向链表。首尾插入/删除 O(1)，不支持随机访问，按索引遍历 O(n)。实现 Deque 接口。
 *    - Vector / Stack：底层也是数组，核心方法加 synchronized 修饰，全表同步锁，性能低下，属于历史遗留类。
 *
 * 2. Set 接口：元素无索引、不允许重复。
 *    - HashSet：底层直接基于 HashMap 实现（只使用 key，value 是一个共享静态虚拟 Object PRESENT）。查询与插入平均 O(1)，无序。
 *    - LinkedHashSet：底层基于 LinkedHashMap 实现，在哈希表基础上维护双向链表，保证元素的遍历顺序与插入顺序完全一致。
 *    - TreeSet：底层基于红黑树（TreeMap 实现），支持元素自然排序（Comparable）或定制排序（Comparator），增删查时间复杂度 O(log n)。
 *
 * 3. Queue / Deque 接口：队列（FIFO）与双端队列。
 *    - ArrayDeque：底层基于循环数组，无容量限制，作为队列和栈的综合性能优于 LinkedList 与 Stack，高并发外单线程首选。
 *    - PriorityQueue：基于二叉小顶堆（默认）或大顶堆实现的无界优先级队列，元素按优先级出队，非 FIFO。
 *
 * 二、Map 接口体系（双列键值对根接口）
 * 存储 Key-Value 映射关系，Key 唯一且不可重复，Value 允许重复。
 * 1. HashMap：
 *    - JDK 1.8 架构：数组 + 单向链表 + 红黑树。
 *    - 树化阈值：当链表长度 >= 8 且数组总容量 >= 64 时，链表转为红黑树；当红黑树节点数 <= 6 时退化为链表。
 *    - 允许一个 key 为 null，允许多个 value 为 null。线程不安全。
 * 2. LinkedHashMap：
 *    - 继承自 HashMap，为每个 Entry 增加 before 与 after 双向指针。
 *    - 支持按“插入顺序”或“访问顺序（accessOrder=true）”排序，常用于实现本地 LRU（最近最少使用）缓存。
 * 3. TreeMap：
 *    - 底层基于红黑树实现，所有 Key 按照排序规则排列，支持 subMap、firstKey、lastKey 等范围检索 API。
 * 4. Hashtable：
 *    - 遗留类，方法全加 synchronized，Key 和 Value 均不允许为 null，性能差，已被 ConcurrentHashMap 完全替代。
 */
public class JavaCollectionArchitecture {

    public static void demonstrateArchitectureOverview() {
        System.out.println("--- Java 集合体系典型接口与数据结构验证 ---");

        // 1. List: 有序可重复
        List<String> arrayList = new ArrayList<>();
        arrayList.add("Java");
        arrayList.add("Python");
        arrayList.add("Java"); // 允许重复
        System.out.println("1. List (ArrayList) 有序且允许重复: " + arrayList);

        // 2. Set: 去重
        Set<String> hashSet = new HashSet<>(arrayList);
        System.out.println("2. Set (HashSet) 自动去重: " + hashSet);

        // TreeSet: 红黑树排序
        Set<Integer> treeSet = new TreeSet<>();
        treeSet.add(80);
        treeSet.add(50);
        treeSet.add(95);
        System.out.println("3. Set (TreeSet) 红黑树自动排序: " + treeSet);

        // 3. Queue / Deque: 双端队列与堆
        Deque<String> stackOrQueue = new ArrayDeque<>();
        stackOrQueue.push("First");
        stackOrQueue.push("Second");
        System.out.println("4. Deque (ArrayDeque) 栈顶弹出: " + stackOrQueue.pop() + ", 剩余: " + stackOrQueue);

        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        minHeap.add(30);
        minHeap.add(10);
        minHeap.add(20);
        System.out.println("5. Queue (PriorityQueue) 小顶堆出队最小值: " + minHeap.poll());

        // 4. Map: 键值对映射
        Map<String, Integer> map = new HashMap<>();
        map.put("CPU", 8);
        map.put("Memory", 16);
        System.out.println("6. Map (HashMap) 键值对映射: " + map);

        // LinkedHashMap: 保持插入顺序
        Map<String, String> linkedMap = new LinkedHashMap<>();
        linkedMap.put("K1", "V1");
        linkedMap.put("K2", "V2");
        System.out.println("7. Map (LinkedHashMap) 维持插入次序: " + linkedMap.keySet());
    }
}
