package oop.staticinner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * 面试专题：静态机制、静态内部类 vs 非静态内部类及编译器底层合成原理。
 *
 * 本类对应面试核心题目：
 * 12. 解释Java中的静态变量和静态方法
 * 13. 非静态内部类和静态内部类的区别？
 * 14. 非静态内部类可以直接访问外部方法，编译器是怎么做到的?
 *
 * 设计目标：
 * 遵循企业级开发规范，从 JVM 内存共享模型、静态/非静态内部类生命周期与内存泄漏隐患，
 * 到利用反射现场逆向抓取编译器合成的 `this$0` 隐式字段，讲透内部类底层真实运行机制。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class StaticAndInnerClassesMechanism {

    public static void main(String[] args) {
        initConsoleEncoding();

        System.out.println("======================================================================");
        System.out.println("     Java 静态机制、内部类差异与编译器 this$0 隐式传参底层原理解析     ");
        System.out.println("======================================================================");

        explainStaticVariableAndMethod();
        explainInnerClassDifferences();
        explainHowCompilerEnablesInnerAccess();

        System.out.println("\n======================================================================");
        System.out.println("             静态与内部类机制解析完毕，请细读类中源码与注释           ");
        System.out.println("======================================================================");
    }

    /**
     * 第十二部分：解释Java中的静态变量和静态方法
     *
     * 面试核心考点：类属性 vs 实例属性；加载时机、内存分配与使用限制。
     */
    public static void explainStaticVariableAndMethod() {
        System.out.println("12. 静态变量（Static Variable）与 静态方法（Static Method）深度解析：");

        /*
         * 1. 静态变量（类变量）：
         *    - 归属：由 static 修饰，属于【类本身】，而不属于任何具体的对象实例。
         *    - 内存与生命周期：在 JVM 类加载的准备阶段完成内存分配与默认初始化（JDK 8+ 存放于堆中 Class 对象末尾）。
         *    - 特征：整个 JVM 进程中只有一份唯一的内存副本，所有该类的实例对象共享同一个静态变量。
         *
         * 2. 静态方法（类方法）：
         *    - 归属：属于【类本身】，无需 new 实例化即可通过 ClassName.methodName() 直接调用。
         *    - 核心三大使用限制：
         *      * 限制一：静态方法内部【绝对不能使用 this 或 super 关键字】（因为静态方法被调用时，可能根本没有实例存在）。
         *      * 限制二：静态方法【不能直接访问非静态的实例变量或实例方法】（没有具体的实例对象上下文）。
         *      * 限制三：静态方法不能被重写（Override）实现多态，只能被隐藏（Hide）。
         */
        CompanyEmployee.companyName = "未来科技有限公司"; // 直接通过类名访问静态变量

        CompanyEmployee emp1 = new CompanyEmployee("张三");
        CompanyEmployee emp2 = new CompanyEmployee("李四");

        System.out.println("   [共享测试] emp1 的公司名称: " + emp1.getCompany());
        System.out.println("   [共享测试] emp2 的公司名称: " + emp2.getCompany());

        // 修改静态变量，所有实例同步感知！
        CompanyEmployee.companyName = "全球创新实验室";
        System.out.println("   [修改类变量后] emp1 感知到公司更名为: " + emp1.getCompany() + " (验证全实例共享唯一内存！)");

        // 调用静态方法
        CompanyEmployee.printCompanyNotice("全体员工明天准时参加技术分享会");
    }

    /**
     * 第十三部分：非静态内部类和静态内部类的区别？
     *
     * 面试核心考点：依赖关系、创建语法、访问权限与生产级内存泄漏（Memory Leak）隐患。
     */
    public static void explainInnerClassDifferences() {
        System.out.println("\n13. 非静态内部类 vs 静态内部类 全维度对比：");

        /*
         * -----------------------------------------------------------------------------------------
         * 比较维度         静态内部类（Static Nested Class）    非静态内部类（Inner Class / 成员内部类）
         * -----------------------------------------------------------------------------------------
         * static 声明     有 static 修饰                     无 static 修饰
         * 外部类实例依赖   【完全独立】，不需要外部类实例即可创建   【绝对依赖】，必须依附于一个已存在的外部类实例
         * 实例化语法       new Outer.StaticInner()            outerInstance.new MemberInner()
         * 外部成员访问权限 只能直接访问外部类的【静态成员】          能直接访问外部类的【所有成员（含 private）】
         * 外部引用持有     【不持有】外部类的引用指针           【隐式强引用持有】外部类实例的 this 指针
         * 内存泄漏风险     安全，绝不会导致外部类内存泄漏        极易在多线程/长生命周期（如 Handler）中引发内存泄漏！
         * -----------------------------------------------------------------------------------------
         */

        // 静态内部类创建演示（直接独立创建，无需外部类实例）
        OuterClass.StaticNestedClass staticInner = new OuterClass.StaticNestedClass();
        staticInner.display();

        // 非静态内部类创建演示（必须依赖外部类实例）
        OuterClass outer = new OuterClass("外部宿主对象A");
        OuterClass.NonStaticInnerClass nonStaticInner = outer.new NonStaticInnerClass();
        nonStaticInner.display();

        System.out.println("   [生产避坑指南] 阿里规约建议：如果内部类不需要访问外部类的非静态成员，");
        System.out.println("   必须优先声明为 static 静态内部类，以彻底切断隐式强引用，避免对象无法回收导致内存泄漏！");
    }

    /**
     * 第十四部分：非静态内部类可以直接访问外部方法，编译器是怎么做到的?
     *
     * 面试拔高终极考点：底层字节码 this$0 隐式字段合成揭秘。
     */
    public static void explainHowCompilerEnablesInnerAccess() {
        System.out.println("\n14. 编译器底层解密：非静态内部类为什么能直接访问外部私有方法？");

        /*
         * 很多人以为这是 JVM 的某种“特异功能”，真相是：
         * 这是 Java 编译器（javac）在背后施展的【语法糖编译替换操作】！
         *
         * 编译器底层的三大幕后操作：
         *
         * 操作一：合成隐式字段 `this$0`
         *   - 在编译非静态内部类（Outer$NonStaticInnerClass.class）时，编译器会在其内部悄悄
         *     生成一个私有的最终成员变量：`final OuterClass this$0;`。
         *
         * 操作二：改写构造方法隐式传参
         *   - 编译器会自动重写内部类的所有构造函数，将外部类的实例作为【第一个隐式参数】传进去：
         *     `NonStaticInnerClass(OuterClass this$0)`
         *   - 并在构造方法首行执行：`this.this$0 = this$0;`，将外部类实例死死保存在当前对象内部！
         *
         * 操作三：方法调用重定向与桥接方法
         *   - 当你在内部类写 `doSomething()` 时，编译器在编译期自动替换为：`this.this$0.doSomething();`。
         *   - 如果外部方法是 private 私有的，编译器还会在外部类自动合成一个包级私有的静态桥接方法
         *     （如 `access$000(OuterClass x)`），完成对私有方法的安全穿透访问。
         */

        // 用代码现场实测：通过 Java 反射机制抓取编译器偷偷合成的 `this$0` 字段！
        OuterClass outerInstance = new OuterClass("实证宿主对象B");
        OuterClass.NonStaticInnerClass innerInstance = outerInstance.new NonStaticInnerClass();

        System.out.println("\n--- [Java 反射现场抓包证据] 检索非静态内部类的所有物理成员字段 ---");
        Field[] fields = OuterClass.NonStaticInnerClass.class.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            try {
                Object fieldValue = f.get(innerInstance);
                System.out.println("   发现内部类物理字段名称: " + f.getName() + " (类型: " + f.getType().getSimpleName() + "), 当前持有的外部引用: " + fieldValue);
            } catch (Exception e) {
                System.out.println("   字段读取失败: " + e.getMessage());
            }
        }

        System.out.println("\n--- [Java 反射现场抓包证据] 检索非静态内部类的构造方法签名 ---");
        Constructor<?>[] constructors = OuterClass.NonStaticInnerClass.class.getDeclaredConstructors();
        for (Constructor<?> c : constructors) {
            System.out.println("   发现内部类物理构造器签名: " + c.toString());
            System.out.println("   (铁证如山：编译器在构造器第一个参数中悄悄塞入了外部类 OuterClass 的引用！)");
        }
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
