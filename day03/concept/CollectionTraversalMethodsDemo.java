package concept;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 题目：集合遍历的方法有哪些?
 *
 * 核心考点与深度剖析：
 *
 * 一、常用遍历的 6 种途径：
 * 1. 普通 for 循环（带索引）：
 *    - 适用范围：仅支持有整数下标的 List 集合（如 ArrayList）。
 *    - 避坑要点：严禁使用普通 for 循环遍历 LinkedList！因为 get(i) 每次都要从头或尾重新寻址，导致时间复杂度劣化至 O(n^2)。
 * 2. 增强 for 循环（for-each）：
 *    - 适用范围：所有实现 Iterable 接口的集合以及数组。
 *    - 底层原理：Java 编译期语法糖，编译后本质上被替换为基于 Iterator 的 hasNext() 与 next() 循环。
 * 3. Iterator（单向迭代器）：
 *    - 适用范围：所有 Collection 单列集合。
 *    - 核心方法：hasNext()、next()、remove()。是传统代码中【边遍历边删除】唯一安全的方式。
 * 4. ListIterator（双向迭代器）：
 *    - 专属于 List 集合的高级迭代器。
 *    - 特有能力：支持向前遍历（hasPrevious()、previous()）、获取游标位置（nextIndex()）、
 *      原地修改元素（set()）、动态插入新元素（add()）。
 * 5. Iterable.forEach() 方法（Java 8+ 函数式内部迭代）：
 *    - 接收 java.util.function.Consumer 函数式接口，语法极度简洁，代码表现力高。
 * 6. Stream API 流式遍历（Java 8+）：
 *    - list.stream().forEach(...)，支持流式中间操作（filter、map、sorted）以及 parallelStream 并行多线程遍历。
 *
 * 二、并发修改异常（ConcurrentModificationException，简称 CME）与快速失败机制（Fail-Fast）：
 * 1. 触发机理：
 *    - ArrayList 内部维护着一个成员变量 modCount（记录集合结构被修改的次数）。
 *    - 当创建 Iterator 或使用增强 for 时，迭代器内部会暂存当前的快照值 expectedModCount = modCount。
 *    - 如果在循环体内直接调用集合自身的 list.remove() 或 list.add()，会导致集合的 modCount 自增。
 *    - 在下一次调用 iterator.next() 时，内部调用 checkForComodification()，检测到 modCount != expectedModCount，
 *      立即抛出 ConcurrentModificationException。
 * 2. 安全删除的标准实践：
 *    - 方案 A：使用 Iterator.remove()（在删除元素的同时同步将 expectedModCount 赋值为新的 modCount）。
 *    - 方案 B：使用 Java 8+ 的 Collection.removeIf(Predicate)（底层一次性批量位移优化，效率最高）。
 *    - 方案 C：使用并发容器 CopyOnWriteArrayList（Fail-Safe 安全失败机制，遍历发生在快照副本上，不抛 CME）。
 */
public class CollectionTraversalMethodsDemo {

    public static void demonstrateAllTraversalMethods() {
        System.out.println("--- 集合遍历的 6 种方式实战演练 ---");
        List<String> list = Arrays.asList("Spring", "MyBatis", "Redis", "Kafka");

        // 1. 普通 for 循环
        System.out.print("1. 普通 for 循环 (带索引): ");
        for (int i = 0; i < list.size(); i++) {
            System.out.print(list.get(i) + (i == list.size() - 1 ? "" : ", "));
        }
        System.out.println();

        // 2. 增强 for 循环
        System.out.print("2. 增强 for 循环 (for-each): ");
        for (String item : list) {
            System.out.print(item + " ");
        }
        System.out.println();

        // 3. 基础 Iterator
        System.out.print("3. Iterator 迭代器: ");
        Iterator<String> it = list.iterator();
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();

        // 4. ListIterator 双向迭代
        System.out.print("4. ListIterator 逆序反向遍历: ");
        ListIterator<String> listIterator = list.listIterator(list.size()); // 定位到尾部
        while (listIterator.hasPrevious()) {
            System.out.print(listIterator.previous() + " ");
        }
        System.out.println();

        // 5. Iterable.forEach 结合 Lambda
        System.out.print("5. Iterable.forEach(Consumer): ");
        list.forEach(item -> System.out.print(item + " "));
        System.out.println();

        // 6. Stream API 流式遍历
        System.out.print("6. Stream API 过滤并遍历: ");
        list.stream().filter(s -> s.startsWith("K") || s.startsWith("R"))
                     .forEach(s -> System.out.print(s + " "));
        System.out.println();
    }

    public static void demonstrateCmeAndSafeRemoval() {
        System.out.println("\n--- ConcurrentModificationException (CME) 与安全删除演示 ---");

        // 演示 1：增强 for 中调用 list.remove() 触发 CME
        List<String> cmeList = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        try {
            for (String item : cmeList) {
                if ("B".equals(item)) {
                    cmeList.remove(item); // 错误做法！直接调用集合自身的 remove
                }
            }
        } catch (ConcurrentModificationException ex) {
            System.out.println("1. 增强 for 中直接调用 list.remove() 触发 Fail-Fast 异常: " + ex.getClass().getSimpleName());
        }

        // 演示 2：方案 A - 使用 Iterator.remove() 安全删除
        List<String> safeIteratorList = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        Iterator<String> safeIt = safeIteratorList.iterator();
        while (safeIt.hasNext()) {
            String item = safeIt.next();
            if ("B".equals(item)) {
                safeIt.remove(); // 正确做法！同步更新 expectedModCount
            }
        }
        System.out.println("2. 使用 Iterator.remove() 安全删除后的集合: " + safeIteratorList);

        // 演示 3：方案 B - 使用 Java 8 removeIf 安全删除
        List<String> removeIfList = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        removeIfList.removeIf("C"::equals);
        System.out.println("3. 使用 Java 8 list.removeIf() 安全删除后的集合: " + removeIfList);

        // 演示 4：方案 C - CopyOnWriteArrayList 在遍历中修改（Fail-Safe 弱一致性快照）
        List<String> cowList = new CopyOnWriteArrayList<>(Arrays.asList("A", "B", "C"));
        for (String item : cowList) {
            if ("B".equals(item)) {
                cowList.remove(item); // 不抛异常，底层基于写时复制副本
            }
        }
        System.out.println("4. CopyOnWriteArrayList Fail-Safe 机制遍历中删除成功: " + cowList);
    }
}
