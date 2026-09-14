package concept;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 题目：Java中的线程安全的集合是什么？
 *
 * 核心技术演进与深度剖析：
 * Java 中的线程安全集合经历了三代重大技术演进：
 *
 * 第一代：早期遗留同步容器（全对象锁，互斥并发，已淘汰）
 * 1. 代表类：Vector、Hashtable、Stack。
 * 2. 实现机制：在所有公开的读写方法上简单粗暴地修饰 synchronized 关键字。
 * 3. 缺点：锁粒度是整个对象，读读互斥、读写互斥，多线程并发时吞吐量极低，已基本废弃。
 *
 * 第二代：Collections 装饰器同步包装（对象监视器锁，依然是单锁）
 * 1. 代表方法：
 *    - Collections.synchronizedList(new ArrayList<>())
 *    - Collections.synchronizedMap(new HashMap<>())
 *    - Collections.synchronizedSet(new HashSet<>())
 * 2. 实现机制：采用装饰器设计模式（Decorator Pattern），包装内部的非线程安全集合，
 *    通过内部成员 final Object mutex 对象在每个方法块执行 synchronized(mutex) {...}。
 * 3. 注意事项：虽然单方法操作线程安全，但在进行迭代遍历时，必须由外部客户端代码手动加锁，
 *    否则在遍历期间其他线程修改集合依然会导致并发不一致或 ConcurrentModificationException。
 *
 * 第三代：JUC（java.util.concurrent）现代高性能并发容器（细粒度锁 / CAS / 读写分离 / 推荐）
 * 1. ConcurrentHashMap：
 *    - 行业标准的高并发 K-V 容器。
 *    - JDK 1.7：Segment 分段锁（继承自 ReentrantLock），并发度默认为 16。
 *    - JDK 1.8+：彻底摒弃 Segment，采用 Node[] 数组 + CAS 无锁设置 + synchronized 锁链表/红黑树首节点。
 *      读操作完全无锁（利用 volatile 保证可见性与 happens-before 原则），锁粒度细化至单个桶级别。
 * 2. CopyOnWriteArrayList / CopyOnWriteArraySet：
 *    - 核心思想：写时复制（Copy-On-Write）。
 *    - 读操作：完全不加锁，直接读取底层 volatile Object[] array 引用，读性能极高且绝对线程安全。
 *    - 写操作：添加/修改/删除时，使用 ReentrantLock 独占锁，先复制出一个新数组，在副本上修改完毕后，
 *      再原子替换原数组指针。
 *    - 适用场景：读极大（99%）、写极少的配置中心、黑白名单、事件监听器订阅列表。
 *    - 缺点：写操作每次均需全量内存拷贝，内存占用大，存在短暂弱一致性延迟。
 * 3. 并发队列（Queue）：
 *    - ConcurrentLinkedQueue：无锁并发队列，基于 CAS 与单向链表（Michael & Scott 算法）实现无界非阻塞队列。
 *    - BlockingQueue 体系（阻塞队列，生产者-消费者模式核心）：
 *      * ArrayBlockingQueue：基于固定数组的有界队列，内部由单一 ReentrantLock 与 notEmpty/notFull 两个 Condition 协作。
 *      * LinkedBlockingQueue：基于单向链表，采用“双锁分离”（putLock 负责入队，takeLock 负责出队），
 *        入队和出队互不干扰，并发吞吐量显著高于 ArrayBlockingQueue。
 */
public class ThreadSafeCollectionsExplorer {

    public static void demonstrateThreadSafeCollections() {
        System.out.println("--- Java 线程安全集合三大演进实测 ---");

        // 1. 第一代：遗留类 Hashtable 与 Vector
        Hashtable<String, String> legacyTable = new Hashtable<>();
        legacyTable.put("k1", "v1");
        Vector<Integer> legacyVector = new Vector<>();
        legacyVector.add(100);
        //锁的粒度太粗了，导致并发性不高
        System.out.println("1. 第一代遗留同步类（方法级 synchronized）: Hashtable size=" + legacyTable.size() + ", Vector size=" + legacyVector.size());

        // 2. 第二代：Collections 装饰器包装
        List<String> syncList = Collections.synchronizedList(new java.util.ArrayList<>());
        syncList.add("Task1");
        Map<String, String> syncMap = Collections.synchronizedMap(new java.util.HashMap<>());
        syncMap.put("Key", "Value");
        System.out.println("2. 第二代 Collections.synchronizedXxx 包装容器: List size=" + syncList.size() + ", Map size=" + syncMap.size());

        // 3. 第三代：JUC 高性能并发容器
        // A. ConcurrentHashMap
        ConcurrentHashMap<String, Integer> concurrentMap = new ConcurrentHashMap<>();
        concurrentMap.put("ServiceA", 8080);
        concurrentMap.computeIfAbsent("ServiceB", k -> 9090); // 原子复合操作
        System.out.println("3. 第三代 JUC ConcurrentHashMap (CAS+细粒度Node锁): " + concurrentMap);

        // B. CopyOnWriteArrayList
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();
        cowList.add("WhiteList_IP1");
        cowList.add("WhiteList_IP2");
        System.out.println("4. 第三代 JUC CopyOnWriteArrayList (读写分离/写时复制): " + cowList);

        // C. 并发与阻塞队列
        ConcurrentLinkedQueue<String> lockFreeQueue = new ConcurrentLinkedQueue<>();
        lockFreeQueue.offer("Message_1");
        System.out.println("5. 第三代 JUC ConcurrentLinkedQueue (CAS无锁非阻塞队列): poll=" + lockFreeQueue.poll());

        BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>(100);
        blockingQueue.offer("Data_Job");
        System.out.println("6. 第三代 JUC LinkedBlockingQueue (双锁分离阻塞队列): peek=" + blockingQueue.peek());
    }
}
