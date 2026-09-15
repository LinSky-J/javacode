package list;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目覆盖：
 * 1. ArrayList的扩容机制说一下
 * 2. list如何快速删除某个指定下标的元素?
 *
 * 核心考点与理论剖析：
 *
 * 一、ArrayList 扩容机制深度解析（以 JDK 1.8+ 源码为准）：
 * 1. 初始容量与延迟加载（Lazy Initialization）：
 *    - 无参构造方法 new ArrayList<>()：
 *      内部使用空数组标记 DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {}，此时容量为 0，不占用堆内存；
 *    - 首次调用 add(e) 添加元素时：
 *      触发真正内存分配，将底层数组容量扩容为默认值 DEFAULT_CAPACITY = 10。
 *    - 有参构造方法 new ArrayList<>(int initialCapacity)：
 *      若 initialCapacity > 0，直接创建对应长度的 Object[] 数组；若为 0 则使用 EMPTY_ELEMENTDATA。
 * 2. 扩容计算算法（1.5 倍算术右移）：
 *    - 计算公式：int newCapacity = oldCapacity + (oldCapacity >> 1);
 *    - 相当于 newCapacity = oldCapacity * 1.5（使用位运算 >> 1 替代除法 / 2，提升 CPU 执行效率）。
 *    - 扩容步长序列：10 -> 15 -> 22 -> 33 -> 49 -> 73 ...
 * 3. 边界值检查与巨大容量（Huge Capacity）：
 *    - 最小扩容需求判断：若 1.5 倍后的容量仍然小于所需的最小容量 minCapacity，则直接使用 minCapacity。
 *    - 最大数组上限：MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8（保留 8 个字节用于某些 JVM 实现的对象头信息）；
 *    - 若所需容量超过 MAX_ARRAY_SIZE，调用 hugeCapacity()，最大允许扩容至 Integer.MAX_VALUE (2^31 - 1)。
 * 4. 内存数据迁移：
 *    - 扩容本质是创建全新的大数组，调用 Arrays.copyOf()，底层调用本地方法 System.arraycopy 进行内存块连续复制。
 *    - 最佳实践：在已知预估数据量时，务必调用 new ArrayList<>(expectedSize) 或 ensureCapacity()，
 *      避免频繁扩容引发的反复内存分配与 GC 压力。
 *
 * 二、list如何快速删除某个指定下标的元素?
 * 1. 默认常规删除（保持原有顺序）：
 *    - 调用 list.remove(index)。
 *    - 底层机制：计算移动距离 numMoved = size - index - 1；
 *      若 numMoved > 0，调用 System.arraycopy(elementData, index + 1, elementData, index, numMoved) 将后续元素整体左移一位；
 *      最后将 elementData[--size] = null 协助 GC 垃圾回收。
 *    - 时间复杂度：O(n - index)。删除末尾为 O(1)，删除头部为 O(n)。
 * 2. 极致性能技巧（无序场景 Swap-and-Pop / O(1) 快速删除）：
 *    - 业务背景：如果列表对元素的物理先后顺序不敏感（例如缓存列表、无序任务池、游戏对象实体列表）。
 *    - 核心思路：
 *      第一步：读取列表最后一个元素，覆盖写到待删除的指定下标位置：list.set(index, list.get(list.size() - 1))；
 *      第二步：直接删除列表末尾元素：list.remove(list.size() - 1)。
 *    - 性能优势：删除末尾元素无任何后续元素移动操作，彻底将时间复杂度由 O(n) 降低为严格的 O(1)！
 */
public class ArrayListInternalsAndGrowth {

    /**
     * 反射获取 ArrayList 底层 Object[] 数组的真实物理容量
     * 在高版本 JDK (如 JDK 17/21) 强模块化封装下，若反射受限将优雅降级
     */
    public static int getArrayListCapacity(ArrayList<?> list) {
        try {
            Field field = ArrayList.class.getDeclaredField("elementData");
            field.setAccessible(true);
            Object[] elementData = (Object[]) field.get(list);
            return elementData.length;
        } catch (Throwable e) {
            return -1;
        }
    }

    private static String formatCapacity(int capacity, int expected) {
        return capacity >= 0 ? String.valueOf(capacity) : expected + " (规范标准容量)";
    }

    /**
     * 演示 ArrayList 扩容演进过程
     */
    public static void demonstrateGrowthMechanism() {
        System.out.println("--- ArrayList 扩容机制动态演进实测 ---");

        ArrayList<Integer> list = new ArrayList<>();
        System.out.println("1. new ArrayList<>() 初始化后未添加元素: size=" + list.size() + ", 底层容量=" + formatCapacity(getArrayListCapacity(list), 0));

        list.add(1);
        System.out.println("2. 首次调用 add() 后（惰性分配默认容量）: size=" + list.size() + ", 底层容量=" + formatCapacity(getArrayListCapacity(list), 10));

        for (int i = 2; i <= 10; i++) {
            list.add(i);
        }
        System.out.println("3. 填满 10 个元素，此时尚未扩容: size=" + list.size() + ", 底层容量=" + formatCapacity(getArrayListCapacity(list), 10));

        list.add(11);
        System.out.println("4. 插入第 11 个元素触发 1.5 倍扩容 (10 -> 15): size=" + list.size() + ", 底层容量=" + formatCapacity(getArrayListCapacity(list), 15));

        for (int i = 12; i <= 15; i++) {
            list.add(i);
        }
        list.add(16);
        System.out.println("5. 插入第 16 个元素再次触发 1.5 倍扩容 (15 -> 22): size=" + list.size() + ", 底层容量=" + formatCapacity(getArrayListCapacity(list), 22));
    }

    /**
     * 演示常规删除与 Swap-and-Pop O(1) 快速删除
     */
    public static void demonstrateFastDeleteByIndex() {
        System.out.println("\n--- List 删除指定下标元素的两种方式对比 ---");

        // 方案 1: 常规有序删除（保留原相对次序，底层内存块左移 O(n)）
        List<String> listOrdered = new ArrayList<>();
        listOrdered.add("A");
        listOrdered.add("B");
        listOrdered.add("C");
        listOrdered.add("D");
        listOrdered.add("E");
        System.out.println("1. 原始有序列表: " + listOrdered);

        int targetIndex = 1; // 删除下标 1 处的 "B"
        String removedItem = listOrdered.remove(targetIndex);
        System.out.println("   常规 list.remove(1) 删除元素 [" + removedItem + "]，后续元素整体左移，结果: " + listOrdered);

        // 方案 2: 无序场景下的 Swap-and-Pop 极致 O(1) 算法
        List<String> listUnordered = new ArrayList<>();
        listUnordered.add("A");
        listUnordered.add("B");
        listUnordered.add("C");
        listUnordered.add("D");
        listUnordered.add("E");
        System.out.println("2. 原始无序列表: " + listUnordered);

        int removeIndex = 1; // 待删除下标 1 ("B")
        int lastIndex = listUnordered.size() - 1;
        if (removeIndex != lastIndex) {
            // 将末尾元素覆盖到待删除位置
            listUnordered.set(removeIndex, listUnordered.get(lastIndex));
        }
        // 直接弹出末尾元素，无数组平移开销 O(1)
        listUnordered.remove(lastIndex);
        System.out.println("   Swap-and-Pop O(1) 快速删除下标 1 后的列表（末尾元素替代待删位）: " + listUnordered);
    }
}
