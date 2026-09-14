package java8;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 面试专题：Java 8 核心新特性全景剖析与 Lambda 表达式底层原理。
 *
 * 本类对应面试核心题目：
 * 1. Java 8你知道有什么新特性?
 * 2. Lambda 表达式了解吗?
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class Java8FeaturesAndLambdaExplanation {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          Java 8 核心新特性全景纵览 与 Lambda 表达式底层深度解析       ");
        System.out.println("======================================================================");

        explainJava8NewFeatures();
        explainLambdaExpressions();

        System.out.println("\n======================================================================");
        System.out.println("        Java 8 与 Lambda 机制解析完毕，请细读类中源码与详细注释        ");
        System.out.println("======================================================================");
    }

    /**
     * 问题一：Java 8你知道有什么新特性?
     *
     * 面试核心考点：
     * 1. 结构化分类回答：从语法、核心API、底层架构、并发等维度系统展开。
     * 2. 深入展开 2~3 个最重量级的特性（Lambda/Stream/Optional/新时间API/元空间）。
     * 3. 讲清特性解决了历史上的哪些痛点。
     */
    public static void explainJava8NewFeatures() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题一：Java 8你知道有什么新特性?");
        System.out.println("--------------------------------------------------");

        /*
         * 【Java 8 十大核心新特性系统盘点】：
         *
         * 1. Lambda 表达式与函数式编程：
         *    - 允许把函数作为参数传递进方法，极大简化了匿名内部类的冗长样板代码，使代码更简洁、紧凑。
         *
         * 2. Stream 流式数据计算 API（java.util.stream）：
         *    - 把真正的函数式编程风格引入到集合操作中，提供了声明式的过滤、映射、分组、归约和并行计算能力。
         *
         * 3. 接口默认方法（default）与静态方法（static）：
         *    - 允许在接口中定义已实现的方法，解决了老接口升级时无法向后兼容（所有实现类必须同步实现新方法）的历史顽疾。
         *
         * 4. 方法引用（Method References）：
         *    - 配合 Lambda 使用的语法糖，直接引用已有方法或构造器，如 System.out::println、String::length、User::new。
         *
         * 5. Optional 优雅判空容器类（java.util.Optional）：
         *    - 专门为防御 NullPointerException（空指针）设计的容器，规范方法返回值可能为空的契约。
         *
         * 6. 全新 Date and Time API（java.time 包）：
         *    - 基于 JSR-310 规范，提供了 LocalDate, LocalTime, LocalDateTime, ZonedDateTime 等。
         *    - 彻底解决旧版 java.util.Date 与 SimpleDateFormat 线程非安全、可变、设计混乱的问题（新 API 均为不可变且线程安全）。
         *
         * 7. 强大的 CompletableFuture 异步并发编程：
         *    - 引入了基于事件驱动和链式编排的异步多任务框架，支持非阻塞回调、任务依赖组合与多线程异常兜底。
         *
         * 8. JVM 永久代（PermGen）被元空间（Metaspace）取代：
         *    - 方法区在 HotSpot 上的实现从 JVM 堆内存的永久代迁移到了操作系统的本地物理内存（Native Memory），
         *      彻底消除了由大量动态代理或反射类加载导致的 java.lang.OutOfMemoryError: PermGen space。
         *
         * 9. HashMap 底层重构优化：
         *    - 由旧版的【数组 + 链表】优化为【数组 + 链表 + 红黑树】（阈值为链表长度 >= 8 且数组容量 >= 64 时树化），
         *      将极端哈希冲突下的查找时间复杂度由 O(n) 降低到 O(log n)。
         *
         * 10. Base64 编解码内置支持：
         *     - java.util.Base64 成为官方标准库一部分，不再需要借助第三方 sun.misc.BASE64Decoder。
         */

        System.out.println("1. [新特性实测：Optional 优雅解决空指针]");
        String nullableValue = null;
        String safeResult = Optional.ofNullable(nullableValue)
                .map(String::toUpperCase)
                .orElse("默认备选值_DEFAULT_VALUE");
        System.out.println("   Optional 链式处理结果: " + safeResult);

        System.out.println("\n2. [新特性实测：全新线程安全的 java.time 日期时间 API]");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("   不可变且线程安全的格式化输出: " + now.format(formatter));
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        System.out.println("   不可变日期运算(+7天): " + nextWeek);

        System.out.println("\n3. [新特性实测：接口默认方法与静态方法调用]");
        CustomCalculationService calc = (x, y) -> x * y + 10;
        double result = calc.calculate(5.0, 4.0);
        calc.logCalculation(5.0, 4.0, result); // 调用接口 default 方法
        System.out.println("   调用接口 static 工具方法: CustomCalculationService.isPositive(10) -> " +
                CustomCalculationService.isPositive(10));
    }

    /**
     * 问题二：Lambda 表达式了解吗?
     *
     * 面试核心考点：
     * 1. 语法基础与结构定义：(参数列表) -> { 方法体; }。
     * 2. 目标类型与函数式接口（@FunctionalInterface，单抽象方法 SAM 契约）。
     * 3. Java 8 四大核心内置函数式接口（Consumer、Supplier、Function、Predicate）。
     * 4. 变量捕获（Variable Capture）限制：必须是 final 或 effectively final 的深层原因。
     * 5. 底层实现机制（大厂分水岭考点）：invokedynamic 指令与 LambdaMetafactory，绝非简单的匿名内部类语法糖！
     */
    public static void explainLambdaExpressions() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题二：Lambda 表达式了解吗?");
        System.out.println("--------------------------------------------------");

        /*
         * 【Lambda 核心概念与本质】：
         * 1. 概念：Lambda 表达式是一个可传递的代码块，可以把它当成一个“匿名函数”看待。
         * 2. 核心支撑：Lambda 不能独立存在，必须依托于【函数式接口（Functional Interface）】作为其目标类型。
         *    函数式接口定义：有且仅有一个未实现的抽象方法（Single Abstract Method，SAM）。
         *    通常添加 @FunctionalInterface 注解供编译器校验。
         *
         * 【四大内置核心函数式接口】：
         * 1. Consumer<T>（消费型）：void accept(T t) -> 接收一个参数，无返回值。
         * 2. Supplier<T>（供给型）：T get() -> 无参数，返回一个结果对象。
         * 3. Function<T, R>（函数转换型）：R apply(T t) -> 接收一个参数，经过处理返回另一种类型的结果。
         * 4. Predicate<T>（断言判断型）：boolean test(T t) -> 接收一个参数，返回布尔值。
         */

        System.out.println("1. [四大核心内置函数式接口实战演示]:");

        // Consumer 消费型
        Consumer<String> consumer = msg -> System.out.println("   [Consumer] 消费处理数据: " + msg);
        consumer.accept("订单已创建");

        // Supplier 供给型
        Supplier<String> tokenSupplier = () -> "AUTH_TOKEN_" + System.currentTimeMillis();
        System.out.println("   [Supplier] 动态生成Token: " + tokenSupplier.get());

        // Function 函数型（转换）
        Function<String, Integer> stringLengthFunction = String::length;
        System.out.println("   [Function] 字符串长度转换: \"Antigravity\" -> " + stringLengthFunction.apply("Antigravity"));

        // Predicate 断言型
        Predicate<Integer> isAdult = age -> age >= 18;
        System.out.println("   [Predicate] 年龄判定 20 岁: " + isAdult.test(20));

        /*
         * 【变量捕获机制（Variable Capture）与闭包限制】：
         * 在 Lambda 表达式内部访问外部局部变量时，该变量必须被显式声明为 final，或者事实上不可变（effectively final）。
         * 为什么不能修改外部局部变量？
         * 原因剖析：
         * 1. 变量生命周期不一致：局部变量存储在栈帧中，方法执行完栈帧就会被弹出销毁；而 Lambda 可能会被传递到其他线程异步执行。
         * 2. Java 的实现机制是【值拷贝（Value Copy）】：Lambda 内部访问的局部变量实际上是该变量的一份副本！
         *    如果允许其中一方修改，另一方无法感知，必然导致内存数据不一致，因此 Java 强制规定必须只读！
         */
        System.out.println("\n2. [变量捕获测试：访问 effectively final 局部变量]");
        String prefix = "ORDER_PREFIX_"; // 未加 final，但在后续没有修改，属于 effectively final
        OrderValidator validator = (id, amt) -> amt > 0 && id.startsWith(prefix);
        System.out.println("   校验订单结果: " + validator.validate("ORDER_PREFIX_9988", 500.0));

        /*
         * 【底层字节码实现深度剖析（大厂面试加分项）】：
         * 问：Lambda 表达式是不是匿名内部类的语法糖？
         * 答：绝对不是！
         * 1. 匿名内部类机制：
         *    - 编译时会在磁盘生成独立的 class 文件（例如 Main$1.class）。
         *    - 每次运行时都需要 new 一个实例，占用堆内存和类元数据空间。
         * 2. Lambda 表达式底层机制：
         *    - 编译期：javac 绝不生成新的类文件，而是将 Lambda 方法体编译为当前类的一个【私有静态合成方法（private static synthetic method）】。
         *    - 运行时：通过 JVM 的【invokedynamic 指令】（自 Java 7 引入），
         *      在首次调用时触发 java.lang.invoke.LambdaMetafactory.metafactory() 引导方法，
         *      在内存中动态生成一个极其轻量的实现类并缓存对应的 CallSite 调用点！
         *    - 优势：大幅减少类编译输出、显著降低内存占用，且由 JVM JIT 深度优化方法内联！
         */
        System.out.println("\n3. [底层机制解析总结]:");
        System.out.println("   - 编译产物：javac 将 Lambda 提取为类内的私有静态方法，无多余的 $1.class 文件。");
        System.out.println("   - 运行调度：依赖 JVM invokedynamic 指令与 LambdaMetafactory 动态生成轻量 CallSite。");
    }
}
