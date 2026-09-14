package concept;

/**
 * Java 集合面试题（概念篇）综合讲解与实战验证
 *
 * 逐字保留面试问题：
 * 1. 数组与集合区别，用过哪些?
 * 2. 说说Java中的集合?
 * 3. Java中的线程安全的集合是什么？
 * 4. Collections和Collection的区别
 * 5. 集合遍历的方法有哪些?
 */
public class CollectionConceptsInterviewExplanation {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("问题 1: 数组与集合区别，用过哪些?");
        System.out.println("================================================================================");
        explainArrayVsCollection();

        System.out.println("\n================================================================================");
        System.out.println("问题 2: 说说Java中的集合?");
        System.out.println("================================================================================");
        explainJavaCollectionsFramework();

        System.out.println("\n================================================================================");
        System.out.println("问题 3: Java中的线程安全的集合是什么？");
        System.out.println("================================================================================");
        explainThreadSafeCollections();

        System.out.println("\n================================================================================");
        System.out.println("问题 4: Collections和Collection的区别");
        System.out.println("================================================================================");
        explainCollectionVsCollections();

        System.out.println("\n================================================================================");
        System.out.println("问题 5: 集合遍历的方法有哪些?");
        System.out.println("================================================================================");
        explainCollectionTraversalMethods();
    }

    /**
     * 问题 1 深度解析
     */
    private static void explainArrayVsCollection() {
        System.out.println("【理论解答】");
        System.out.println("1. 数组与集合的核心差异：");
        System.out.println("   - 长度特性：数组长度固定，初始化后不可更改，扩容需重新创建并拷贝；集合长度动态可变，自动扩容。");
        System.out.println("   - 存储类型：数组可存储基本类型和引用类型，基本类型无装箱拆箱开销；集合只能存储引用类型，存储数值需自动装箱为包装类。");
        System.out.println("   - 类型系统：数组支持协变（String[] 是 Object[] 子类）且具备运行时类型检查；集合类型不变（通过编译期泛型擦除保证类型安全）。");
        System.out.println("   - 内存布局：数组是连续物理内存，空间紧凑且 CPU 缓存行（Cache Line）命中率高；集合对象分散，具备对象头与指针开销。");
        System.out.println("   - 功能丰富度：数组仅支持 length 与下标检索；集合体系提供完整的 List/Set/Queue/Map 数据结构及排序、查找、流计算 API。");
        System.out.println("2. 生产实际使用场景：");
        System.out.println("   - 数组：网络/文件 I/O 缓冲区（byte[] buffer）、Disruptor 环形缓冲区（Object[] ringBuffer）、算法高频计算矩阵。");
        System.out.println("   - 集合：ArrayList（绝大多数业务读多写少列表）、LinkedList（高频首尾双向队列）、HashSet（去重）、");
        System.out.println("          TreeSet（红黑树排序与范围查询）、HashMap（核心 K-V 缓存）、ConcurrentHashMap（高并发安全映射）、");
        System.out.println("          ArrayDeque（高效无锁双端队列与栈）、PriorityQueue（Top-K 堆排序）。");

        System.out.println();
        ArrayVsCollectionComparison.demonstrateComparison();
    }

    /**
     * 问题 2 深度解析
     */
    private static void explainJavaCollectionsFramework() {
        System.out.println("【理论解答】");
        System.out.println("Java 集合框架（JCF）主要由两大顶层根接口组成：");
        System.out.println("1. Collection<E> 单列集合体系：");
        System.out.println("   - List 体系（有序、有索引、元素可重复）：");
        System.out.println("     * ArrayList：底层 Object[] 动态数组，随机访问 O(1)，尾部追加 O(1)，中间插入删除 O(n)。非线程安全。");
        System.out.println("     * LinkedList：底层双向链表，首尾增删 O(1)，无随机索引，按索引查找 O(n)。实现 Deque 接口。");
        System.out.println("     * Vector / Stack：遗留同步类，方法加 synchronized，性能低下。");
        System.out.println("   - Set 体系（无序无索引、元素唯一不重复）：");
        System.out.println("     * HashSet：底层基于 HashMap（仅用 key，value 共享虚拟 Object），查找增删 O(1)。");
        System.out.println("     * LinkedHashSet：底层基于 LinkedHashMap，维护双向链表，保证迭代顺序与插入顺序一致。");
        System.out.println("     * TreeSet：底层基于红黑树（TreeMap 实现），支持自然排序或 Comparator 定制排序，操作 O(log n)。");
        System.out.println("   - Queue / Deque 体系（队列与双端队列）：");
        System.out.println("     * ArrayDeque：基于循环数组的高性能双端队列，作为栈和队列效率超越 Stack 与 LinkedList。");
        System.out.println("     * PriorityQueue：基于二叉小顶堆/大顶堆的优先级队列，出队元素具有最高优先级。");
        System.out.println("2. Map<K, V> 双列键值对体系：");
        System.out.println("   - 存储 Key-Value 映射，Key 唯一不可重复，Value 可重复。");
        System.out.println("   - HashMap：数组 + 单向链表 + 红黑树（JDK 8+，链表>=8且数组>=64树化），线程不安全。");
        System.out.println("   - LinkedHashMap：在 HashMap 基础上维护双向链表，支持插入序或访问序（用于 LRU 缓存淘汰）。");
        System.out.println("   - TreeMap：基于红黑树实现，所有 Key 严格有序，提供 subMap、firstKey 等范围检索。");
        System.out.println("   - Hashtable：遗留全表同步类，Key/Value 均不可为 null。");

        System.out.println();
        JavaCollectionArchitecture.demonstrateArchitectureOverview();
    }

    /**
     * 问题 3 深度解析
     */
    private static void explainThreadSafeCollections() {
        System.out.println("【理论解答】");
        System.out.println("Java 线程安全集合经历了三代重大技术演进：");
        System.out.println("1. 第一代：早期遗留同步类（Vector, Hashtable, Stack）：");
        System.out.println("   - 在几乎所有公开方法上直接修饰 synchronized，锁粒度为整个对象。");
        System.out.println("   - 读读互斥、读写互斥，多线程并发吞吐量极差，已淘汰。");
        System.out.println("2. 第二代：Collections 装饰器包装（Collections.synchronizedXxx）：");
        System.out.println("   - 使用装饰器模式包装非安全集合，通过内部 final Object mutex 进行互斥同步。");
        System.out.println("   - 单方法安全，但在外部使用 Iterator 遍历时仍需客户端代码手动显式加锁，否则易发生并发异常。");
        System.out.println("3. 第三代：JUC 高性能并发容器（分段锁 / CAS / 读写分离 / 无锁算法 / 推荐）：");
        System.out.println("   - ConcurrentHashMap：JDK 7 采用 Segment 分段锁；JDK 8+ 采用 Node[] 数组 + CAS + synchronized 锁桶节点，读完全无锁。");
        System.out.println("   - CopyOnWriteArrayList / CopyOnWriteArraySet：写时复制。读操作无锁并发极高；写操作加锁复制底层新数组，适合读多写极少场景。");
        System.out.println("   - 并发队列：ConcurrentLinkedQueue（基于 CAS 的无锁非阻塞队列）；");
        System.out.println("   - 阻塞队列：ArrayBlockingQueue（单锁双条件）、LinkedBlockingQueue（双锁分离高效生产者-消费者队列）。");

        System.out.println();
        ThreadSafeCollectionsExplorer.demonstrateThreadSafeCollections();
    }

    /**
     * 问题 4 深度解析
     */
    private static void explainCollectionVsCollections() {
        System.out.println("【理论解答】");
        System.out.println("1. 概念本质区别：");
        System.out.println("   - Collection 是一个【接口】（java.util.Collection），为单列集合（List、Set、Queue）提供抽象行为契约，无法实例化。");
        System.out.println("   - Collections 是一个【工具类】（java.util.Collections），构造方法私有化，专门提供静态算法与包装方法。");
        System.out.println("2. 核心功能区别：");
        System.out.println("   - Collection 接口定义了 add()、remove()、contains()、size()、iterator()、stream() 等方法。");
        System.out.println("   - Collections 工具类提供了：");
        System.out.println("     * 排序与翻转：sort(list)、reverse(list)、shuffle(list)。");
        System.out.println("     * 二分查找与统计：binarySearch(list, key)、max(coll)、min(coll)、frequency(coll, o)。");
        System.out.println("     * 线程安全装饰：synchronizedList(list)、synchronizedMap(map)。");
        System.out.println("     * 不可变只读视图：unmodifiableList(list)，试图修改立即抛出 UnsupportedOperationException。");
        System.out.println("     * 内存优化单例：emptyList()、emptyMap()，避免每次都 new 空集合造成垃圾回收压力。");

        System.out.println();
        CollectionVsCollectionsDifference.demonstrateDifference();
    }

    /**
     * 问题 5 深度解析
     */
    //集合遍历的方法有哪些
    private static void explainCollectionTraversalMethods() {
        System.out.println("【理论解答】");
        System.out.println("1. 集合遍历的 6 种方式：");
        System.out.println("   - 普通 for 循环（带下标）：仅支持带索引的 List（如 ArrayList 效率高，严禁用于 LinkedList，否则导致 O(n^2) 性能灾难）。");
        System.out.println("   - 增强 for 循环（for-each）：语法糖，底层编译为 Iterator，适用于所有 Iterable 集合及数组。");
        System.out.println("   - Iterator（单向迭代器）：具备 hasNext()、next()、remove() 方法，是边遍历边删除唯一安全的传统方式。");
        System.out.println("   - ListIterator（双向迭代器）：专属于 List，支持向前遍历、修改 set() 与动态插入 add()。");
        System.out.println("   - Iterable.forEach()（Java 8+）：函数式内部迭代，接收 Consumer 函数式接口。");
        System.out.println("   - Stream API（Java 8+）：list.stream().forEach()，支持流式过滤、转换、聚合以及 parallelStream 并行计算。");
        System.out.println("2. 快速失败机制（Fail-Fast）与 ConcurrentModificationException（CME）：");
        System.out.println("   - 原理：ArrayList 内部维护 modCount 记录结构修改次数。创建 Iterator 时保存 expectedModCount = modCount。");
        System.out.println("   - 触发：在遍历过程中直接调用 list.remove() 或 list.add() 导致 modCount 变化，下一次 next() 校验失败立即抛出 CME。");
        System.out.println("   - 正确解法：");
        System.out.println("     * 方式 A：使用 Iterator.remove()（内部同步更新 expectedModCount = modCount）。");
        System.out.println("     * 方式 B：使用 Java 8+ Collection.removeIf(predicate)（底层批量移位优化，性能最高）。");
        System.out.println("     * 方式 C：使用 CopyOnWriteArrayList（Fail-Safe 弱一致性，在底层数组快照上遍历，绝不抛 CME）。");

        System.out.println();
        CollectionTraversalMethodsDemo.demonstrateAllTraversalMethods();
        CollectionTraversalMethodsDemo.demonstrateCmeAndSafeRemoval();
    }
}
