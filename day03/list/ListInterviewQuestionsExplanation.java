package list;

/**
 * Java List 面试题全景深度剖析与实战演练
 *
 * 逐字保留面试问题：
 * 1. 讲一下java里面list的几种实现，几种实现有什么不同？
 * 2. list可以一边遍历一边修改元素吗？
 * 3. list如何快速删除某个指定下标的元素?
 * 4. Arraylist和LinkedList的区别，哪个集合是线程安全的?
 * 5. arraylist和vector 区别是什么?
 * 6. ArrayList线程安全吗？把ArrayList变成线程安全有哪些方法？
 * 7. 为什么ArrayList不是线程安全的，具体来说是哪里不安全?
 * 8. ArrayList 和 LinkedList 的应用场景?
 * 9. ArrayList的扩容机制说一下
 * 10. 线程安全的List, CopyonWriteArraylist是如何实现线程安全的
 * 11. List<>里面填基本数据类型为什么会报错?
 * 12. List和数组如何互相转换?
 */
public class ListInterviewQuestionsExplanation {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("问题 1 & 4 & 5 & 8: List 各种实现类、差异、线程安全性与应用场景");
        System.out.println("包含原题：");
        System.out.println("- 讲一下java里面list的几种实现，几种实现有什么不同？");
        System.out.println("- Arraylist和LinkedList的区别，哪个集合是线程安全的?");
        System.out.println("- arraylist和vector 区别是什么?");
        System.out.println("- ArrayList 和 LinkedList 的应用场景?");
        System.out.println("================================================================================");
        explainListImplementationsAndComparison();

        System.out.println("\n================================================================================");
        System.out.println("问题 2: list可以一边遍历一边修改元素吗？");
        System.out.println("================================================================================");
        explainTraversalModification();

        System.out.println("\n================================================================================");
        System.out.println("问题 3: list如何快速删除某个指定下标的元素?");
        System.out.println("================================================================================");
        explainFastDeleteByIndex();

        System.out.println("\n================================================================================");
        System.out.println("问题 6 & 7: ArrayList 线程安全性剖析与改造方案");
        System.out.println("包含原题：");
        System.out.println("- ArrayList线程安全吗？把ArrayList变成线程安全有哪些方法？");
        System.out.println("- 为什么ArrayList不是线程安全的，具体来说是哪里不安全?");
        System.out.println("================================================================================");
        explainArrayListThreadSafety();

        System.out.println("\n================================================================================");
        System.out.println("问题 9: ArrayList的扩容机制说一下");
        System.out.println("================================================================================");
        explainArrayListGrowth();

        System.out.println("\n================================================================================");
        System.out.println("问题 10: 线程安全的List, CopyonWriteArraylist是如何实现线程安全的");
        System.out.println("================================================================================");
        explainCopyOnWriteArrayList();

