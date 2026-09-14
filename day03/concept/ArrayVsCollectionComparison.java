package concept;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 题目：数组与集合区别，用过哪些?
 *
 * 核心考点与理论剖析：
 * 1. 长度特性：
 *    - 数组：长度固定。创建时必须指定容量，一旦初始化不可更改。若容量不足需手动创建新数组并通过 System.arraycopy 迁移。
 *    - 集合：长度动态可变。具备自动扩容机制（如 ArrayList 默认 1.5 倍扩容，HashMap 默认 2 倍扩容）。
 * 2. 存储类型：
 *    - 数组：既能存储基本数据类型（int、double 等），也能存储引用对象；基本数据类型直接存值，无装箱拆箱开销。
 *    - 集合：只能存储引用数据类型。存储基本类型时必须通过自动装箱（如 Integer、Double），存在内存对象头与装箱开销。
 * 3. 泛型与协变（Covariance vs Invariance）：
 *    - 数组是协变的（Covariant）：String[] 是 Object[] 的子类。运行时具备类型检查，错误赋值会抛出 ArrayStoreException。
 *    - 集合是不可变的（Invariant）：List<String> 不是 List<Object> 的子类型，通过泛型类型擦除在编译期保障类型安全。
 * 4. 内存布局与缓存局部性（CPU Cache Line）：
 *    - 数组：底层为连续物理内存，空间紧凑无额外对象头开销，CPU 缓存行友好，连续寻址速度极快。
 *    - 集合：对象分散在堆中（如 LinkedList），每个节点存在指针与对象头开销，内存碎片较多。
 * 5. 丰富度与功能支持：
 *    - 数组：仅提供 length 属性与下标访问，需配合 Arrays 工具类进行有限操作。
 *    - 集合：提供完整的抽象体系（List/Set/Queue/Map），内置迭代器、流式计算、并发控制、动态排序等海量 API。
 *
 * 生产实践中用过哪些：
 * 1. 数组使用场景：
 *    - 底层高性能 I/O 缓冲区：byte[] buffer 读写文件/网络套接字（如 Netty ByteBuf、NIO DirectByteBuffer）。
 *    - 高频热点环形缓冲区：如 Disruptor 中的 Object[] ringBuffer，依靠固定数组预分配规避 GC 压力与伪共享。
 *    - 确定维度的数据元组或算法矩阵：如 int[][] dp 动态规划表，高密度数值计算。
 * 2. 集合使用场景：
 *    - 线性序列：ArrayList（绝大多数业务读多写少列表）、LinkedList（高频首尾插入的双向链表/双端队列）。
 *    - 唯一性去重：HashSet（无序去重）、TreeSet（红黑树有序去重与区间查询）、LinkedHashSet（保持插入次序）。
 *    - 键值映射：HashMap（核心业务缓存与查询）、ConcurrentHashMap（高并发高吞吐线程安全容器）、LinkedHashMap（实现 LRU 淘汰策略）。
 *    - 队列与堆：ArrayDeque（高性能无锁双端队列与栈）、PriorityQueue（Top-K 优先级小顶堆/大顶堆）。
 */
public class ArrayVsCollectionComparison {

    public static void demonstrateComparison() {
        System.out.println("--- 数组与集合特性实战对比 ---");

        // 1. 基本数据类型存储与内存紧凑性
        int[] primitiveArray = new int[]{10, 20, 30};
        System.out.println("1. 数组直接存储基本类型 int，无对象头与装箱开销，长度固定: " + primitiveArray.length);

        //
        double[] d1 = {10.1,20.2};
        // 集合只能存储引用对象，自动装箱为 Integer
        List<Integer> boxedList = new ArrayList<>();
        boxedList.add(10); // 自动装箱 Integer.valueOf(10)
        boxedList.add(20);
        boxedList.add(30);
        System.out.println("2. 集合通过自动装箱存储包装类 Integer，支持动态添加，当前大小: " + boxedList.size());

        // 2. 动态扩容对比
        boxedList.add(40); // 内部触发自动扩容检测
        System.out.println("3. 集合追加元素后自动扩容: " + boxedList);

        // 3. 数组的协变性与运行时检查
        Object[] objectArray = new String[2];
        try {
            // 编译期通过（因为 String[] 是 Object[] 的子类），但运行期抛出 ArrayStoreException
            objectArray[0] = Integer.valueOf(999);
        } catch (ArrayStoreException ex) {
            System.out.println("4. 数组支持协变但存在运行时类型安全风险，捕获 ArrayStoreException: " + ex.getClass().getSimpleName());
        }

        // 集合通过泛型不变性在编译期彻底阻断类型不兼容
        // List<Object> objectList = new ArrayList<String>(); // 编译错误！Type mismatch
        System.out.println("5. 集合通过泛型不变性在编译阶段确保类型安全，避免运行时隐患。");
    }
}
