package keywords.statickeyword;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

// 演示 static import 静态导包：直接导入 Math 类的静态常量与静态方法
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

/**
 * 面试专题：Java 中 static 关键字深度解析与底层原理解析。
 *
 * 本类对应面试核心题目：
 * Java 中 static的作用是什么?
 *
 * 核心考点涵盖：
 * 1. static 修饰变量（静态变量/类变量）：属于类本身，全实例共享，内存分配在元空间/堆中的 Class 对象末尾，生命周期贯穿类加载到卸载。
 * 2. static 修饰方法（静态方法/类方法）：直接类名调用，严禁使用 this/super，禁止直接访问实例成员，重写实质是“方法隐藏”（Method Hiding）。
 * 3. static 修饰代码块（静态初始化块）：类加载的初始化阶段执行且仅执行一次，实测验证类加载与对象实例化的先后时序。
 * 4. static 修饰内部类（静态内部类）：不持有外部宿主对象的隐式指针（无 this$0），杜绝内存泄露。
 * 5. static import 静态导入机制：简化静态成员调用。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaStaticKeywordMechanism {

    public static void main(String[] args) {
        initConsoleEncoding();

        System.out.println("======================================================================");
        System.out.println("                 Java 中 static 关键字核心作用深度解析                ");
        System.out.println("======================================================================");

        explainStaticKeywordRole();

        System.out.println("\n======================================================================");
        System.out.println("             static 关键字解析完毕，请细读类中源码与注释              ");
        System.out.println("======================================================================");
    }

    /**
     * 核心解答方法：
     * Java 中 static的作用是什么?
     */
    public static void explainStaticKeywordRole() {
        System.out.println("Java 中 static的作用是什么?");

        /*
         * 维度一：static 修饰变量（静态变量 / 类变量）
         * 1. 归属性：属于类本身，而不是某一个具体的对象实例。
         * 2. 内存共享：该类的所有对象实例共享同一份静态变量内存（JDK 8+ 存放于堆中该类的 Class 对象末尾）。
         * 3. 生命周期：随着类的加载而创建，随着类的卸载而销毁，生命周期远长于普通堆对象。
         * 4. 访问方式：直接通过 类名.变量名 访问，无需实例化对象。
         */
        System.out.println("\n--- 维度一：static 修饰变量（全类实例共享唯一内存） ---");
        System.out.println("   [初始状态] 未创建任何节点时，直接读取类变量: ServerNode.totalOnlineConnections = " + ServerNode.totalOnlineConnections);

        // 创建两个独立的节点对象
        ServerNode nodeA = new ServerNode("Node-East-01");
        ServerNode nodeB = new ServerNode("Node-East-02");

        // 分别在两个节点上接入用户
        nodeA.acceptConnection("Alice");
        nodeA.acceptConnection("Bob");
        nodeB.acceptConnection("Charlie");

        System.out.println("   [实测验证] nodeA 本地连接数: " + nodeA.getLocalConnections() + "，nodeB 本地连接数: " + nodeB.getLocalConnections());
        System.out.println("   [实测验证] 静态共享变量 ServerNode.totalOnlineConnections = " + ServerNode.totalOnlineConnections + " (全实例累计共享！)");

        /*
         * 维度二：static 修饰方法（静态方法 / 类方法）
         * 1. 归属性：属于类本身，直接通过 类名.方法名() 调用。
         * 2. 核心语法限制（面试必考）：
         *    - 静态方法内部【绝对不能出现 this 或 super 关键字】（因为静态方法被调用时堆中可能根本不存在对象实例！）。
         *    - 静态方法【不能直接调用非静态成员变量或非静态方法】；但非静态方法可以随意调用静态成员。
         *    - 静态方法【不能被子类真正重写（Override）】！如果子类定义了同名同参的静态方法，在底层称为“方法隐藏”（Hiding），
         *      调用哪个方法完全取决于编译期声明的静态引用类型，无法享受运行期动态多态虚方法分派。
         */
        System.out.println("\n--- 维度二：static 修饰方法（类级别直接调用与禁忌） ---");
        ServerNode.broadcastNotice("机房双十一流量压测即将开始！");
        System.out.println("   核心禁忌提醒：静态方法内禁止使用 this/super，禁止直接访问实例字段（如 nodeId）。");

        /*
         * 维度三：static 修饰代码块（静态初始化块）
         * 1. 执行时机：在 JVM 执行类加载机制的“初始化”（Initialization）阶段执行，早于任何对象的构造。
         * 2. 执行频次：在整个 JVM 运行生命周期内，【只执行且仅执行一次】！
         * 3. 经典初始化执行顺序：
         *    父类静态变量/静态块 -> 子类静态变量/静态块 -> 父类实例变量/实例块 -> 父类构造函数 -> 子类实例变量/实例块 -> 子类构造函数。
         */
        System.out.println("\n--- 维度三：static 代码块执行顺序回顾 ---");
        System.out.println("   上面已实测控制台输出：");
        System.out.println("   1. 首先触发 [静态初始化阶段] ServerNode 静态代码块（仅一次）");
        System.out.println("   2. 每次 new 对象时依次触发 [实例初始化阶段] 实例代码块");
        System.out.println("   3. 最后执行 [构造函数阶段] ServerNode 构造函数");

        /*
         * 维度四：static 修饰内部类（静态嵌套类 Static Nested Class）
         * 1. 语法：只能修饰成员内部类，不能修饰顶级外部类。
         * 2. 本质区别：
         *    - 非静态内部类隐式持有外部类对象的强引用（this$0）。
         *    - 静态内部类完全独立，不持有任何外部类引用，无需创建外部类实例即可直接 new 出来！
         * 3. 架构优势：完全切断生命周期捆绑，彻底避免内存泄漏。
         */
        System.out.println("\n--- 维度四：static 修饰内部类（独立自治组件与防泄漏） ---");
        // 无需 new OuterContainer()，直接独立实例化静态内部类
        OuterContainer.StaticComponent component = new OuterContainer.StaticComponent("核心监控引擎");
        component.printInfo();
        System.out.println("   实证结论：静态内部类无需外部类实例即可独立存在，内部没有 this$0 强引用，有效杜绝内存泄漏。");

        /*
         * 维度五：static import 静态导入机制（Java 5+）
         * 1. 作用：直接将一个类的静态成员（常量或方法）导入到当前类命名空间中。
         * 2. 优点：调用时可省略 ClassName. 前缀，常用于测试断言（如 assertEquals）或数学运算（Math.PI）。
         */
        System.out.println("\n--- 维度五：static import 静态导入语法实测 ---");
        double circleArea = PI * sqrt(16.0); // 直接使用 PI 和 sqrt，无需 Math.PI 和 Math.sqrt
        System.out.println("   静态导入 Math 成员计算: PI * sqrt(16.0) = " + circleArea);
    }

    /**
     * 初始化控制台字符编码，解决 Windows 环境终端输出中文乱码的问题。
     */
    private static void initConsoleEncoding() {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}
