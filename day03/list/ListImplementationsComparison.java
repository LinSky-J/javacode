package list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

/**
 * 题目覆盖：
 * 1. 讲一下java里面list的几种实现，几种实现有什么不同？
 * 2. list可以一边遍历一边修改元素吗？
 * 3. Arraylist和LinkedList的区别，哪个集合是线程安全的?
 * 4. arraylist和vector 区别是什么?
 * 5. ArrayList 和 LinkedList 的应用场景?
 *
 * 核心考点与理论剖析：
 *
 * 一、Java 常见 List 实现类及核心区别：
 * 1. ArrayList：
 *    - 底层结构：Object[] 动态数组。连续物理内存，支持 O(1) 随机访问，尾插平摊 O(1)，中间插入删除需移动后续元素 O(n)。
 *    - 线程安全：线程不安全。
 *    - 内存特点：空间紧凑，无额外指针开销，CPU 缓存命中率高，末尾可能存在少量预分配闲置空间。
 * 2. LinkedList：
 *    - 底层结构：基于 Node<E> 双向链表（item, next, prev）。
 *    - 核心优势：首尾插入与删除操作达到极致的 O(1)，实现 Deque 接口。
 *    - 缺点：不支持高效随机访问，按索引查找必须从头部或尾部遍历寻址 O(n)；每个元素需要维护两个指针（prev/next），
 *      在 64 位 JVM 上每个节点额外开销至少 24 字节，内存碎片较多，CPU 缓存行缺失严重。
 *    - 线程安全：线程不安全。
 * 3. Vector：
 *    - 底层结构：Object[] 动态数组。
 *    - 核心区别：几乎所有方法全用 synchronized 修饰，全表互斥锁，多线程并发吞吐量极低。
 *    - 扩容机制：默认扩容为原来的 2 倍（可自定义 capacityIncrement），而 ArrayList 是 1.5 倍。
 *    - 历史地位：JDK 1.0 产物，属于遗留类，已被弃用。
 * 4. Stack：
 *    - 继承自 Vector，基于数组实现后进先出（LIFO）的栈结构。同样全局加锁性能差，官方建议使用 ArrayDeque 替代。
 * 5. CopyOnWriteArrayList：
 *    - JUC 并发安全列表，写时复制机制，读操作完全无锁，写操作独占加锁并全量复制底层新数组。
 *
 * 二、list可以一边遍历一边修改元素吗？
 * 面试关键陷阱：必须严格区分【修改元素内容（set）】与【结构性修改（add / remove）】：
 * 1. 修改元素内容（非结构性修改）：【完全可以】！
 *    - 普通 for 循环调用 list.set(i, newVal) 或增强 for 中修改对象内部属性；
 *    - 使用 ListIterator.set(newVal)；
 *    - 原因：set() 方法不改变集合元素总数，不修改内部 modCount 计数器，绝不触发 CME。
 * 2. 结构性修改（增加或删除元素）：
 *    - 在增强 for 或标准 Iterator 遍历过程中，直接调用集合的 list.add() 或 list.remove() 会导致 modCount 变化，
 *      触发 Fail-Fast 快速失败机制抛出 ConcurrentModificationException。
 *    - 安全的结构性修改途径：
 *      * 使用 Iterator.remove()（传统安全删除）；
 *      * 使用 ListIterator.add() / ListIterator.remove()；
 *      * 使用 Java 8+ 的 list.removeIf(predicate)；
 *      * 使用 CopyOnWriteArrayList（Fail-Safe 弱一致性机制）。
 *
 * 三、ArrayList 和 LinkedList 的应用场景：
 * 1. ArrayList 的应用场景（现代业务系统 99% 的首选）：
 *    - 绝大部分后端业务接口查询、分页数据、数据传输对象（DTO/VO）列表；
 *    - 读多写少、按索引高频随机访问（get(i)）、批量顺序追加（add(e)）的场景；
 *    - 高度依赖 CPU 缓存亲和性（Cache Locality）的高性能数据遍历。
 * 2. LinkedList 的应用场景：
 *    - 频繁在列表【头部与尾部】进行插入与删除操作（如滑动窗口、历史撤回栈、轻量级 FIFO 队列）；
 *    - 绝不需要根据索引随机寻址的场景。
 *    - 注意：在现代高性能架构中，即使需要队列/栈结构，官方也强烈推荐使用 ArrayDeque，
 *      因为 ArrayDeque 底层循环数组规避了频繁分配 Node 节点对象的 GC 压力，性能依然超越 LinkedList。
 */
public class ListImplementationsComparison {

    public static void demonstrateTraversalModification() {
        System.out.println("--- List 遍历中修改元素机制验证 ---");

        // 1. 修改元素内容（非结构性修改，安全执行）
        List<String> list = new ArrayList<>(Arrays.asList("java", "python", "go"));
        System.out.println("1. 原始列表: " + list);

        // 使用普通 for 循环调用 set()
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i).toUpperCase()); // 安全修改内容
        }
        System.out.println("   遍历中使用 list.set() 修改元素内容成功（未改动 modCount）: " + list);

        // 使用 ListIterator.set() 修改内容
        ListIterator<String> listIt = list.listIterator();
        while (listIt.hasNext()) {
            String val = listIt.next();
            if ("GO".equals(val)) {
                listIt.set("GOLANG"); // 安全修改
            }
        }
        System.out.println("   使用 ListIterator.set() 修改内容成功: " + list);

        // 2. 结构性修改：ListIterator 动态新增元素
        while (listIt.hasPrevious()) {
            String val = listIt.previous();
            if ("JAVA".equals(val)) {
                listIt.add("KOTLIN"); // ListIterator.add() 同步更新游标与期望修改计数
            }
        }
        System.out.println("2. 使用 ListIterator.add() 遍历中安全新增元素: " + list);
    }

    public static void demonstrateArrayListVsVector() {
        System.out.println("\n--- ArrayList 与 Vector 核心对比 ---");
        List<Integer> arrayList = new ArrayList<>();
        Vector<Integer> vector = new Vector<>();

        arrayList.add(100);
        vector.add(100);

        System.out.println("1. ArrayList (非线程安全，1.5 倍扩容，无锁高性能): size=" + arrayList.size());
        System.out.println("2. Vector (线程安全方法加锁，默认 2 倍扩容，遗留同步类): size=" + vector.size());
    }
}
