package map;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 题目覆盖：
 * 1. HashMap是线程安全的吗?
 * 2. hashmap 调用get方法一定安全吗?
 * 3. 列举HashMap在多线程下可能会出现的问题?
 *
 * 核心考点与理论剖析：
 *
 * 一、HashMap 是线程安全的吗？
 * - 【绝对不是】！HashMap 是纯粹面向单线程场景设计的高性能散列表，未引入任何锁机制或原子变量。
 *
 * 二、HashMap 调用 get 方法一定安全吗？
 * - 【在多线程环境下绝不安全】！
 * - 核心原因：
 *   1. 读到正在扩容的中间态数据或 null：
 *      当一个线程在调用 get(key) 时，若另一个线程正在执行 put 并触发了 resize() 扩容数据重排迁移。
 *      在数组替换和链表节点被重新链接的临界期，get 线程顺着已被修改的 next 指针可能遍历中断，
 *      导致本应存在的 Key 错误地返回 null，产生诡异的不可重现业务 Bug。
 *   2. JDK 1.7 环形链表导致 get 陷入死循环：
 *      若并发扩容已在某哈希桶中形成了环形死链（A.next=B 且 B.next=A），后续任何线程调用 get 访问该桶时，
 *      将在 while(e != null) { e = e.next; } 中永远无法跳出，导致该 CPU 核心直接飙满 100%！
 *
 * 三、列举 HashMap 在多线程环境下可能出现的致命问题：
 * 1. 数据覆盖与丢失（Data Overwrite）：
 *    - JDK 1.7 与 JDK 1.8 均存在。
 *    - 两线程同时计算出相同的桶下标且此时槽位为空，两线程并发写入，后写覆盖先写，导致先写数据无声丢失。
 * 2. 扩容死循环与 CPU 100%（JDK 1.7 独有）：
 *    - JDK 1.7 在扩容数据迁移时采用【头插法】，会逆转链表中节点的先后顺序；
 *    - 两个线程同时并发扩容时，链表节点的 next 指针被相互交叉引用，形成 A -> B -> A 环形链表；
 *    - 注：JDK 1.8 彻底改用【尾插法】，保持链表节点原有先后次序，彻底修复了扩容死循环问题。
 * 3. size 计数器脏写与数值不一致：
 *    - ++size 包含“读、加、写”多步非原子指令，多线程并发累加必然发生更新丢失，导致 size 远小于实际存入数量。
 * 4. 扩容结果覆盖：
 *    - 多个线程同时检测到需要扩容，各自独立 newTable 并迁移数据，最终赋值给全局 table 时相互覆盖，丢失整批数据。
 * 5. 迭代器 Fail-Fast 异常：
 *    - 遍历期间其他线程修改集合结构（增删节点），触发 ConcurrentModificationException。
 */
public class HashMapConcurrencyFailureModes {

    public static void demonstrateConcurrentPutFailure() {
        System.out.println("--- HashMap 多线程并发写入数据丢失实测 ---");

        int threadCount = 10;
        int elementsPerThread = 1000;
        int expectedTotal = threadCount * elementsPerThread;

        Map<Integer, String> unsafeMap = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < elementsPerThread; j++) {
                        int key = threadId * elementsPerThread + j;
                        unsafeMap.put(key, "Val_" + key);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor.shutdown();

        System.out.println("1. 预期存入唯一 Key 总数: " + expectedTotal + ", HashMap 实际 size: " + unsafeMap.size());
        if (unsafeMap.size() != expectedTotal) {
            System.out.println("   [实测结论] 实际元素数小于预期数量，发生严重的数据覆盖与丢失，证明多线程下必须使用 ConcurrentHashMap！");
        }
    }
}
