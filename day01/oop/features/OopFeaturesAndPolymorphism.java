package oop.features;

/**
 * 面试专题：面向对象核心本质、三大特性、多态深度剖析与设计原则。
 *
 * 本类对应面试核心题目：
 * 1. 怎么理解面向对象?简单说说封装继承多态
 * 2. 多态体现在哪几个方面?
 * 3. 多态解决了什么问题?
 * 4. 面向对象的设计原则你知道有哪些吗
 * 5. 重载与重写有什么区别?
 *
 * 设计目标：
 * 遵循企业级开发规范，从现实模型抽象、JVM 动态分派虚方法表（vtable），
 * 到开闭原则重构消灭 if-else，全面建立面向对象架构设计与底层执行认知。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class OopFeaturesAndPolymorphism {

    public static void main(String[] args) {
        initConsoleEncoding();

        System.out.println("======================================================================");
        System.out.println("        面向对象三大特性、多态本质、设计原则与重载重写深度解析        ");
        System.out.println("======================================================================");

        explainOopAndThreeFeatures();
        explainPolymorphismAspects();
        explainWhatProblemPolymorphismSolves();
        explainOopDesignPrinciples();
        explainOverloadVsOverride();

        System.out.println("\n======================================================================");
        System.out.println("             面向对象核心理论解析完毕，请细读类中源码与注释           ");
        System.out.println("======================================================================");
    }

    /**
     * 第一部分：怎么理解面向对象?简单说说封装继承多态
     *
     * 面试核心考点：面向过程 vs 面向对象思维对比；三大特性的定义与核心价值。
     */
    public static void explainOopAndThreeFeatures() {
        System.out.println("1. 面向对象（OOP）思维与三大核心特性解析：");

        /*
         * 1. 怎么理解面向对象（OOP - Object Oriented Programming）？
         *    - 经典生活对比：【洗衣服】
         *      * 面向过程（POP）：把洗衣服拆成一系列连续的步骤函数：
         *        放水() -> 浸泡() -> 放洗衣粉() -> 手工揉搓() -> 拧干() -> 晾晒()。
         *        侧重于“步骤的按部就班”，数据与处理函数分离，系统庞大时牵一发而动全身。
         *      * 面向对象（OOP）：将世界抽象为一个个互相协同的实体“对象”：
         *        创建【洗衣机】对象，创建【人】对象，创建【衣服】对象。
         *        人.把衣服放进(洗衣机); 洗衣机.开启洗涤模式();
         *        侧重于“谁来做这件事”，将数据（属性）与行为（方法）高度绑定成一个自治单元。
         *
         * 2. 三大特性之【封装（Encapsulation）】：
         *    - 概念：将对象的内部状态属性私有化（private），对外只暴露公共安全的访问接口（getter/setter）。
         *    - 价值：防止外部代码随意篡改内部数据，保障高内聚与数据安全。
         */
        BankAccount account = new BankAccount("张三", 1000.0);
        account.deposit(500.0); // 合法存款
        account.withdraw(2000.0); // 内部校验拦截：余额不足！

        /*
         * 3. 三大特性之【继承（Inheritance）】：
         *    - 概念：子类通过 extends 关键字自动继承父类的非私有属性和方法（is-a 关系）。
         *    - 价值：大幅提升代码复用性，也是实现运行时多态的前提。
         */

        /*
         * 4. 三大特性之【多态（Polymorphism）】：
         *    - 概念：同一行为指令，在不同具体子类对象上产生不同的业务表现。
         *    - 价值：接口与实现解耦，使软件具备极致的扩展能力。
         */
        System.out.println("   [封装实测] 账户余额当前受保护值: " + account.getBalance());
        System.out.println("   [三大特性总结] 封装是基础（藏数据），继承是手段（复用代码），多态是精髓（解耦合）。");
    }

    /**
     * 第二部分：多态体现在哪几个方面?
     *
     * 面试核心考点：编译期多态（静态多态） vs 运行期多态（动态多态）。
     */
    public static void explainPolymorphismAspects() {
        System.out.println("\n2. 多态体现在哪几个方面？");

        /*
         * 维度一：编译时多态（静态多态）—— 方法重载（Overload）
         *   - 发生时机：编译期。
         *   - 体现方式：同一个类中方法名相同，参数列表不同。
         *   - 机制：编译器在编译阶段根据传入参数的静态类型，直接静态绑定要调用的方法签名。
         */
        Calculator calc = new Calculator();
        System.out.println("   [编译时多态/重载] add(int, int) = " + calc.add(10, 20));
        System.out.println("   [编译时多态/重载] add(double, double) = " + calc.add(1.5, 2.5));

        /*
         * 维度二：运行时多态（动态多态）—— 方法重写（Override）+ 父类引用指向子类对象
         *   - 发生时机：运行期。
         *   - 必要三要素：1. 存在继承或接口实现；2. 子类重写父类方法；3. 父类引用指向子类对象。
         *   - 机制：JVM 在运行期通过 invokevirtual 字节码指令，根据栈中对象的真实堆内存类型，
         *     动态查找其虚方法表（vtable）执行具体子类的方法。
         */
        Animal myDog = new Dog(); // 父类引用指向子类对象
        Animal myCat = new Cat();

        System.out.print("   [运行时多态/重写] myDog.makeSound() -> ");
        myDog.makeSound(); // 输出汪汪汪
        System.out.print("   [运行时多态/重写] myCat.makeSound() -> ");
        myCat.makeSound(); // 输出喵喵喵

        /*
         * 维度三：接口多态
         *   - 接口引用指向不同实现类对象（如 List list = new ArrayList();）。
         */
    }

    /**
     * 第三部分：多态解决了什么问题?
     *
     * 面试拔高考点：开闭原则（OCP）落地，消灭恶心的 if-else / switch-case。
     */
    public static void explainWhatProblemPolymorphismSolves() {
        System.out.println("\n3. 多态解决了什么问题？");

        /*
         * 1. 痛点场景：如果没有多态，扩展一个新业务会有多灾难？
         *    - 假设做支付系统，没有多态时代码往往写成：
         *      if ("alipay".equals(type)) { payByAlipay(); }
         *      else if ("wechat".equals(type)) { payByWechat(); }
         *      else if ("union".equals(type)) { payByUnion(); }
         *    - 致命缺陷：每增加一种新支付方式（如数字人民币），就必须修改核心业务类加一个 else if，
         *      严重违背“开闭原则”，测试回归成本极高，极易引发生产事故！
         *
         * 2. 多态的优雅解决：
         *    - 定义统一支付接口 PaymentService，调用方只依赖接口。
         *    - 增加新支付方式时，只需新建一个实现类即可，原有调用逻辑【一行都不需要改动】！
         */
        PaymentService payment1 = new AlipayService();
        PaymentService payment2 = new WechatPayService();

        processPayment(payment1, 199.0);
        processPayment(payment2, 299.0);

        System.out.println("   [核心价值] 1. 解除模块硬编码依赖；2. 消灭庞大冗余的 if-else；3. 完美支撑开闭原则（对扩展开放，对修改关闭）。");
    }

    private static void processPayment(PaymentService paymentService, double amount) {
        // 调用方完全不知道具体是哪种支付，只管调用 pay 抽象行为
        paymentService.pay(amount);
    }

    /**
     * 第四部分：面向对象的设计原则你知道有哪些吗
     *
     * 面试核心考点：SOLID 原则 + 迪米特法则 + 组合优于继承原则。
     */
    public static void explainOopDesignPrinciples() {
        System.out.println("\n4. 面向对象经典设计原则（SOLID 五大原则 + 拓展两大原则）：");

        /*
         * 1. S - 单一职责原则（Single Responsibility Principle, SRP）
         *    - 核心：一个类只负责一项职责，只有一个引起它变化的原因。
         *    - 反例：一个类既负责用户登录，又负责发短信，还负责生成财务报表。
         *
         * 2. O - 开闭原则（Open-Closed Principle, OCP）—— 【设计模式的终极总纲】
         *    - 核心：软件实体（类、模块、函数）应该【对扩展开放，对修改关闭】。
         *    - 做法：通过抽象建立框架，通过具体实现扩展功能，新增功能不改动旧代码。
         *
         * 3. L - 里氏替换原则（Liskov Substitution Principle, LSP）
         *    - 核心：子类必须能够完全替换掉它们的父类，而程序的行为不产生异常。
         *    - 警戒：子类尽量不要重写父类已实现的具体非抽象方法，否则容易破坏原有契约（经典反例：正方形继承长方形）。
         *
         * 4. I - 接口隔离原则（Interface Segregation Principle, ISP）
         *    - 核心：客户端不应该被迫依赖于它不使用的方法。
         *    - 做法：建立单一、精炼的专有接口，避免建立臃肿庞大的“万能胖接口”。
         *
         * 5. D - 依赖倒置原则（Dependency Inversion Principle, DIP）
         *    - 核心：高层模块不应该依赖低层模块，二者都应该依赖抽象；抽象不应该依赖细节，细节应该依赖抽象。
         *    - 落地：在 Spring 中，Controller 依赖 Service 接口，而不直接 new 具体实现类。
         *
         * 6. 迪米特法则 / 最少知识原则（Law of Demeter, LoD）
         *    - 核心：只与你的直接朋友交谈，不与“陌生人”说话。降低类之间的耦合度。
         *
         * 7. 合成复用原则（Composite Reuse Principle, CRP）
         *    - 核心：优先使用对象组合（Composition）或聚合，尽量少用继承（Inheritance）。
         */
        System.out.println("   SOLID 原则：单一职责(SRP)、开闭原则(OCP)、里氏替换(LSP)、接口隔离(ISP)、依赖倒置(DIP)。");
        System.out.println("   补充原则：最少知识法则(LoD / 迪米特法则)、合成复用原则(CRP / 组合优于继承)。");
    }

    /**
     * 第五部分：重载与重写有什么区别?
     *
     * 面试核心考点：多维度横向对照表。
     */
    public static void explainOverloadVsOverride() {
        System.out.println("\n5. 重载（Overload）与 重写（Override）全维度深度对比：");

        /*
         * -----------------------------------------------------------------------------------------
         * 对比维度             重载（Overload）                     重写（Override）
         * -----------------------------------------------------------------------------------------
         * 发生位置             同一个类中                          父子类之间
         * 方法名称             必须完全相同                        必须完全相同
         * 参数列表             必须不同（个数、类型、顺序至少一个不同） 必须完全相同
         * 返回值类型           可以相同，也可以不同（不能以此区分重载）  必须相同，或是父类返回类型的子类（协变返回）
         * 抛出异常范围         可以任意改变                        只能更小或相同，绝不能抛出比父类更宽泛的受检异常
         * 访问权限修饰符       可以任意改变                        子类权限只能更大或相同（如父类 protected，子类必须 protected 或 public）
         * 底层执行分派机制     编译期静态分派（Static Dispatch）    运行期动态分派（Dynamic Dispatch，查虚方法表）
         * -----------------------------------------------------------------------------------------
         */
        System.out.println("   重载(Overload)：同名不同参，编译期静态决定；");
        System.out.println("   重写(Override)：父子同名同参，运行期动态分派。两同两小一大原则（方法名参相同，返回异常更小，权限更大）。");
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
