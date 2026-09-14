package map;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 题目覆盖：
 * 1. HashTable 底层实现原理是什么？
 * 2. HashTable线程安全是怎么实现的？
 * 3. Hashmap和Hashtable有什么不一样的？Hashmap一般怎么用?
 * 4. ConcurrentHashMap怎么实现的？
 * 5. 分段锁怎么加锁的?
 * 6. 分段锁是可重入的吗?
 * 7. 已经用了synchronized，为什么还要用CAS呢？
 * 8. ConcurrentHashMap用了悲观锁还是乐观锁?
 * 9. hashtable 和 concurrentHashMap有什么区别
 * 10. 说一下HashMap和Hashtable、ConcurrentMap的区别
 *
 * 核心考点与源码级理论剖析：
 *
 * 一、HashTable 底层原理与线程安全机制：
 * 1. 数据结构：Entry<?,?>[] 动态数组 + 单向链表。
 * 2. 线程安全实现：在几乎所有公共方法（put, get, remove, size）上直接修饰 synchronized 关键字。
 * 3. 锁粒度：【全表对象锁】。整个容器共享同一把锁，任何读写操作相互排斥，并发吞吐量极低。
 * 4. 键值限制：【Key 和 Value 严禁为 null】，存入 null 会直接抛出 NullPointerException。
 * 5. 扩容与哈希：初始容量 11，扩容倍数为 2n + 1，取模除法定位 (hash & 0x7FFFFFFF) % length。
 *
 * 二、HashMap vs Hashtable 核心差异与用法：
 * 1. 差异对比：
 *    - 线程安全：HashMap 线程不安全，无锁极速；Hashtable 线程安全，全表同步低效；
 *    - Null 容忍度：HashMap 允许 1 个 null key 和多个 null value；Hashtable 绝不允许 null；
 *    - 扩容与计算：HashMap 初始 16，2 倍扩容，位与运算；Hashtable 初始 11，2n+1 扩容，取模运算；
 *    - 树化机制：HashMap 具备红黑树优化（>=8且>=64）；Hashtable 永不树化。
 * 2. HashMap 一般怎么用？
 *    - 单线程或方法局部变量直接使用 HashMap；
 *    - 预估容量：new HashMap<>(expectedSize / 0.75 + 1)，彻底规避中间扩容内存抖动；
 *    - 多线程并发场景：坚决放弃 Hashtable 与 Collections.synchronizedMap，统一选用 ConcurrentHashMap。
 *
 * 三、ConcurrentHashMap 底层实现演进史：
 * 1. JDK 1.7 架构（Segment 分段锁机制）：
 *    - 数据结构：Segment[] 数组 + HashEntry[] 链表。
 *    - 分段加锁原理：Segment 继承自 ReentrantLock。通过 key 的高位哈希值定位到具体的 Segment 槽位，
 *      写入时仅对该 Segment 加锁（segment.lock()），不同 Segment 之间并发写完全互不影响，默认并发度 16。
 *    - 分段锁是可重入的吗？【绝对是可重入的】！因为 Segment 本身就是 ReentrantLock 的子类，通过 AQS 的 state 记录重入计数。
 * 2. JDK 1.8+ 架构（Node 数组 + CAS + synchronized 细粒度节点锁）：
 *    - 摒弃了 Segment，结构与 HashMap 统一为 Node[] 数组 + 链表 + 红黑树；
 *    - 锁粒度：由原本的“分段锁（16 个槽）”进一步细化为【单个 Hash 桶的头节点（Node）】，支持成千上万的高并发吞吐。
 *
 * 四、已经用了 synchronized，为什么还要用 CAS？（互补分工机制）：
 * 1. 桶为空时使用 CAS（无冲突零锁极速写入）：
 *    - 当数组槽位 table[i] == null 时，直接调用 casTabAt(tab, i, null, new Node(...))；
 *    - 若 CAS 成功，全程不使用任何重量级互斥锁，指令耗时仅需几个纳秒，单次写入性能达到硬件级极致；失败则自旋。
 * 2. 桶发生冲突时使用 synchronized（复杂链表/树操作的互斥保障）：
 *    - 一旦发生哈希碰撞（table[i] 已有节点），涉及遍历链表、尾插、红黑树旋转平衡等复杂多步指针变更，CAS 无法原子化完成；
 *    - 此时动用 synchronized 仅仅锁定该桶的【头节点】，其他访问不同哈希桶的并发线程完全畅通无阻。
 *
 * 五、ConcurrentHashMap 用了悲观锁还是乐观锁？
 * - 核心结论：【两者兼具，是乐观锁与悲观锁的高性能结合体】！
 * - 乐观锁部分：
 *   1. 读操作（get）：完全无锁，依赖 volatile 关键字的内存可见性直接读取最新数据；
 *   2. 首次写入（桶为空）：使用 CAS 尝试无锁写入（乐观并发思想）；
 *   3. 计数统计（addCount）：底层采用类似 LongAdder 的 CounterCell 分散并发竞争，通过 CAS 累加。
 * - 悲观锁部分：
 *   1. 桶发生哈希冲突时：使用 synchronized（悲观排他锁）锁住链表或红黑树头节点。
 */
