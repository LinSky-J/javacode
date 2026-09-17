package classloading;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 类加载五大核心子阶段深度剖析、类加载过程与双亲委派原则深度联动实战
 *
 * 涵盖面试核心题目：
 * 3. 讲一下类加载过程?
 * 4. 讲一下类的加载和双亲委派原则
 */
public class ClassLoadingDeepDiveAndDelegationIntegration {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          类加载五大完整子阶段剖析与双亲委派原则联动机制实测          ");
        System.out.println("======================================================================");

        explainDetailedClassLoadingProcess();
        explainRelationBetweenClassLoadingAndDelegation();
        demonstrateClassLoaderNamespaceIsolation();

        System.out.println("======================================================================");
        System.out.println("                 类加载过程与双亲委派联动解析完成                    ");
        System.out.println("======================================================================");
    }

    /**
     * 问题 3：讲一下类加载过程?
     *
     * 五大子阶段：
     * 1. 加载（Loading）
     * 2. 验证（Verification）
     * 3. 准备（Preparation）
     * 4. 解析（Resolution）
     * 5. 初始化（Initialization）
     * （后三个子阶段合称为“连接 Linking”）
     */
    public static void explainDetailedClassLoadingProcess() {
        System.out.println("\n--- 1. 类加载完整五大核心阶段深度解剖 ---");

        System.out.println("阶段一：加载（Loading - 类加载过程的第一步）");
        System.out.println("   - 三大核心任务：");
        System.out.println("     1) 通过一个类的全限定名来获取定义此类的二进制字节流（来源极为广泛：本地 .class、jar/war 包、网络流、动态代理运行期生成等）；");
        System.out.println("     2) 将这个字节流所代表的静态存储结构转化为方法区（元空间）的运行时数据结构（如 HotSpot 的 InstanceKlass 对象）；");
        System.out.println("     3) 在 Java 堆内存中生成一个代表该类的 java.lang.Class 对象，作为程序访问方法区中这些类型数据的外部入口。");

        System.out.println("\n阶段二：验证（Verification - 连接阶段之首）");
        System.out.println("   - 目的：确保 Class 文件的字节流中包含的信息完全符合 JVM 规范，保证这些代码运行后不会危害虚拟机的安全。");
        System.out.println("   - 四大检验严密把关：");
        System.out.println("     1) 文件格式验证：魔数必须是 0xCAFEBABE、主次版本号在当前 JVM 支持范围内、常量池 tag 检查等；");
        System.out.println("     2) 元数据验证：对字节码描述的信息进行语义分析（是否有父类、是否继承了 final 类、是否实现了抽象类所有方法）；");
        System.out.println("     3) 字节码验证：最复杂的一步，通过数据流和控制流分析，确定程序语义合乎逻辑，如方法操作数栈类型匹配、跳转指令不越界；");
        System.out.println("     4) 符号引用验证：对类自身以外的信息（常量池中各种符号引用）进行匹配性校验，确保引用的类、方法、字段在外部切实存在且有访问权限。");

        System.out.println("\n阶段三：准备（Preparation - 连接阶段第二步）");
        System.out.println("   - 核心任务：正式在方法区（元空间）中为【类变量（即 static 变量）】分配内存并设置【初始零值】。");
        System.out.println("   - 极高频面试避坑细节：");
        System.out.println("     * 细节 1：准备阶段只为【static 修饰的类变量】分配内存，不包括实例变量！实例变量是在对象实例化时随对象一起在堆内存分配的；");
        System.out.println("     * 细节 2：这里设置的内存初始值通常是数据类型的【零值】（如 int 为 0，boolean 为 false，引用为 null），而不是代码中的赋值：");
        System.out.println("       例如代码：public static int number = 666; 在准备阶段之后 number 的值是 0，真正赋值为 666 要等到初始化阶段！");
        System.out.println("     * 细节 3（常量特例）：若字段同时被 static final 修饰（常量）：");
        System.out.println("       例如代码：public static final int CONST_NUM = 666; 编译器在编译时会为该字段生成 ConstantValue 属性，在准备阶段直接被赋值为 666！");

        System.out.println("\n阶段四：解析（Resolution - 连接阶段第三步）");
        System.out.println("   - 核心任务：JVM 将常量池内的【符号引用（Symbolic References）】替换为【直接引用（Direct References）】。");
        System.out.println("   - 概念对照：");
        System.out.println("     * 符号引用：以一组符号（字面量）来描述所引用的目标，只要能无歧义定位即可，与内存布局无关；");
        System.out.println("     * 直接引用：可以直接指向目标的指针、相对偏移量或能间接定位到目标的句柄，目标必定已存在于 JVM 内存中；");
        System.out.println("   - 静态解析 vs 动态链接：");
        System.out.println("     * 静态解析（非虚方法）：静态方法、私有方法、构造器方法、父类方法在类加载解析阶段即可完全确定唯一直接引用；");
        System.out.println("     * 动态链接（虚方法）：多态重写的方法必须等到程序运行期根据调用对象的实际类型动态分派（vtable 虚方法表偏移量查找）。");

        System.out.println("\n阶段五：初始化（Initialization - 类加载的收尾与高潮）");
        System.out.println("   - 核心任务：真正开始执行类中编写的 Java 代码，执行类构造器 <clinit>() 方法。");
        System.out.println("   - <clinit>() 方法的核心特性：");
        System.out.println("     1) 编译器自动收集：javac 编译器按源码出现的顺序自动收集所有类变量（static 变量）的赋值动作和静态代码块（static {}）；");
        System.out.println("     2) 父类优先：JVM 保证在子类 <clinit>() 执行前，父类的 <clinit>() 已经执行完毕；");
        System.out.println("     3) 线程安全互斥锁：JVM 会保证一个类的 <clinit>() 方法在多线程高并发环境下被正确加锁同步，确保类只被初始化一次。");
    }

    /**
     * 问题 4：讲一下类的加载和双亲委派原则
     *
     * 类的加载与双亲委派原则的紧密结合关系。
     */
    public static void explainRelationBetweenClassLoadingAndDelegation() {
        System.out.println("\n--- 2. 类的加载全流程与双亲委派原则的深度联动关系 ---");
        System.out.println("一、定位与分工关系：");
        System.out.println("   1. 类加载机制（Class Loading）：");
        System.out.println("      - 是 JVM 虚拟机将 .class 字节流转化为内存中 Class 结构体并完成装配执行的【生命周期全流程】（加载、连接、初始化）。");
        System.out.println("   2. 双亲委派原则（Parents Delegation）：");
        System.out.println("      - 是 ClassLoader 类加载器在执行类加载的第一步——【加载（Loading）阶段获取字节流】时所遵循的【最高路由协议与查找策略】！");
        System.out.println("   3. 联动时序：");
        System.out.println("      - 当 JVM 需要使用一个类时，首先向当前 ClassLoader 发起加载请求；");
        System.out.println("      - ClassLoader 严格遵循双亲委派原则：自底向上逐层委派给父加载器检查缓存并尝试从核心类路径读取字节流；");
        System.out.println("      - 找到字节流后，调用 native 方法 defineClass() 将字节数组转化为 Class 镜像，随后紧接着触发验证、准备、解析与初始化！");

        System.out.println("\n二、类加载器所构建的【运行时命名空间（Runtime Namespace）】概念：");
        System.out.println("   - 在 Java 虚拟机中，类加载器不仅负责“搬运字节码”，还充当了【类型边界与命名空间隔离墙】！");
        System.out.println("   - 核心定理：两个类是由同一个 Class 文件编译而来，只要加载它们的 ClassLoader 实例不同，");
        System.out.println("     在 JVM 看来它们就是两个完全独立、互不兼容的类型（instanceof 返回 false，强转抛 ClassCastException）！");
        System.out.println("   - 正是双亲委派原则，确保了所有基础类都只由同一个父加载器加载，从而消除了不同命名空间下的类型冲突。");
    }

    /**
     * 现场实测：同一个 Class 字节码由不同类加载器加载，产生命名空间隔离与类型不匹配
     */
    public static void demonstrateClassLoaderNamespaceIsolation() {
        System.out.println("\n--- 3. [现场实测] 破坏双亲委派导致命名空间隔离与类型不兼容验证 ---");

        try {
            // 自定义一个重写了 loadClass 的类加载器（打破双亲委派，直接自己从当前 ClassPath 读取字节码）
            CustomDirectClassLoader isolatedLoader = new CustomDirectClassLoader();

            String targetClassName = "classloading.ClassLoadingDeepDiveAndDelegationIntegration$SampleEntity";

            // 1. 通过系统默认的 AppClassLoader 加载 SampleEntity
            Class<?> classFromAppLoader = SampleEntity.class;

            // 2. 通过打破双亲委派的自定义加载器加载同一个类
            Class<?> classFromIsolatedLoader = isolatedLoader.loadClass(targetClassName);

            System.out.println("1. 比较类名全限定名:");
            System.out.println("   classFromAppLoader 类名:      " + classFromAppLoader.getName());
            System.out.println("   classFromIsolatedLoader 类名: " + classFromIsolatedLoader.getName());
            System.out.println("   类名是否完全一致:             " + classFromAppLoader.getName().equals(classFromIsolatedLoader.getName()));

            System.out.println("\n2. 比较类加载器实例:");
            System.out.println("   classFromAppLoader 的加载器:      " + classFromAppLoader.getClassLoader());
            System.out.println("   classFromIsolatedLoader 的加载器: " + classFromIsolatedLoader.getClassLoader());

            System.out.println("\n3. 核心比对：JVM 内部两个 Class 实例是否相同（== 比对）:");
            System.out.println("   classFromAppLoader == classFromIsolatedLoader ? " + (classFromAppLoader == classFromIsolatedLoader));

            System.out.println("\n4. 核心比对：实例化自定义加载器的对象，并用当前类的类型进行 isInstance 检查:");
            Object isolatedObject = classFromIsolatedLoader.getDeclaredConstructor().newInstance();
            boolean isInstanceOfAppType = classFromAppLoader.isInstance(isolatedObject);
            System.out.println("   classFromAppLoader.isInstance(isolatedObject) ? " + isInstanceOfAppType);
            System.out.println("   证实结论: 即使代码和字节码完全一模一样，不同 ClassLoader 加载产生的类在 JVM 判定中互不兼容！");
            System.out.println("            这深刻说明了双亲委派原则保证类型全局唯一性的关键价值！");

        } catch (Exception e) {
            System.out.println("   实测抛出异常: " + e);
        }
    }

    /**
     * 辅助演示实体类
     */
    public static class SampleEntity {
        public SampleEntity() {}
    }

    /**
     * 自定义类加载器：通过重写 loadClass 打破双亲委派，强制直接读取字节码
     */
    static class CustomDirectClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            // 只对指定测试类打破双亲委派，其他系统类（如 java.lang.Object）仍走默认委派
            if (name.startsWith("classloading.ClassLoadingDeepDiveAndDelegationIntegration$SampleEntity")) {
                try {
                    String classFileName = name.replace('.', '/') + ".class";
                    InputStream is = getClass().getClassLoader().getResourceAsStream(classFileName);
                    if (is == null) {
                        return super.loadClass(name);
                    }
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                    }
                    byte[] bytes = bos.toByteArray();
                    return defineClass(name, bytes, 0, bytes.length);
                } catch (Exception e) {
                    throw new ClassNotFoundException(name, e);
                }
            }
            return super.loadClass(name);
        }
    }
}
