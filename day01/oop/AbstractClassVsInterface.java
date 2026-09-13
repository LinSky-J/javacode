package oop;

/**
 * 面试专题：抽象类与接口深度辨析、方法演进与实例化底层原理。
 *
 * 本类对应面试核心题目：
 * 6. 抽象类和普通类区别?
 * 7. Java抽象类和接口的区别是什么?
 * 8. 抽象类能加final修饰吗?
 * 9. 接口里面可以定义哪些方法?
 * 10. 抽象类可以被实例化吗?
 * 11. 接口可以包含构造函数吗?
 *
 * 设计目标：
 * 遵循企业级开发规范，从 Java 8/9 接口特性演进、构造函数链调用，
 * 到抽象类匿名内部类实例化的本质，建立透彻而严密的面向对象抽象认知。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class AbstractClassVsInterface {

    public static void main(String[] args) {
        initConsoleEncoding();

        System.out.println("======================================================================");
        System.out.println("         抽象类 vs 接口 全维度对比、方法体系与实例化深度解析          ");
        System.out.println("======================================================================");

        explainAbstractVsNormalClass();
        explainAbstractVsInterface();
        explainCanAbstractClassHaveFinal();
        explainMethodsInInterface();
        explainCanAbstractClassBeInstantiated();
        explainCanInterfaceHaveConstructor();

        System.out.println("\n======================================================================");
        System.out.println("             抽象类与接口解析完毕，请细读类中源码与注释               ");
        System.out.println("======================================================================");
    }

    /**
     * 第六部分：抽象类和普通类区别?
     *
     * 面试核心考点：实例化能力、抽象方法支持、设计定位。
     */
    public static void explainAbstractVsNormalClass() {
        System.out.println("6. 抽象类与普通类的核心区别：");

        /*
         * 1. 实例化能力：
         *    - 普通类：有完整的实现，可以直接通过 new 关键字实例化对象。
         *    - 抽象类：由 abstract 修饰，【绝不能直接 new 实例化】！
         *
         * 2. 抽象方法支持：
         *    - 普通类：绝对不允许声明任何没有方法体的 abstract 抽象方法。
         *    - 抽象类：可以包含 0 个、1 个或多个 abstract 抽象方法；也可以全是具体实现方法。
         *
         * 3. 设计目的与定位：
         *    - 普通类：用于描述具体的、完整的业务概念。
         *    - 抽象类：用于抽取子类的公共特征和通用模板（模板方法模式），强制要求子类按规范去实现特异性逻辑。
         */
        System.out.println("   区别一 [实例化]：普通类可直接 new；抽象类严禁直接 new 实例化。");
        System.out.println("   区别二 [抽象方法]：普通类不能有抽象方法；抽象类可以包含 abstract 抽象方法。");
        System.out.println("   区别三 [设计定位]：普通类描述具体业务实体；抽象类作为公共模板基类强制规范子类行为。");
    }

    /**
     * 第七部分：Java抽象类和接口的区别是什么?
     *
     * 面试超高频必考题：设计理念、继承规则、变量类型、构造方法对比。
     */
    public static void explainAbstractVsInterface() {
        System.out.println("\n7. Java 抽象类与接口的全方位对比：");

        /*
         * -----------------------------------------------------------------------------------------
         * 比较维度         抽象类（Abstract Class）            接口（Interface）
         * -----------------------------------------------------------------------------------------
         * 设计思想理念     is-a（是什么，具有强烈的血缘所属）    like-a / can-do（具备什么能力契约）
         * 继承/实现规则    单继承（一个类只能 extends 一个抽象类）多实现（一个类可 implements 多个接口）
         * 成员变量支持     可以有任何类型的普通成员变量（可变/私有）只能是 public static final 常量
         * 构造方法         有构造函数（供子类 super() 调用初始化） 绝对没有构造函数！
         * 方法体支持       可以随意编写具体方法与模板方法        Java 8 前无方法体；Java 8+ 支持 default/static
         * 运行效率         继承调用效率略高                    接口动态绑定（invokeinterface）开销略微多一点点
         * -----------------------------------------------------------------------------------------
         */
        System.out.println("   设计理念：抽象类是 is-a（表示血缘和通用状态）；接口是 can-do（表示行为规范与契约）。");
        System.out.println("   继承实现：抽象类单继承（Java 类的单继承局限）；接口支持多实现，弥补了单继承缺陷。");
        System.out.println("   成员字段：抽象类可有普通私有可变字段；接口只有 public static final 全局常量。");
    }

    /**
     * 第八部分：抽象类能加final修饰吗?
     *
     * 面试核心考点：语法矛盾与语义冲突。
     */
    public static void explainCanAbstractClassHaveFinal() {
        System.out.println("\n8. 抽象类能加 final 修饰吗？");

        /*
         * 结论：【绝对不能！】编译器会直接报语法错误！
         *
         * 根本原因（语义针锋相对、完全矛盾）：
         * 1. abstract 的核心使命是什么？
         *    - 抽象类生来就是“不完整的”，它的存在价值就是【必须被子类继承，必须由子类去实现其抽象方法】。
         * 2. final 的核心语义是什么？
         *    - final 修饰类表示【该类已达终态，绝对禁止被任何子类继承】！
         * 3. 冲突：
         *    一个既要求“必须被继承”（abstract），又要求“严禁被继承”（final）的类在逻辑上是完全悖论！
         *    同理，抽象方法（abstract method）也绝对不能加 final、private、static 修饰！
         */
        System.out.println("   结论：绝对不能！abstract（必须被继承）与 final（严禁被继承）语义完全矛盾，编译器直接报错！");
    }

    /**
     * 第九部分：接口里面可以定义哪些方法?
     *
     * 面试现代考点：Java 7/8/9 接口特性的演进全貌。
     */
    public static void explainMethodsInInterface() {
        System.out.println("\n9. 接口里面可以定义哪些方法？（Java 7 / 8 / 9 演进全貌）");

        /*
         * 1. 抽象方法（从 Java 1.0 开始）：
         *    - 默认且隐式为 public abstract void foo(); 无方法体，实现类必须实现。
         *
         * 2. default 默认方法（从 Java 8 引入）：
         *    - 允许在接口中定义带有方法体（{...}）的方法！
         *    - 引入背景：为了给已有接口平滑增加新功能（例如在 Collection 接口中增加 stream() 方法），
         *      而无需强迫上万个旧实现类去一一改动实现它。
         *
         * 3. static 静态方法（从 Java 8 引入）：
         *    - 允许在接口中定义静态工具方法，直接通过 InterfaceName.method() 调用。
         *
         * 4. private 私有方法（从 Java 9 引入）：
         *    - 接口内部可定义 private 普通方法和 private static 静态方法。
         *    - 作用：抽取多个 default 方法中的公共代码进行复用，且不对外暴露细节。
         */

        // 接口方法调用实测
        PaymentPlugin plugin = new FastPayPlugin();
        plugin.executePayment(500.0); // 调用实现类的抽象方法
        plugin.logTransaction("支付完成"); // 调用 Java 8 的 default 默认方法
        PaymentPlugin.printPluginVersion(); // 调用 Java 8 的 static 静态方法

        System.out.println("   接口方法演进：1. 抽象方法(Java 1+)；2. default 默认方法(Java 8)；3. static 静态方法(Java 8)；4. private 私有方法(Java 9)。");
    }

    /**
     * 第十部分：抽象类可以被实例化吗?
     *
     * 面试深度回答：直接实例化 vs 匿名内部类 vs 构造函数级联。
     */
    public static void explainCanAbstractClassBeInstantiated() {
        System.out.println("\n10. 抽象类可以被实例化吗？");

        /*
         * 1. 标准结论：
         *    - 抽象类【不能被直接实例化】（new AbstractClass() 会直接报编译错误）。
         *
         * 2. 很多人见过的“new 抽象类”到底是怎么回事？
         *    - 常见写法：TemplateTask task = new TemplateTask() { ... };
         *    - 本质揭秘：这并不是实例化了抽象类本身！而是 Java 编译器在底层悄悄创建了一个
         *      【继承自该抽象类的无名具体子类（匿名内部类）】，并实例化了这个子类！
         *
         * 3. 抽象类有构造函数吗？有！
         *    - 子类在 new 实例化时，子类构造函数第一行会默认执行 super()，
         *      级联调用抽象父类的构造方法，用于初始化抽象父类中声明的成员变量！
         */

        // 匿名内部类代码实测
        TemplateTask anonymousTask = new TemplateTask("数据导出任务") {
            @Override
            public void executeJob() {
                System.out.println("   [匿名内部类执行] 正在执行具体的数据导出业务逻辑...");
            }
        };
        anonymousTask.start();

        System.out.println("   结论：抽象类自身绝不能直接实例化；匿名内部类本质是实例化了一个隐式的子类。");
    }

    /**
     * 第十一部分：接口可以包含构造函数吗?
     *
     * 面试核心考点：状态初始化能力与语言契约规范。
     */
    public static void explainCanInterfaceHaveConstructor() {
        System.out.println("\n11. 接口可以包含构造函数吗？");

        /*
         * 结论：【绝对不能包含构造函数！】
         *
         * 深度原因解析：
         * 1. 构造函数的根本作用是什么？
         *    - 构造函数是用来【初始化对象实例变量的内存状态】的。
         * 2. 接口有没有实例变量？
         *    - 接口没有任何实例成员变量！接口里的字段全是 public static final 静态全局常量，
         *      它们在类加载阶段就已经初始化好了。
         * 3. 语义定位：
         *    - 接口是一套纯粹的行为契约与规范，没有任何属于自己的“对象实例状态”需要被构造。
         *    - 试图在接口中写 interface Foo { Foo(); } 编译器会直接报错！
         */
        System.out.println("   结论：绝对不能！接口无任何实例变量状态需要构造，纯粹是行为规范契约。");
    }

    // ================================= 辅助接口与抽象类演示 =================================

    /**
     * 演示接口：涵盖 Java 7、8、9 的完整方法体系
     */
    interface PaymentPlugin {
        // 1. 全局静态常量（隐式 public static final）
        String PLUGIN_NAME = "EnterprisePaymentPlugin";

        // 2. 抽象方法（Java 1.0+）
        void executePayment(double amount);

        // 3. 默认方法（Java 8 引入）
        default void logTransaction(String message) {
            String logPrefix = getLogPrefix(); // 调用 Java 9 私有辅助方法
            System.out.println("   [接口 default 方法] " + logPrefix + " - " + message);
        }

        // 4. 静态方法（Java 8 引入）
        static void printPluginVersion() {
            System.out.println("   [接口 static 方法] 当前插件名称: " + PLUGIN_NAME + "，版本: 3.0");
        }

        // 5. 私有方法（Java 9 引入，供 default 方法内部复用）
        private String getLogPrefix() {
            return "[AUDIT-LOG-" + System.currentTimeMillis() + "]";
        }
    }

    static class FastPayPlugin implements PaymentPlugin {
        @Override
        public void executePayment(double amount) {
            System.out.println("   [实现类方法] 快捷支付扣款: " + amount + " 元完成");
        }
    }

    /**
     * 演示抽象类：包含构造方法、成员变量、模板方法
     */
    static abstract class TemplateTask {
        private String taskName; // 抽象类可以有普通私有成员变量

        // 抽象类完全可以有构造函数，供子类 super() 调用
        public TemplateTask(String taskName) {
            this.taskName = taskName;
            System.out.println("   [抽象父类构造方法触发] 正在初始化任务名称: " + taskName);
        }

        // 模板方法：定义执行流程
        public void start() {
            System.out.println("   [抽象类模板方法] 准备启动任务: " + taskName);
            executeJob(); // 调用抽象方法
            System.out.println("   [抽象类模板方法] 任务结束: " + taskName);
        }

        // 抽象方法：强制子类实现
        public abstract void executeJob();
    }

    /**
     * 初始化控制台字符编码，解决 Windows 环境终端输出中文乱码的问题。
     */
    private static void initConsoleEncoding() {
        try {
            System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}