        System.out.println("\n================================================================================");
        System.out.println("问题 11 & 12: List 泛型本质与数组互转机制");
        System.out.println("包含原题：");
        System.out.println("- List<>里面填基本数据类型为什么会报错?");
        System.out.println("- List和数组如何互相转换?");
        System.out.println("================================================================================");
        explainGenericsAndArrayConversion();
    }

    private static void explainListImplementationsAndComparison() {
        //1.java里面的list的几种实现，几种实现有什么不同
        System.out.println("【理论解答】");
        System.out.println("1. List 的主流实现类及区别：");
        System.out.println("   - ArrayList：底层 Object[] 动态数组。连续内存，随机访问 O(1)，中间插入删除 O(n)。非线程安全。");
        System.out.println("   - LinkedList：底层双向链表。首尾增删 O(1)，按索引寻址 O(n)，节点携带前后指针内存开销大。非线程安全。");
        System.out.println("   - Vector：底层 Object[] 数组。所有方法全加 synchronized，线程安全但并发性能差，默认 2 倍扩容，遗留类。");
        System.out.println("   - Stack：继承自 Vector 的 LIFO 栈结构，已被 ArrayDeque 取代。");
        System.out.println("   - CopyOnWriteArrayList：JUC 读写分离无锁读容器，线程安全。");
        System.out.println("2. 哪个集合是线程安全的？");
        System.out.println("   - ArrayList 和 LinkedList 均【不是】线程安全的！");
        System.out.println("   - 线程安全的实现是 Vector、Collections.synchronizedList 和 CopyOnWriteArrayList。");
        System.out.println("3. ArrayList 与 Vector 的区别：");
        System.out.println("   - 同步机制：Vector 方法级加锁（低效），ArrayList 无锁（高效）。");
        System.out.println("   - 扩容步长：ArrayList 扩容为 1.5 倍，Vector 默认扩容为 2 倍。");
        System.out.println("   - 历史版本：Vector 是 JDK 1.0 遗留类，ArrayList 是 JDK 1.2 现代集合框架。");
        System.out.println("4. ArrayList 和 LinkedList 的应用场景：");
        System.out.println("   - ArrayList：适用于 99% 的常见业务场景（查询多、尾部追加、数据量中大型、CPU 缓存局部性好）。");
        System.out.println("   - LinkedList：仅适用于频繁在头部和尾部做增删、绝不需要随机根据下标 get(i) 的双端队列场景。");

        System.out.println();
        ListImplementationsComparison.demonstrateArrayListVsVector();
    }

    //list可以一遍遍历一遍修改元素吗？
    private static void explainTraversalModification() {
        System.out.println("【理论解答】");
        System.out.println("必须区分【修改元素内容】与【结构性修改（增/删）】：");
        System.out.println("1. 修改元素内容（非结构性修改）：完全可以！");
        System.out.println("   - 普通 for 循环调用 list.set(i, val)，或 ListIterator.set(val)，因为不改变元素数量与 modCount，安全无异常。");
        System.out.println("2. 结构性修改（增删元素）：");
        System.out.println("   - 在增强 for 或普通 Iterator 遍历中直接调用 list.add() 或 list.remove() 会破坏 modCount 一致性，触发 CME。");
        System.out.println("   - 正确解法：使用 Iterator.remove()、ListIterator.add()、Java 8 removeIf() 或并发容器 CopyOnWriteArrayList。");

        System.out.println();
        ListImplementationsComparison.demonstrateTraversalModification();
    }

    //list如果快速删除某个指定元素的下标
    private static void explainFastDeleteByIndex() {
        System.out.println("【理论解答】");
        System.out.println("1. 常规删除方式（保持原有元素顺序）：");
        System.out.println("   - list.remove(index)：底层通过 System.arraycopy 将后续元素整体左移 1 位，复杂度为 O(n - index)。");
        System.out.println("2. 极致性能技巧（无序场景下的 Swap-and-Pop 算法，达到严格 O(1)）：");
        System.out.println("   - 核心步骤：");
        System.out.println("     Step 1: 将末尾元素覆盖到待删除的下标位置：list.set(index, list.get(list.size() - 1))；");
        System.out.println("     Step 2: 直接弹出删除末尾元素：list.remove(list.size() - 1)。");
        System.out.println("   - 优势：彻底消除大量内存块拷贝移动，耗时恒定 O(1)。");

        System.out.println();
        ArrayListInternalsAndGrowth.demonstrateFastDeleteByIndex();
    }

    //为什么ArrayList不是线程安全的，具体来说是哪里不安全？
    private static void explainArrayListThreadSafety() {
        System.out.println("【理论解答】");
        System.out.println("1. 为什么 ArrayList 不是线程安全的？具体哪里不安全？");
        System.out.println("   - 核心隐患点：add(E e) 中的核心语句 elementData[size++] = e 并非原子操作。");
        System.out.println("   - 隐患一（数据覆盖/丢失）：两线程读到相同 size，先后写入同一槽位，后写覆盖先写，造成数据丢失。");
        System.out.println("   - 隐患二（数组越界异常）：两线程同时执行容量校验均通过，先后写入时导致第二个线程越界抛出 ArrayIndexOutOfBoundsException。");
        System.out.println("   - 隐患三（脏写与 null 洞）：size 未加 volatile，无内存可见性保证。");
        System.out.println("2. 把 ArrayList 变成线程安全的方法：");
        System.out.println("   - 方案 A：Collections.synchronizedList(new ArrayList<>())（装饰器对象监视器锁）。");
        System.out.println("   - 方案 B：CopyOnWriteArrayList（写时复制无锁读容器）。");
        System.out.println("   - 方案 C：业务层使用 ReentrantLock 或 synchronized 显式互斥加锁。");
        System.out.println("   - 方案 D：ThreadLocal<List<T>> 线程副本隔离。");

        System.out.println();
        ArrayListConcurrencyUnsafeDemo.demonstrateUnsafeAdd();
        ArrayListConcurrencyUnsafeDemo.demonstrateThreadSafeSolutions();
    }

    //arrayList的扩容机制？
    private static void explainArrayListGrowth() {
        System.out.println("【理论解答】");
        System.out.println("ArrayList 扩容机制核心步骤（JDK 1.8+）：");
        System.out.println("1. 初始容量：");
        System.out.println("   - new ArrayList<>() 默认构造空数组 {}，首次调用 add() 发生惰性分配，容量初始化为 10。");
        System.out.println("2. 扩容算法（1.5 倍增长）：");
        System.out.println("   - 计算公式：int newCapacity = oldCapacity + (oldCapacity >> 1);");
        System.out.println("   - 步长增长：10 -> 15 -> 22 -> 33 -> 49 -> 73 ...");
        System.out.println("3. 巨大容量处理：");
        System.out.println("   - 上限为 MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8，若继续扩容调用 hugeCapacity() 可达到 Integer.MAX_VALUE。");
        System.out.println("4. 内存迁移：底层调用 Arrays.copyOf() -> 本地方法 System.arraycopy 进行高效率内存块迁移。");

        System.out.println();
        ArrayListInternalsAndGrowth.demonstrateGrowthMechanism();
    }
    //线程安全的List CopyOnWritrArrayList()是如何实现线程安全的？
    private static void explainCopyOnWriteArrayList() {
        System.out.println("【理论解答】");
        System.out.println("CopyOnWriteArrayList 线程安全实现原理：");
        System.out.println("1. 核心结构：");
        System.out.println("   - volatile Object[] array：保证底层数组引用的可见性与 happens-before 原则。");
        System.out.println("   - 互斥锁（ReentrantLock 或 Object lock）：仅在写操作时加锁。");
        System.out.println("2. 读写分离机制：");
        System.out.println("   - 读操作（get/size）：完全无锁！直接读取 volatile 数组引用，读读并发、读写并发互不阻塞，读性能极高。");
        System.out.println("   - 写操作（add/set/remove）：加互斥锁，通过 Arrays.copyOf() 复制出一份全新的新数组，在副本上完成修改后，");
        System.out.println("     通过原子写回更新 volatile 数组指针，最后释放锁。");
        System.out.println("3. 迭代器特性：COWIterator 保存创建时的快照副本（Snapshot），遍历过程弱一致性，绝不抛 CME。");
        System.out.println("4. 缺点与权衡：写操作内存开销大（频繁全量拷贝引发 GC），存在弱一致性延时，仅适用于读极多写极少的场景。");

        System.out.println();
        CopyOnWriteArrayListDeepDive.demonstrateCowMechanisms();
    }

    private static void explainGenericsAndArrayConversion() {
        System.out.println("【理论解答】");
        System.out.println("1. List<> 里面填基本数据类型（如 List<int>）为什么报错？");
        System.out.println("   - Java 泛型采用【类型擦除（Type Erasure）】，编译后类型参数擦除为上限 Object。");
        System.out.println("   - Java 基本数据类型（int、double 等）直接存数值，不是对象，不继承自 java.lang.Object。");
        System.out.println("   - JVM 无法将栈上 4 字节的裸数据直接当作堆上 8 字节的对象指针处理，因此必须使用包装类（如 List<Integer>）。");
        System.out.println("2. List 和数组互相转换的陷阱与标准方式：");
        System.out.println("   - 数组转 List 陷阱：Arrays.asList 返回 Arrays$ArrayList 固定长度内部类，不能 add/remove，且数据与原数组双向绑定；");
        System.out.println("     将 int[] 传入返回的是 List<int[]> 而非 List<Integer>。");
        System.out.println("   - 推荐写法：");
        System.out.println("     * 引用类型数组转 List：new ArrayList<>(Arrays.asList(array))；");
        System.out.println("     * 基本类型数组转 List：Arrays.stream(arr).boxed().collect(Collectors.toList())；");
        System.out.println("     * List 转强类型数组：list.toArray(new String[0])（官方推荐传长度为 0 的空数组，性能最优）。");

        System.out.println();
        ListVsArrayConversionAndGenerics.demonstrateGenericsAndConversion();
    }
}
