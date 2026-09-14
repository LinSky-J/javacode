package list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目覆盖：
 * 1. List<>里面填基本数据类型为什么会报错?
 * 2. List和数组如何互相转换?
 *
 * 核心考点与理论剖析：
 *
 * 一、为什么 List<> 里面填基本数据类型（如 List<int>）会报错？
 * 1. 泛型擦除机制（Type Erasure）：
 *    - Java 的泛型是伪泛型，编译后所有的类型形参 <T> 都会被擦除为其边界类型，未指定边界时默认擦除为 java.lang.Object。
 *    - 编译生成的底层字节码中，List.add(T) 变成 List.add(Object)，List.get(int) 变成 Object get(int)。
 * 2. 类型系统二元分化：
 *    - 基本数据类型（int、double、boolean 等）：是纯粹的裸数据（Raw Bits），直接分配在栈帧局部变量表或堆对象字段内，
 *      没有对象头（Mark Word 与 Klass Word），不具备面向对象特征，【不继承自 java.lang.Object】。
 *    - 引用数据类型：全部继承自 java.lang.Object，存储的是堆内存的引用指针。
 * 3. 冲突根源：
 *    - JVM 无法将一个 4 字节的裸 int 数据直接当作 8 字节的堆内存对象指针存储在 Object[] 槽位中。
 *    - 解决方案：使用包装类（如 List<Integer>），通过编译期/运行期的自动装箱（Integer.valueOf()）将基本类型包装为堆对象。
 *
 * 二、List 与数组互相转换的标准方式与三大陷阱：
 * 1. 数组转 List：
 *    - 陷阱 A：Arrays.asList(array) 返回的是 java.util.Arrays$ArrayList 内部类，并非 java.util.ArrayList。
 *      该类并未重写 add() 与 remove() 方法，调用会抛出 UnsupportedOperationException。
 *    - 陷阱 B：底层数据联动。Arrays.asList 底层直接持有原数组指针，修改数组中的元素会导致 List 跟着改变，反之亦然。
 *    - 陷阱 C：基本类型数组陷阱。将 int[] arr 传入 Arrays.asList(arr)，由于 int[] 本身是一个 Object，
 *      返回的类型是 List<int[]>（长度为 1），而非期望的 List<Integer>。
 *    - 推荐标准解法：
 *      * 引用类型：new ArrayList<>(Arrays.asList(strArray))；
 *      * 基本类型：Arrays.stream(intArray).boxed().collect(Collectors.toList())。
 * 2. List 转数组：
 *    - list.toArray()：返回 Object[] 数组，无法向下强转为 String[]，否则触发 ClassCastException。
 *    - list.toArray(T[] a) 最佳实践：
 *      * 语法：list.toArray(new String[0])。
 *      * 性能考量：现代 JVM 推荐使用 new T[0]（长度为 0 的空数组），JVM JIT 编译器经过专门优化，
 *        避免了预分配 new T[list.size()] 产生的内存清零开销，性能甚至略优于指定长度。
 */
public class ListVsArrayConversionAndGenerics {

    public static void demonstrateGenericsAndConversion() {
        System.out.println("--- List 泛型与数组互转机制实测 ---");

        // 1. 数组转 List 的陷阱与安全实践
        String[] strArray = new String[]{"Alpha", "Beta", "Gamma"};

        // 陷阱 A & B 演示
        List<String> fixedList = Arrays.asList(strArray);
        System.out.println("1. Arrays.asList() 返回固定长度内部类: " + fixedList.getClass().getName());
        try {
            fixedList.add("Delta"); // 抛出 UnsupportedOperationException
        } catch (UnsupportedOperationException ex) {
            System.out.println("   [捕获异常] 试图向 Arrays.asList 结果添加元素抛出: " + ex.getClass().getSimpleName());
        }

        // 修改原数组，观察 List 是否联动
        strArray[0] = "Alpha_Modified";
        System.out.println("   修改原数组首元素后，fixedList 同步联动被修改: " + fixedList.get(0));

        // 安全拷贝方式
        List<String> modifiableList = new ArrayList<>(Arrays.asList(strArray));
        modifiableList.add("Delta_Safe");
        System.out.println("2. 安全转换为可修改 ArrayList: " + modifiableList);

        // 陷阱 C：基本类型数组转换
        int[] primitiveNums = {10, 20, 30};
        List<int[]> wrongList = Arrays.asList(primitiveNums);
        System.out.println("3. 基本类型 int[] 传入 Arrays.asList 错误变为 List<int[]>，size=" + wrongList.size());

        // 正确的基本类型数组转 List
        List<Integer> correctList = Arrays.stream(primitiveNums).boxed().collect(Collectors.toList());
        System.out.println("4. 使用 Arrays.stream().boxed() 正确转为 List<Integer>: " + correctList);

        // 2. List 转数组的最佳实践
        List<String> frameworkList = new ArrayList<>(Arrays.asList("Spring", "Netty", "MyBatis"));

        // 规范推荐做法：toArray(new String[0])
        String[] resultArray = frameworkList.toArray(new String[0]);
        System.out.println("5. List.toArray(new String[0]) 转换为类型安全强类型数组: length=" + resultArray.length + ", 首元素=" + resultArray[0]);
    }
}
