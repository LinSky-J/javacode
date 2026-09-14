package set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 题目：Java 集合中 List 和 Set区别是什么？
 *
 * 核心考点与理论剖析：
 *
 * 1. 元素重复性（Duplicates）：
 *    - List：允许元素重复。可以在列表中存储多个相同的对象引用，也允许多个 null 元素。
 *    - Set：元素唯一，不允许重复。HashSet 和 LinkedHashSet 最多仅允许一个 null 元素；
 *      TreeSet 在使用自然排序（Comparable）时严禁存入 null，否则会抛出 NullPointerException。
 *
 * 2. 顺序性（Order）：
 *    - List：严格保证元素的存取顺序（Insertion Order），每个元素都有固定对应的整数索引下标。
 *    - Set：总体上不以插入顺序为标准：
 *      * HashSet：完全无序（无索引，底层取决于哈希码计算的桶位与链表重排）；
 *      * LinkedHashSet：维护了一条贯穿所有节点的双向链表，能够精确记录并还原插入顺序；
 *      * TreeSet：按照元素的大小排序规则（Comparable/Comparator）进行排序，并非插入顺序。
 *
 * 3. 访问与检索方式（Access Mechanism）：
 *    - List：支持基于整数索引的精准快速随机访问：list.get(index)、list.set(index, element)。
 *    - Set：没有整数索引概念，无法通过下标访问元素。只能通过迭代器（Iterator）、增强 for 循环
 *      或 contains(element) 方法判断是否存在。
 *
 * 4. 底层实现架构（Underlying Data Structure）：
 *    - List：直接基于数据结构实现（ArrayList 底层是动态 Object[] 数组，LinkedList 底层是双向链表）。
 *    - Set：底层全部复用 Map 体系（HashSet 包装了 HashMap，LinkedHashSet 包装了 LinkedHashMap，TreeSet 包装了 TreeMap），
 *      Set 的元素全部作为 Map 的 Key 存储，Value 统一使用一个静态虚拟 Object 对象占位。
 *
 * 5. 应用场景对比：
 *    - List：适用于存储具备明确先后次序、需要根据索引检索或分页、允许重复数据的业务数据流（如订单列表、评论流、分页 DTO）。
 *    - Set：适用于数据去重、快速成员资格测试（contains 时间复杂度为 O(1)）、数学集合运算（并集、交集、差集）等。
 */
public class ListVsSetComparison {

    public static void demonstrateListVsSet() {
        System.out.println("--- List 与 Set 核心特性对比实测 ---");

        // 1. 重复性与 Null 值对比
        List<String> list = new ArrayList<>();
        list.add("Java");
        list.add("Python");
        list.add("Java"); // 允许重复
        list.add(null);
        list.add(null);   // 允许多个 null
        System.out.println("1. List 允许重复元素与多个 null: size=" + list.size() + ", 内容=" + list);

        Set<String> set = new HashSet<>();
        set.add("Java");
        set.add("Python");
        boolean duplicateAdded = set.add("Java"); // 添加重复元素返回 false
        set.add(null);
        set.add(null);   // 重复 null 被覆盖
        System.out.println("2. Set 自动去重且最多允许一个 null: size=" + set.size() + ", 重复添加返回=" + duplicateAdded + ", 内容=" + set);

        // 2. 索引访问对比
        System.out.println("3. List 支持基于整数下标的随机访问 list.get(0): " + list.get(0));
        System.out.println("   Set 不支持索引访问，只能通过 contains() 或迭代器判定: set.contains(\"Python\") -> " + set.contains("Python"));

        // 3. 数学集合运算演示（Set 专有优势）
        Set<Integer> setA = new HashSet<>(Arrays.asList(1, 2, 3, 4));
        Set<Integer> setB = new HashSet<>(Arrays.asList(3, 4, 5, 6));

        // 交集 (retainAll)
        Set<Integer> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        System.out.println("4. Set 交集运算 setA ∩ setB: " + intersection);

        // 并集 (addAll)
        Set<Integer> union = new HashSet<>(setA);
        union.addAll(setB);
        System.out.println("5. Set 并集运算 setA ∪ setB: " + union);

        // 差集 (removeAll)
        Set<Integer> difference = new HashSet<>(setA);
        difference.removeAll(setB);
        System.out.println("6. Set 差集运算 setA - setB: " + difference);
    }
}
