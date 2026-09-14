package set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 题目：如何对Set排序?
 *
 * 核心考点与理论剖析：
 * 由于 HashSet 本身是无序的，无法直接调用 Collections.sort(set)（Collections.sort 仅接收 List 类型的参数）。
 * 对 Set 排序主要有以下四大标准策略：
 *
 * 策略一：转换为 List 排序（最通用、支持多种临时排序规则）
 * - 步骤：
 *   1. 创建一个 ArrayList，将 Set 元素传入构造方法：List<T> list = new ArrayList<>(set);
 *   2. 调用 Collections.sort(list) 或 list.sort(comparator) 进行排序。
 * - 优势：不改变原有 Set 的结构，灵活性最高，排序后可按索引分页或输出。
 *
 * 策略二：使用 TreeSet 自动排序（红黑树内置排序）
 * - 原理：TreeSet 底层基于红黑树（TreeMap 实现），每次调用 add() 时自动将元素插入到对应的树节点位置，
 *   使集合在任何时刻都保持有序状态。
 * - 排序条件：
 *   * 方式 A（自然排序）：元素所属类必须实现 Comparable 接口，重写 compareTo 方法；
 *   * 方式 B（定制排序）：构造 TreeSet 时显式传入 Comparator 比较器。
 *
 * 策略三：Java 8+ Stream API 流式排序并收集为 LinkedHashSet（现代函数式）
 * - 核心代码：
 *   set.stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new));
 * - 面试关键陷阱：
 *   千万不能写成 collect(Collectors.toSet())！因为 toSet() 默认返回的可能是无序的 HashSet，
 *   导致排序结果被哈希表重新打乱。必须显式收集为能够保持迭代顺序的 LinkedHashSet！
 *
 * 策略四：直接使用 TreeSet 构造器全量导入
 * - Set<T> sortedSet = new TreeSet<>(originalSet);
 */
public class SetSortingStrategies {

    public static void demonstrateSetSorting() {
        System.out.println("--- 对 Set 集合进行排序的四大策略实战 ---");

        // 构造一个无序的原始 HashSet
        Set<UserAccountItem> originalSet = new HashSet<>();
        originalSet.add(new UserAccountItem(1003, "Charlie", 85));
        originalSet.add(new UserAccountItem(1001, "Alice", 95));
        originalSet.add(new UserAccountItem(1005, "Eve", 72));
        originalSet.add(new UserAccountItem(1002, "Bob", 95)); // 分数与 Alice 相同，用于验证二级排序
        originalSet.add(new UserAccountItem(1004, "David", 88));

        System.out.println("1. 原始 HashSet（无序存储）:");
        originalSet.forEach(u -> System.out.println("   " + u));

        // 策略一：转为 List 排序
        List<UserAccountItem> sortedList = new ArrayList<>(originalSet);
        Collections.sort(sortedList); // 调用 Comparable 自然排序
        System.out.println("\n2. [策略一] 转换为 List 并使用 Collections.sort() 排序结果:");
        sortedList.forEach(u -> System.out.println("   " + u));

        // 策略二：传入 TreeSet 自动维护有序（Comparable 自然排序）
        Set<UserAccountItem> treeSet = new TreeSet<>(originalSet);
        System.out.println("\n3. [策略二] 导入 TreeSet 自动通过红黑树排序结果（高分在前，同分按 ID 升序）:");
        treeSet.forEach(u -> System.out.println("   " + u));

        // 定制 Comparator 排序的 TreeSet（例如强制仅按 userId 降序）
        Set<UserAccountItem> customTreeSet = new TreeSet<>(Comparator.comparingLong(UserAccountItem::getUserId).reversed());
        customTreeSet.addAll(originalSet);
        System.out.println("\n4. [策略二拓展] TreeSet 传入定制 Comparator（按 userId 降序）:");
        customTreeSet.forEach(u -> System.out.println("   " + u));

        // 策略三：Stream API 排序并收集到 LinkedHashSet
        Set<UserAccountItem> linkedHashSetSorted = originalSet.stream()
                .sorted(Comparator.comparingInt(UserAccountItem::getScore).reversed()
                        .thenComparingLong(UserAccountItem::getUserId))
                .collect(Collectors.toCollection(LinkedHashSet::new)); // 关键：收集为 LinkedHashSet 保序
        System.out.println("\n5. [策略三] Stream.sorted() 收集为 LinkedHashSet 保持排序结果:");
        linkedHashSetSorted.forEach(u -> System.out.println("   " + u));
    }
}
