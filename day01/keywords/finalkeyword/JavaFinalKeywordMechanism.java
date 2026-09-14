package keywords.finalkeyword;


/**
 * 面试专题：Java 中 final 关键字深度解析与底层原理解析。
 *
 * 本类对应面试核心题目：
 * Java 中 final 作用是什么?
 *
 * 核心考点涵盖：
 * 1. final 修饰类：禁止继承、安全防篡改与 JIT 虚方法内联优化（以 String、Integer 与 FinalSecurityConfig 为例）。
 * 2. final 修饰方法：禁止子类重写、锁定核心算法流程（以 BasePaymentProcessor 模板方法为例）。
 * 3. final 修饰变量：
 *    - 基本数据类型：数值变为常量，不可修改。
 *    - 引用数据类型：引用地址被锁死不可指向新对象，但对象内部成员属性依然可以自由修改（以 UserEntity 为例实测）。
 * 4. final 成员变量的初始化时机（显式赋值、构造块、构造函数，严禁默认零值）。
 * 5. final 与匿名内部类/Lambda 闭包捕获（effectively final 与生命周期解耦拷贝）。
 * 6. JMM 内存模型下的 final 域内存屏障（StoreStore 屏障保证多线程下的安全发布）。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaFinalKeywordMechanism {

    // 1. static final 全局编译期常量：类加载准备阶段直接赋予字面量，存入运行时常量池
    public static final String GLOBAL_APP_NAME = "CloudEnterpriseSystem";

    // 2. 空白 final 实例成员变量：必须在每一个构造函数退出前完成显式初始化
    private final String instanceId;

    public JavaFinalKeywordMechanism(String instanceId) {
        this.instanceId = instanceId;
    }

    public static void main(String[] args) {

        System.out.println("======================================================================");
        System.out.println("                  Java 中 final 关键字核心作用深度解析                ");
        System.out.println("======================================================================");

        explainFinalKeywordRole();

        System.out.println("\n======================================================================");
        System.out.println("             final 关键字解析完毕，请细读类中源码与注释               ");
        System.out.println("======================================================================");
    }

    /**
     * 核心解答方法：
     * Java 中 final 作用是什么?
     */
    public static void explainFinalKeywordRole() {
        System.out.println("Java 中 final 作用是什么?");

        /*
         * 维度一：final 修饰类（Class）
         * 1. 语法：该类无法被任何其他类继承。
         * 2. 安全价值：JDK 核心系统类（如 String, Integer, System）均声明为 final，
         *    防止恶意子类继承并重写关键逻辑破坏不可变约定。
         * 3. 性能优化：JIT 编译器能确切知道不会有子类派生，直接内联方法调用，消除虚方法分派开销。
         */
        System.out.println("\n--- 维度一：final 修饰类（防篡改与内联优化） ---");
        FinalSecurityConfig config = new FinalSecurityConfig("AES_SECRET_987654321", 7200);
        config.printConfigSummary();
        System.out.println("   实证结论：final 修饰的类（如 String、FinalSecurityConfig）绝不可被任何类继承。");

        /*
         * 维度二：final 修饰方法（Method）
         * 1. 语法：该方法可以被子类正常继承并调用，但严禁被子类重写（Override）。
         * 2. 设计模式价值：模板方法模式（Template Method Pattern）的核心支撑。
         *    父类提供一个 final 骨架调度方法，锁定业务主干生命周期，子类只能实现可扩展节点，不能篡改主干。
         */
        System.out.println("\n--- 维度二：final 修饰方法（锁定核心业务逻辑骨架） ---");
        SubPaymentProcessor processor = new SubPaymentProcessor();
        processor.executeTransactionFlow("云闪付通道", 1200.0);
        System.out.println("   实证结论：子类可重写非 final 方法 validateCustomParameters()，但绝无法重写 final 骨架方法 executeTransactionFlow()。");

        /*
         * 维度三：final 修饰变量（基本类型 vs 引用类型）—— 面试最大高频坑！
         * 1. 修饰基本类型：数值一旦确定，绝对不可更改。
         * 2. 修饰引用类型：
         *    - 引用所指向的堆内存地址（指针）不可变，不能重新 new 或者指向其他对象！
         *    - 但是！该引用所指向的对象【内部的成员属性完全可以被自由修改】！
         */
        System.out.println("--- 维度三：final 修饰变量（基本类型 vs 引用类型实证） ---");

        // 3.1 基本类型测试
        final int maxRetryCount = 3;
        // maxRetryCount = 5; // 编译报错：Cannot assign a value to final variable 'maxRetryCount'
        System.out.println("   [基本类型] final int 常量值固定为: " + maxRetryCount + "，不可重新赋值。");

        // 3.2 引用类型测试（实测内存地址锁定，但对象属性可变）
        final UserEntity user = new UserEntity("张三", 25);
        String originalAddress = "0x" + Integer.toHexString(System.identityHashCode(user)).toUpperCase();
        System.out.println("   [初始化对象] " + user + "，堆内存物理地址: " + originalAddress);

        // 尝试修改对象内部的属性：完全合法且成功！
        user.setName("李四");
        user.setAge(30);
        String afterModifyAddress = "0x" + Integer.toHexString(System.identityHashCode(user)).toUpperCase();
        System.out.println("   [修改属性后] " + user + "，堆内存物理地址: " + afterModifyAddress);
        System.out.println("   [地址比对结果] 修改前地址与修改后地址是否完全一致: " + originalAddress.equals(afterModifyAddress));

        /*
         * 错误代码示范：
         * 如果尝试将 user 重新指向新对象：
         * user = new UserEntity("王五", 28); // 编译报错：Cannot assign a value to final variable 'user'
         */
        System.out.println("   实证结论：final 修饰引用时，锁死的是引用的地址指针；对象内部属性是否可变取决于该属性自身是否为 final。");

        /*
         * 维度四：final 成员变量的初始化规则
         * 1. 成员变量被 final 修饰后，JVM 不会为其自动赋予默认零值（0/null/false）！
         * 2. 开发者必须在以下三个位置之一进行显式初始化赋值：
         *    - 在声明定义字段时直接赋值；
         *    - 在实例初始化代码块 { ... } 中赋值；
         *    - 在每一个构造方法中显式赋值（空白 final 变量）。
         */
        System.out.println("\n--- 维度四：final 成员变量初始化机制 ---");
        JavaFinalKeywordMechanism mechanism = new JavaFinalKeywordMechanism("NODE-CLUSTER-001");
        System.out.println("   空白 final 实例变量 instanceId 必须在构造函数中完成初始化: " + mechanism.instanceId);

        /*
         * 维度五：内部类与 Lambda 闭包捕获为什么必须是 final？
         * 1. 原因：局部变量存放在方法栈帧中，方法执行完毕栈帧销毁；而内部类对象存放在堆中，生命周期通常远长于方法栈。
         * 2. 底层实现：Java 编译器把外部局部变量作为内部类的成员变量拷贝了一份复制品传入！
         *    为了避免外部方法修改变量后内部类感知不到（或者内部类修改后外部方法感知不到），
         *    Java 强制要求闭包变量必须是 final（或 Java 8+ 的 effectively final 事实上不可变），保证数据一致性！
         */
        System.out.println("\n--- 维度五：闭包捕获变量的 final 约束原理 ---");
        String taskDesc = "数据清洗任务"; // effectively final 变量
        Runnable runnableTask = () -> {
            System.out.println("   [Lambda 闭包执行] 读取外部 effectively final 局部变量: " + taskDesc);
        };
        runnableTask.run();

        /*
         * 维度六：JMM 内存模型中的 final 内存屏障保障
         * 1. 在多线程并发环境下，普通变量的初始化可能被 CPU 指令重排序，导致其他线程读取到未完全初始化的半成品对象。
         * 2. JMM 对 final 域有特殊的可见性规则保证：
         *    在构造函数退出前，编译器会插入 StoreStore 内存屏障，强制先将 final 字段写入主存，
         *    只要没有发生 this 逃逸，其他线程在看到该对象引用的同时，必定能看到正确初始化后的 final 字段值！
         */
        System.out.println("\n--- 维度六：JMM 内存模型中 final 的安全发布保障 ---");
        System.out.println("   JMM 底层插入 StoreStore 内存屏障，彻底禁止构造函数内 final 域写操作重排序到构造函数之外，确保并发安全发布。");
    }
}
