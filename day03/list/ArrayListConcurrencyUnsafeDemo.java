package list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 题目覆盖：
 * 1. ArrayList线程安全吗？把ArrayList变成线程安全有哪些方法？
 * 2. 为什么ArrayList不是线程安全的，具体来说是哪里不安全?
 *
 * 核心考点与理论剖析：
 *
 * 一、为什么 ArrayList 不是线程安全的？具体是哪里不安全？
 * 深入 ArrayList.add(E e) 的核心源码：
 *      public boolean add(E e) {
 *          ensureCapacityInternal(size + 1);  // 1. 扩容容量检查
 *          elementData[size++] = e;             // 2. 赋值并自增
 *          return true;
 *      }
 *
 * 致命不安全隐患一：元素值被覆盖（数据丢失）
 * - elementData[size++] = e 并非原子操作（Atomic Operation），底层可拆解为四步字节码指令：
 *   Step 1: 读取当前成员变量 size 的值；
 *   Step 2: 将元素 e 赋值给 elementData[size]；
 *   Step 3: 计算 size + 1；
 *   Step 4: 将新值写回成员变量 size。
 * - 并发时序：线程 A 和线程 B 同时读取到 size=5。
 *   线程 A 执行 elementData[5] = "ValA"；
 *   线程 B 紧接着执行 elementData[5] = "ValB"，将线程 A 刚刚写入的数据直接覆盖！
 *   最终两个线程分别将 size 加 1，size 变成了 7（甚至因未刷回主存导致 size 变小），但 index=5 的数据被彻底覆盖丢失。
 *
 * 致命不安全隐患二：数组下标越界异常（ArrayIndexOutOfBoundsException）
 * - 场景：假设当前底层数组容量为 10，当前 size=9。
 * - 线程 A 调用 add()，执行 ensureCapacityInternal(10)，容量 10 满足要求，无需扩容。CPU 时间片切换。
 * - 线程 B 调用 add()，此时 size 仍然是 9，执行 ensureCapacityInternal(10)，同样判断无需扩容。
 * - 线程 A 恢复执行：elementData[9] = e1，随后 size 变为 10。
 * - 线程 B 恢复执行：此时直接写入 elementData[10] = e2！
 *   但底层数组长度仅为 10（合法下标 0~9），直接发生 JVM 崩溃抛出 ArrayIndexOutOfBoundsException！
 *
 * 致命不安全隐患三：size 计数值脏写与 null 洞
 * - size 变量没有使用 volatile 修饰，多线程 CPU L1/L2 缓存可见性无法保障，读写存在脏读。
 *
 * 二、把 ArrayList 变成线程安全的方法有哪些？
 * 1. 使用 Collections.synchronizedList(new ArrayList<>())：
 *    - 装饰器模式，内部所有方法加 synchronized(mutex) 互斥锁。注意遍历时需手动对包装对象加锁。
 * 2. 使用 java.util.concurrent.CopyOnWriteArrayList：
 *    - 写时复制，读操作完全无锁，写操作独占加锁并全量复制底层新数组，适合读多写极少。
 * 3. 客户端显式同步控制：
 *    - 在业务层使用 ReentrantLock 或 synchronized 代码块对 ArrayList 的操作手动加锁保护。
 * 4. 线程隔离（ThreadLocal）：
 *    - ThreadLocal<List<T>>，每个线程拥有独享的 ArrayList 副本，彻底消除资源并发竞争。
 */
public class ArrayListConcurrencyUnsafeDemo {

    /**
     * 实测 ArrayList 多线程并发写入时的数据丢失与异常隐患
     */
    public static void demonstrateUnsafeAdd() {
        System.out.println("--- ArrayList 多线程并发写入不安全性实测 ---");

        int threadCount = 10;
        int operationsPerThread = 1000;
        int expectedTotal = threadCount * operationsPerThread;

        List<Integer> unsafeList = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        unsafeList.add(j);
                    }
                } catch (Exception ex) {
                    System.out.println("捕获并发异常: " + ex.getClass().getSimpleName());
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

        System.out.println("1. 预期元素总数: " + expectedTotal + ", ArrayList 实际容量: " + unsafeList.size());
        if (unsafeList.size() != expectedTotal) {
            System.out.println("   [实测结果] 发生数据丢失与覆盖，证明 ArrayList 线程不安全！");
        }
    }

    /**
     * 演示将 List 转为线程安全的解决方案
     */
    public static void demonstrateThreadSafeSolutions() {
        System.out.println("\n--- ArrayList 线程安全改造方案实测 ---");

        int threadCount = 10;
        int operationsPerThread = 1000;
        int expectedTotal = threadCount * operationsPerThread;

        // 方案 1: Collections.synchronizedList
        List<Integer> syncList = Collections.synchronizedList(new ArrayList<>());
        executeConcurrentWrites(syncList, threadCount, operationsPerThread);
        System.out.println("1. 方案一 (Collections.synchronizedList): 实际元素数=" + syncList.size() + " (预期=" + expectedTotal + ")");

        // 方案 2: CopyOnWriteArrayList
        List<Integer> cowList = new CopyOnWriteArrayList<>();
        executeConcurrentWrites(cowList, threadCount, 200); // 写时复制写开销较大，跑 2000 次
        System.out.println("2. 方案二 (CopyOnWriteArrayList 写时复制): 实际元素数=" + cowList.size() + " (预期=" + (threadCount * 200) + ")");
    }

    private static void executeConcurrentWrites(List<Integer> list, int threadCount, int operationsPerThread) {
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        list.add(j);
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
    }
}