public class HashTableVsConcurrentHashMapDeepDive {

    public static void demonstrateNullHandling() {
        System.out.println("--- HashMap 与 Hashtable 对 null 值的支持差异验证 ---");

        // 1. HashMap 允许 null key 与 null value
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put(null, null);
        System.out.println("1. HashMap 成功存入 null key 和 null value: " + hashMap);

        // 2. Hashtable 存入 null key 抛出 NullPointerException
        Hashtable<String, String> hashtable = new Hashtable<>();
        try {
            hashtable.put(null, "Value");
        } catch (NullPointerException ex) {
            System.out.println("2. Hashtable 存入 null key 抛出异常: " + ex.getClass().getSimpleName());
        }

        // 3. Hashtable 存入 null value 抛出 NullPointerException
        try {
            hashtable.put("Key", null);
        } catch (NullPointerException ex) {
            System.out.println("3. Hashtable 存入 null value 抛出异常: " + ex.getClass().getSimpleName());
        }

        // 4. ConcurrentHashMap 同样严禁 null key 和 null value
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();
        try {
            chm.put(null, "Val");
        } catch (NullPointerException ex) {
            System.out.println("4. ConcurrentHashMap 存入 null key 同样抛出: " + ex.getClass().getSimpleName());
        }
    }

    public static void displayMapEcosystemComparison() {
        System.out.println("\n--- HashMap、Hashtable、ConcurrentHashMap 综合对比表 ---");
        System.out.println(String.format("%-18s | %-16s | %-24s | %-14s | %-10s", "对比维度", "HashMap", "Hashtable", "ConcurrentHashMap", "Collections.syncMap"));
        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.println(String.format("%-18s | %-16s | %-24s | %-14s | %-10s", "线程安全性", "线程不安全", "线程安全 (全表同步锁)", "线程安全 (CAS+Node锁)", "线程安全 (内部互斥锁)"));
        System.out.println(String.format("%-18s | %-16s | %-24s | %-14s | %-10s", "并发性能", "无并发支持", "极其低下 (单锁串行竞争)", "极高 (分桶锁+读完全无锁)", "低 (单锁装饰器)"));
        System.out.println(String.format("%-18s | %-16s | %-24s | %-14s | %-10s", "底层结构 (JDK 8)", "数组+链表+红黑树", "数组+链表", "数组+链表+红黑树", "包装普通 Map"));
        System.out.println(String.format("%-18s | %-16s | %-24s | %-14s | %-10s", "Null Key/Value", "支持 1个null key", "完全禁止 (抛 NPE)", "完全禁止 (抛 NPE)", "取决于被包装 Map"));
        System.out.println(String.format("%-18s | %-16s | %-24s | %-14s | %-10s", "扩容机制", "16起步, 2倍扩容", "11起步, 2n+1 扩容", "16起步, 多线程协助并发扩容", "取决于被包装 Map"));
        System.out.println(String.format("%-18s | %-16s | %-24s | %-14s | %-10s", "生产推荐度", "单线程/局部变量首选", "废弃淘汰 (禁止使用)", "高并发场景工业标准", "特定兼容场景过渡"));
    }
}
