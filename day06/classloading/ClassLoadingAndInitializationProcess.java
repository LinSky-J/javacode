package classloading;

/**
 * 类生命周期七大阶段、类初始化时机（主动引用 vs 被动引用）与 <clinit> / <init> 触发顺序实测
 *
 * 涵盖面试核心题目：
 * 类的初始化和加载
 * 深入扩展：类的生命周期（加载、连接、初始化）、被动引用不触发初始化的三大场景、静态代码块与构造器执行先后顺序。
 */
public class ClassLoadingAndInitializationProcess {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          类生命周期七大阶段、主动引用 vs 被动引用与初始化实测          ");
        System.out.println("======================================================================");

        explainClassLifecycleSevenStages();
        explainActiveVsPassiveReference();
        demonstrateInitializationExecutionOrder();

        System.out.println("======================================================================");
        System.out.println("                 类的初始化与加载流程解析完成                        ");
        System.out.println("======================================================================");
    }

    /**
     * 类的生命周期七大阶段全景剖析（加载 -> 连接[验证、准备、解析] -> 初始化 -> 使用 -> 卸载）
     */
    public static void explainClassLifecycleSevenStages() {
        System.out.println("\n--- 1. 类生命周期七大核心阶段（从 .class 到卸载） ---");

        System.out.println("阶段一：加载（Loading）");
        System.out.println("   - 1. 通过类的全限定名获取定义此类的二进制字节流（可来自 class 文件、jar 包、网络、动态代理等）；");
        System.out.println("   - 2. 将字节流所代表的静态存储结构转化为方法区（元空间）的运行时数据结构；");
        System.out.println("   - 3. 在 Java 堆内存中生成一个代表此类的 java.lang.Class 对象，作为访问方法区元数据的外部接口。");

        System.out.println("\n阶段二：验证（Verification - 连接的第一步）");
        System.out.println("   - 确保 Class 文件的字节流中包含的信息符合《Java虚拟机规范》的全部约束要求，杜绝恶意代码；");
        System.out.println("   - 包含四项验证：文件格式验证（魔数 0xCAFEBABE、版本号）、元数据验证、字节码验证、符号引用验证。");

        System.out.println("\n阶段三：准备（Preparation - 连接的第二步）");
        System.out.println("   - 正式在方法区（元空间）中为【类变量（static 修饰的变量）】分配内存并设置【初始零值】！");
        System.out.println("   - [极其关键考点] 此时分配的是零值（0、null、false），而不是程序员赋的值：");
        System.out.println("     例如：public static int value = 123; 在准备阶段之后 value 的值是 0，而不是 123！真正赋值为 123 是在初始化阶段。");
        System.out.println("   - [常量特例] 如果字段是 static final 常量：");
        System.out.println("     例如：public static final int CONST_VAL = 123; 编译时 javac 会生成 ConstantValue 属性，在准备阶段直接被赋值为 123！");

        System.out.println("\n阶段四：解析（Resolution - 连接的第三步）");
        System.out.println("   - JVM 将运行时常量池内的【符号引用（Symbolic References）】替换为【直接引用（Direct References）】的过程；");
        System.out.println("   - 符号引用是用字面量描述引用的目标；直接引用是可以直接指向目标的指针、相对偏移量或间接句柄。");

        System.out.println("\n阶段五：初始化（Initialization - 核心执行阶段）");
        System.out.println("   - 初始化阶段是真正开始执行类中编写的 Java 程序代码；");
        System.out.println("   - JVM 会执行【类构造器 <clinit>() 方法】：");
        System.out.println("     * <clinit>() 是由 javac 编译器自动收集类中所有静态变量的赋值动作和静态代码块（static {}）中的语句合并产生的；");
        System.out.println("     * JVM 会保证在子类的 <clinit>() 执行之前，父类的 <clinit>() 已经执行完毕；");
        System.out.println("     * JVM 会保证一个类的 <clinit>() 方法在多线程环境下被正确地加锁和同步，确保只被初始化一次。");

        System.out.println("\n阶段六：使用（Using）");
        System.out.println("   - 业务代码通过 new 实例化对象、调用静态方法或通过反射使用该类。");

        System.out.println("\n阶段七：卸载（Unloading）");
        System.out.println("   - 必须同时满足三个苛刻条件，类才会被 JVM 卸载（方法区回收）：");
        System.out.println("     1) 该类的所有实例都已经被垃圾回收器回收；");
        System.out.println("     2) 加载该类的 ClassLoader 已经被回收；");
        System.out.println("     3) 该类对应的 java.lang.Class 对象没有任何地方被引用，无法在任何地方通过反射访问该类。");
    }

    /**
     * 类的初始化时机：主动引用（触发初始化） vs 被动引用（不触发初始化）
     */
    public static void explainActiveVsPassiveReference() {
        System.out.println("\n--- 2. 类初始化的触发时机：主动引用 vs 被动引用 ---");
        System.out.println("一、必须立即对类进行初始化的 6 种【主动引用】场景：");
        System.out.println("   1. 遇到 new、getstatic、putstatic 或 invokestatic 4 条字节码指令时；");
        System.out.println("   2. 使用 java.lang.reflect 包的方法对类进行反射调用时；");
        System.out.println("   3. 当初始化类时，若其父类尚未初始化，必须先触发父类的初始化；");
        System.out.println("   4. 虚拟机启动时，指定要执行的主类（包含 main() 方法的启动类）；");
        System.out.println("   5. JDK 7+ 动态语言支持的方法句柄 MethodHandle 解析结果属于 REF_getStatic、REF_putStatic、REF_invokeStatic 等；");
        System.out.println("   6. JDK 8+ 当一个接口定义了 default 方法，若其实现类初始化，该接口必须先初始化。");

        System.out.println("\n二、绝不会触发类初始化的 3 种典型【被动引用】场景（大厂常考笔试陷阱）：");
        System.out.println("   场景 1：通过子类引用父类的静态字段，只会触发父类初始化，子类不会被初始化！");
        System.out.println("   场景 2：通过数组定义来引用类（如 SuperClass[] array = new SuperClass[10];），不会触发 SuperClass 的初始化！");
        System.out.println("   场景 3：引用类的编译期常量（static final），常量在编译阶段存入调用类的常量池中，本质上没有直接引用定义常量的类，不会触发其初始化！");
    }

    /**
     * 实测静态代码块、构造代码块、父子类初始化先后执行顺序
     */
    public static void demonstrateInitializationExecutionOrder() {
        System.out.println("\n--- 3. [现场实测] 类初始化 <clinit> 与对象初始化 <init> 执行顺序 ---");
        System.out.println("经典口诀：父类静态 -> 子类静态 -> 父类普通代码块/构造 -> 子类普通代码块/构造");
        System.out.println("执行 new ChildClass() 开始：\n");

        ChildClass child = new ChildClass();

        System.out.println("\n实测完毕！证实执行规律：");
        System.out.println("1. <clinit>（类初始化）只在类初次加载时执行一次：");
        System.out.println("   ① 父类 static 属性与 static 代码块；");
        System.out.println("   ② 子类 static 属性与 static 代码块；");
        System.out.println("2. <init>（实例构造）每次 new 实例都会按顺序执行：");
        System.out.println("   ③ 父类实例变量赋值与普通代码块；");
        System.out.println("   ④ 父类构造函数；");
        System.out.println("   ⑤ 子类实例变量赋值与普通代码块；");
        System.out.println("   ⑥ 子类构造函数。");
    }

    /**
     * 辅助父类
     */
    static class ParentClass {
        static {
            System.out.println("   [1] ParentClass <clinit> 静态代码块执行 (父类静态初始化)");
        }

        {
            System.out.println("   [3] ParentClass <init> 实例代码块执行 (父类普通代码块)");
        }

        public ParentClass() {
            System.out.println("   [4] ParentClass <init> 构造方法执行 (父类构造函数)");
        }
    }

    /**
     * 辅助子类
     */
    static class ChildClass extends ParentClass {
        static {
            System.out.println("   [2] ChildClass <clinit> 静态代码块执行 (子类静态初始化)");
        }

        {
            System.out.println("   [5] ChildClass <init> 实例代码块执行 (子类普通代码块)");
        }

        public ChildClass() {
            System.out.println("   [6] ChildClass <init> 构造方法执行 (子类构造函数)");
        }
    }
}
