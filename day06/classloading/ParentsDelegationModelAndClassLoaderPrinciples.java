package classloading;

/**
 * 双亲委派机制深度剖析、源码执行逻辑、四大核心作用与沙箱防篡改实战
 *
 * 涵盖面试核心题目：
 * 1. Java中双亲委派 是什么？有啥用?
 * 2. 双亲委派模型的作用
 */
public class ParentsDelegationModelAndClassLoaderPrinciples {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("        Java 双亲委派机制底层本质、源码级流转与核心作用深度实测        ");
        System.out.println("======================================================================");

        explainWhatIsParentsDelegation();
        explainClassLoaderSourceCodeFlow();
        explainParentsDelegationRolesAndBenefits();
        demonstrateSandboxSecurityDefense();

        System.out.println("======================================================================");
        System.out.println("                 双亲委派机制与作用解析完成                          ");
        System.out.println("======================================================================");
    }

    /**
     * 问题 1 第一问：Java中双亲委派 是什么？
     *
     * 概念、名称渊源与核心流转模式。
     */
    public static void explainWhatIsParentsDelegation() {
        System.out.println("\n--- 1. 什么是双亲委派机制（Parents Delegation Model）？ ---");
        System.out.println("一、双亲委派的核心定义：");
        System.out.println("   - 双亲委派模型是 Java 虚拟机在类加载机制中推荐采用的一种【任务委派架构】；");
        System.out.println("   - 当一个类加载器收到类加载请求时，它首先不会自己去尝试加载这个类，而是把这个加载任务【委派给父类加载器】去完成；");
        System.out.println("   - 每一个层次的类加载器都是如此，因此所有的加载请求最终都应该传送到最顶层的启动类加载器（Bootstrap ClassLoader）中；");
        System.out.println("   - 只有当父类加载器反馈自己无法完成这个加载请求（它的搜索范围中没有找到所需的类，抛出 ClassNotFoundException）时，");
        System.out.println("     子类加载器才会尝试自己去其负责的路径中搜索并加载该类。");

        System.out.println("\n二、关键认知澄清（面试避坑点）：");
        System.out.println("   1. 为什么叫“双亲”？");
        System.out.println("      - 英文原词为“Parents Delegation Model”。在英文中 Parent 既可指双亲也可指父亲；");
        System.out.println("      - 实际上在 Java 源码中，ClassLoader 内部只有一个 parent 属性（private final ClassLoader parent;），");
        System.out.println("        属于单亲委托关系，并非有两个父亲，这是早期技术引进时的直译习惯。");
        System.out.println("   2. 父子类加载器之间是【继承关系】吗？");
        System.out.println("      - 绝大多数不是！类加载器之间的父子关系通常不是以“类继承（extends）”代码形式实现的，");
        System.out.println("        而是采用【组合（Composition）】模式，在子加载器对象中持有父加载器对象的引用（parent 字段）。");
    }

    /**
     * 双亲委派在 JDK 官方源码中的具体实现剖析
     * (对应 java.lang.ClassLoader.loadClass 方法)
     */
    public static void explainClassLoaderSourceCodeFlow() {
        System.out.println("\n--- 2. 双亲委派在 ClassLoader.loadClass() 中的源码实现拆解 ---");
        System.out.println("JDK 中核心方法 loadClass(String name, boolean resolve) 的标准实现模板：");
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println("protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {");
        System.out.println("    synchronized (getClassLoadingLock(name)) { // 1. 针对类名加锁，保证并发安全");
        System.out.println("        // 2. 首先检查请求的类是否已经被加载过（自底向上检查缓存）");
        System.out.println("        Class<?> c = findLoadedClass(name);");
        System.out.println("        if (c == null) {");
        System.out.println("            try {");
        System.out.println("                if (parent != null) {");
        System.out.println("                    // 3. 父加载器不为空，向上委派给父加载器递归调用 loadClass");
        System.out.println("                    c = parent.loadClass(name, false);");
        System.out.println("                } else {");
        System.out.println("                    // 4. parent 为 null，代表父加载器是顶层 Bootstrap ClassLoader");
        System.out.println("                    c = findBootstrapClassOrNull(name);");
        System.out.println("                }");
        System.out.println("            } catch (ClassNotFoundException e) {");
        System.out.println("                // 父加载器抛出 ClassNotFoundException，说明父加载器在其搜索范围无法加载此类");
        System.out.println("            }");
        System.out.println("            if (c == null) {");
        System.out.println("                // 5. 父加载器无法加载时，自顶向下才调用自身的 findClass(name) 搜索本类加载路径");
        System.out.println("                c = findClass(name);");
        System.out.println("            }");
        System.out.println("        }");
        System.out.println("        if (resolve) {");
        System.out.println("            resolveClass(c); // 进行连接阶段的解析");
        System.out.println("        }");
        System.out.println("        return c;");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println("源码精髓口诀：【自底向上查缓存，自顶向下试加载】！");
    }

    /**
     * 问题 1 第二问 & 问题 2：双亲委派有啥用？双亲委派模型的作用
     *
     * 四大核心作用：
     * 1. 沙箱安全防篡改（保护核心 API，防止伪造 String/Object）
     * 2. 避免类的重复加载（保证内存节约与元空间性能）
     * 3. 保证 Java 核心类库类型的唯一性（全限定类名 + ClassLoader 决定类型唯一性）
     * 4. 规范清晰的系统加载边界与模块化隔离
     */
    public static void explainParentsDelegationRolesAndBenefits() {
        System.out.println("\n--- 3. 双亲委派模型的核心作用与不可替代的价值 ---");

        System.out.println("作用一：沙箱安全防篡改机制（保护 Java 核心 API 不被恶意侵入）");
        System.out.println("   - 保证了 Java 核心基础类库在各种运行环境中的绝对安全性；");
        System.out.println("   - 场景假设：黑客或别有用心者在项目中恶意编写了一个包名为 java.lang、类名为 String 的伪造类，试图劫持密码校验逻辑；");
        System.out.println("   - 在双亲委派机制下：");
        System.out.println("     * AppClassLoader 收到加载请求，直接向上委托给 PlatformClassLoader，再委托给 Bootstrap ClassLoader；");
        System.out.println("     * Bootstrap 启动类加载器在 JDK 原生 rt.jar / java.base 模块中直接找到了正版 java.lang.String 并将其返回；");
        System.out.println("     * 开发者自定义的同名恶意类根本没有被加载的机会，从源头上彻底阻断了对系统核心库的篡改！");

        System.out.println("\n作用二：避免类的重复加载（保障 JVM 元空间高效与整洁）");
        System.out.println("   - 如果没有双亲委派，每个类加载器都各自为政、盲目尝试自己加载：");
        System.out.println("     * 每一个自定义加载器在加载代码时，遇到 Object、String、Integer，都会在其内部重新在方法区生成一份独立的 Class 元数据对象；");
        System.out.println("     * 这将导致 JVM 元空间（Metaspace）被成千上万个重复的 Object.class 挤满，造成极其严重的内存浪费与垃圾回收压力；");
        System.out.println("   - 双亲委派机制确保了只要顶层父加载器已经加载过，子加载器就能复用父加载器的 Class 镜像，保证全系统只存在一份基础类元信息。");

        System.out.println("\n作用三：保证 Java 基础类在 JVM 全生命周期的【类型唯一性】（核心底层准则）");
        System.out.println("   - JVM 判定两个类是否属于同一个 Class 的黄金准则：");
        System.out.println("     【类的全限定类名相同】 并且 【必须由同一个 ClassLoader 实例所加载】！");
        System.out.println("   - 假定没有双亲委派机制，由不同的类加载器各自加载了一份 java.lang.Integer：");
        System.out.println("     * 那么哪怕这两个类的字节码完全一模一样，在 JVM 看来它们也是两个完全风马牛不相及的独立类型；");
        System.out.println("     * 将加载器 A 生成的 Integer 对象强转为加载器 B 的 Integer 类型时，JVM 会直接抛出 java.lang.ClassCastException！");
        System.out.println("     * instanceof 运算符比对也会直接返回 false，导致面向对象多态与类型系统彻底分崩离析。");

        System.out.println("\n作用四：清晰的类库分工与模块化隔离边界");
        System.out.println("   - 顶层 Bootstrap 负责 JVM 最核心的运行基石（java.base、rt.jar）；");
        System.out.println("   - 中间层 Platform/Ext 负责扩展库；");
        System.out.println("   - 底层 AppClassLoader 负责用户工程的 ClassPath；");
        System.out.println("   - 自定义加载器负责动态解密与插件热替换。层次分明，职责内聚。");
    }

    /**
     * 现场实测：演示伪造 java.lang 包的沙箱防护与安全校验
     */
    public static void demonstrateSandboxSecurityDefense() {
        System.out.println("\n--- 4. [代码现场实测] 试图通过自定义类加载器破坏沙箱安全 ---");

        FakeClassSecurityViolator violatorLoader = new FakeClassSecurityViolator();

        try {
            // 试图让自定义类加载器直接 defineClass 一个以 java.lang 开头的自定义类
            System.out.println("1. 尝试通过 ClassLoader.defineClass 定义属于 java.lang.CustomHack 的非法类...");
            violatorLoader.attemptDefineProhibitedClass();
        } catch (SecurityException se) {
            System.out.println("   [JVM 安全机制拦截成功！]");
            System.out.println("   捕获到预期异常: " + se.getClass().getName() + ": " + se.getMessage());
            System.out.println("   证实结论: JVM 源码层面在 preDefineClass 中做了强校验，任何自定义加载器试图声明以 \"java.\" 开头的类，");
            System.out.println("            都会被立即抛出 SecurityException: Prohibited package name: java.lang 强力阻断！");
        }
    }

    /**
     * 辅助测试类加载器：模拟违规注入 java.lang 包
     */
    static class FakeClassSecurityViolator extends ClassLoader {
        public void attemptDefineProhibitedClass() {
            // 空的假字节流，只要类名以 java.lang 开头就会在 defineClass 阶段被 JVM 底层 preDefineClass 拦截
            byte[] fakeByteCode = new byte[] { (byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE };
            defineClass("java.lang.CustomHack", fakeByteCode, 0, fakeByteCode.length);
        }
    }
}
