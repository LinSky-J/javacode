package generics;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 面试专题：Java 泛型机制、应用形态、类型擦除与通配符法则深度解析。
 *
 * 本类对应面试核心题目：
 * 什么是泛型？
 *
 * 核心考点涵盖：
 * 1. 泛型的本质定义：参数化类型（Parameterized Types），将数据类型像方法入参一样进行参数化传递。
 * 2. 泛型解决的痛点：
 *    - 消除繁琐的手工强制类型转换（Downcasting）；
 *    - 将运行期严重的 ClassCastException 异常提前拦截在【编译期】，实现编译期强类型安全。
 * 3. 泛型四大应用形态：泛型类、泛型接口、泛型方法、通配符体系（PECS 原则）。
 * 4. 泛型底层核心机制——类型擦除（Type Erasure）：
 *    - Java 采用伪泛型（Compile-time Generics），编译后所有泛型类型参数都会被擦除为其上限类型（如 Object）；
 *    - 反射证据 1：不同泛型实参的集合其 Class 运行时类型完全相同；
 *    - 反射证据 2：利用反射绕过编译期安全检查，成功向 List<String> 插入 Integer 对象！
 * 5. 泛型的四大语法限制与避坑要点。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaGenericsConceptAndErasure {

    public static void main(String[] args) {

        System.out.println("======================================================================");
        System.out.println("            Java 泛型机制全景、类型擦除与反射实测深度剖析              ");
        System.out.println("======================================================================");

        explainWhatIsGenerics();

        System.out.println("\n======================================================================");
        System.out.println("             泛型知识体系解析完毕，请细读类中源码与注释                ");
        System.out.println("======================================================================");
    }

    /**
     * 核心解答方法：
     * 什么是泛型？
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void explainWhatIsGenerics() {
        System.out.println("什么是泛型？");

        /*
         * 1. 泛型的核心本质（参数化类型 Parameterized Types）：
         *    - 传统方法定义中，操作的数据是“形参”传入的，类型是固定的。
         *    - 泛型则是把【具体的类型本身】也当作参数传递！
         *    - 比如 List<E>，E 就是一个类型形参，当编写 List<String> 时，String 就是传递给 E 的类型实参。
         */
        System.out.println("\n--- 一、泛型解决的核心痛点对比实测 ---");

        // 痛点回顾：Java 1.4 无泛型时代的隐患
        List rawList = new ArrayList();
        rawList.add("合法字符串");
        rawList.add(10086); // 容器是 Object，什么都能塞进去，没有任何编译期检查！

        try {
            // 程序员误以为全是字符串，取出时强转直接触发崩溃
            String item0 = (String) rawList.get(0);
            System.out.println("   [无泛型获取] 第 0 项强转成功: " + item0);
            // 故意尝试强转第 1 项为 String
            String item1 = (String) rawList.get(1); // 运行期直接抛出 ClassCastException
        } catch (ClassCastException e) {
            System.out.println("   [生产灾难警示] 无泛型容器在取出数据时发生严重运行期崩溃: " + e);
        }

        // 泛型时代的降维打击：编译期直接阻断非法数据！
        List<String> genericList = new ArrayList<>();
        genericList.add("安全规范字符串");
        // genericList.add(10086); // 编译直接报错！Incompatible types: int cannot be converted to String
        String safeItem = genericList.get(0); // 无需任何手工强转，编译器自动插入类型转换指令
        System.out.println("   [泛型容器获取] 编译期强类型安全约束，获取无需手工强转: " + safeItem);

        /*
         * 2. 泛型的四大应用形态实战：
         *    - 泛型类（Generic Class）：ResultWrapper<T>
         *    - 泛型方法（Generic Method）：ResultWrapper.<E>success(E data)
         *    - 泛型接口（Generic Interface）：GenericRepository<T, ID>
         *    - 泛型通配符（Wildcards）：? extends T 与 ? super T（PECS 原则）
         */
        System.out.println("\n--- 二、泛型核心应用形态（类、方法、接口、通配符） ---");

        // 2.1 泛型类与静态泛型方法
        UserEntity user1 = new UserEntity(1001L, "研发工程师-张三", 26);
        ResultWrapper<UserEntity> apiResponse = ResultWrapper.success(user1);
        System.out.println("   [泛型类与静态泛型方法实测] " + apiResponse);

        // 2.2 泛型接口与实现类
        GenericRepository<UserEntity, Long> userRepository = new UserRepositoryImpl();
        userRepository.save(user1);
        userRepository.findById(1001L);

        // 2.3 泛型通配符与 PECS 原则实测
        System.out.println("\n--- 三、通配符与 PECS 原则（Producer Extends, Consumer Super） ---");
        /*
         * PECS 原则是泛型通配符使用的黄金法则：
         * 1. 生产者使用 extends（Producer Extends）：
         *    - List<? extends Number>：只能从中【读取数据】（作为生产者），不能向其中添加任何元素（除 null 外）！
         *    - 因为编译器只知道里面是 Number 的某种子类，但不知道具体是 Integer、Double 还是 Long，无法保证写入安全。
         * 2. 消费者使用 super（Consumer Super）：
         *    - List<? super Integer>：专门用来【存入消费数据】（作为消费者）。
         *    - 允许存入 Integer 及其子类；但读取出来的数据只能作为 Object 对待。
         */
        List<Integer> integerList = Arrays.asList(10, 20, 30);
        double sum = sumOfList(integerList); // 协变只读操作
        System.out.println("   [PECS 原则-Producer Extends 只读累加] 列表求和结果: " + sum);

        List<Number> consumerList = new ArrayList<>();
        addNumbers(consumerList); // 逆变只写操作
        System.out.println("   [PECS 原则-Consumer Super 只写消费] 成功写入元素列表: " + consumerList);

        /*
         * 3. 泛型底层核心原理解密——类型擦除（Type Erasure）现场实测证据
         * 为什么称 Java 泛型是“伪泛型”？
         * - C++ 模板是真泛型，每个类型都会生成一份独一无二的机器码。
         * - Java 为了向下兼容旧版本的 JVM 字节码，在编译后将所有的泛型参数信息全部“擦除”！
         * - 无界类型如 <T> 被擦除为 Object；有界类型如 <T extends Number> 被擦除为 Number。
         */
        System.out.println("\n--- 四、类型擦除（Type Erasure）现场反射抓包实证 ---");

        List<String> strList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();

        // 抓包证据 1：运行时 Class 类型完全相同
        System.out.println("   [证据一] strList.getClass() == intList.getClass(): "
                + (strList.getClass() == intList.getClass())
                + " (真实运行时类名均为: " + strList.getClass().getName() + ")");

        // 抓包证据 2：利用反射绕过编译期泛型检查，向 List<String> 成功塞入 Integer！
        strList.add("原始安全字符串");
        try {
            // 通过反射获取 ArrayList 的原始 add(Object) 方法
            Method addMethod = strList.getClass().getMethod("add", Object.class);
            // 跨过编译期，直接把 Integer 放入 List<String> 的堆内存中！
            addMethod.invoke(strList, 999999);
            System.out.println("   [证据二] 反射突破泛型拦截成功！当前 List<String> 真实内存内容: " + strList);
            System.out.println("   (铁证如山：泛型仅存活在编译期阶段；进入运行期后，类型约束荡然无存！)");
        } catch (Exception e) {
            System.out.println("   反射执行失败: " + e.getMessage());
        }

        /*
         * 4. 泛型的四大语法限制与避坑总结
         */
        System.out.println("\n--- 五、泛型的四大语法限制与避坑总结 ---");
        System.out.println("   1. 不能使用基本数据类型：如 List<int> 是非法语法，必须使用包装类 List<Integer>（因为类型擦除后无法用 Object 容纳基本类型）。");
        System.out.println("   2. 不能直接实例化类型参数：如 new T() 是非法的（因为类型擦除后运行时根本不知道具体该 new 哪个类）。");
        System.out.println("   3. 不能创建泛型数组：如 new ArrayList<String>[10] 是非法的（数组协变与泛型擦除会破坏数组运行期类型安全）。");
        System.out.println("   4. 静态上下文不能直接引用类的泛型形参：因为静态成员在类加载时就已初始化，而类泛型只有在 new 实例化时才能确定。");
    }

    /**
     * PECS 生产者示例：只读上限通配符 ? extends Number
     */
    private static double sumOfList(List<? extends Number> list) {
        double sum = 0.0;
        for (Number n : list) {
            sum += n.doubleValue(); // 允许安全读取为 Number
        }
        // list.add(100); // 编译报错！无法写入任何具体类型数据
        return sum;
    }

    /**
     * PECS 消费者示例：只写下限通配符 ? super Integer
     */
    private static void addNumbers(List<? super Integer> list) {
        // 允许安全写入 Integer 及其子类
        list.add(100);
        list.add(200);
        // Integer n = list.get(0); // 编译报错！读取出来只能是 Object，无法保证为 Integer
    }
}
