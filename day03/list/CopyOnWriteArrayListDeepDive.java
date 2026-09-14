package list;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 题目覆盖：
 * 线程安全的List, CopyonWriteArraylist是如何实现线程安全的
 *
 * 核心考点与源码级深度剖析：
 *
 * 一、CopyOnWriteArrayList 核心架构与成员变量：
 * 1. 核心成员变量：
 *    - private transient volatile Object[] array;
 *      底层数据结构依然是数组，关键在于修饰了 volatile 关键字：
 *      保证多线程内存可见性（Memory Visibility），一旦数组指针被替换，其他线程立即可见。
 *    - final transient Object lock = new Object(); (JDK 11+) 或 ReentrantLock lock; (JDK 8)
 *      用于在写操作时提供互斥访问保障。
 *
 * 二、读写分离与线程安全机制：
 * 1. 读操作（get、size、isEmpty、contains）：
 *    - 核心特点：【完全无锁】！
 *    - 源码逻辑：直接读取当前 volatile 数组引用并在下标上取值：getArray()[index]。
 *    - 优势：读线程之间、读线程与写线程之间互不阻塞，读性能达到极致。
 * 2. 写操作（add、set、remove）：
 *    - 核心机制：【写时复制（Copy-On-Write）】。
 *    - 详细时序：
 *      Step 1: 加独占互斥锁（synchronized (lock)），保证同一时刻只有一个写线程进入；
 *      Step 2: 读取当前旧数组 Object[] es = getArray()，获取长度 len；
 *      Step 3: 调用 Arrays.copyOf(es, len + 1) 分配一个长度加 1 的【新数组 newElements】；
 *      Step 4: 将待添加元素写入新数组末尾 newElements[len] = element；
 *      Step 5: 调用 setArray(newElements)，将底层 volatile 数组指针原子重定向到新数组；
 *      Step 6: 释放锁。
 *    - 关键保证：写线程在修改数据的整个过程中，旧数组内容未发生任何改变，读线程依然安全稳定地访问旧数组。
 *
 * 三、快照迭代器（COWIterator 与 Fail-Safe）：
 * 1. 弱一致性快照：
 *    - 迭代器在创建时，保存了当时底层数组的一个瞬时快照：final Object[] snapshot = getArray()。
 *    - 即使在遍历过程中其他线程调用了 add/remove 产生新数组，迭代器依然遍历的是旧快照，
 *      因此【绝对不会抛出 ConcurrentModificationException】。
 * 2. 迭代器不可变性：
 *    - COWIterator 不支持修改，调用 it.remove() / it.set() 会直接抛出 UnsupportedOperationException。
 *
 * 四、缺点与性能代价（面试深水区）：
 * 1. 内存消耗与 GC 压力：
 *    - 每次写操作都要将整块数组全量拷贝一份。若集合存有 10 万个对象，每次写入都要复制 10 万个指针，
 *      会频繁占用新生代 Eden 区，引发高频 Minor GC 乃至 Full GC。
 * 2. 数据弱一致性：
 *    - 只能保证最终一致性，无法保证强一致性。写线程刚刚写入的数据，由于读线程持有旧数组引用，
 *      可能在短时间内读到脏数据/旧数据。
 * 3. 适用场景：
 *    - 读极多、写极少（99% 读，1% 写）的业务。例如：RPC 框架服务节点路由缓存、黑白名单、权限配置、系统事件监听器。
 */
public class CopyOnWriteArrayListDeepDive {

    public static void demonstrateCowMechanisms() {
        System.out.println("--- CopyOnWriteArrayList 线程安全与快照遍历实测 ---");

        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();
        cowList.add("Node_A");
        cowList.add("Node_B");
        cowList.add("Node_C");

        System.out.println("1. 初始集合: " + cowList);

        // 演示快照迭代器（遍历中并发修改不抛 CME）
        System.out.println("2. 启动快照迭代器遍历...");
        Iterator<String> iterator = cowList.iterator();

        // 在迭代器创建之后，主线程往集合中新增元素
        cowList.add("Node_D");
        System.out.println("   [写线程] 已向集合追加新元素 Node_D，当前实时集合: " + cowList);

        System.out.print("   [读线程] 迭代器遍历输出: ");
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();
        System.out.println("3. 结果证实：迭代器遍历的是快照版本（仅 Node_A, Node_B, Node_C），未读到遍历期间追加的 Node_D，且无 CME 异常！");

        // 尝试在 COWIterator 上调用 remove
        Iterator<String> testRemoveIt = cowList.iterator();
        testRemoveIt.next();
        try {
            testRemoveIt.remove();
        } catch (UnsupportedOperationException ex) {
            System.out.println("4. COWIterator 是只读快照，调用 iterator.remove() 抛出: " + ex.getClass().getSimpleName());
        }
    }
}
