package objects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 面试专题：Java 对象创建全方式、对象生命周期与垃圾回收判定、私有对象与属性破解实战。
 *
 * 本类对应面试核心题目：
 * 1. java创建对象有哪些方式?
 * 2. Java创建对象除了new还有别的什么方式?
 * 3. New出的对象什么时候回收?
 * 4. 如何获取私有对象?
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaObjectCreationAndLifecycle {

    public static void main(String[] args) {

        System.out.println("======================================================================");
        System.out.println("          Java 对象创建方式全景、垃圾回收生命周期与私有获取实战       ");
        System.out.println("======================================================================");

        explainObjectCreationWays();
        explainWhenNewObjectIsRecycled();
        explainHowToAccessPrivateObject();

        System.out.println("\n======================================================================");
        System.out.println("            对象体系与生命周期解析完毕，请细读类中源码与注释          ");
        System.out.println("======================================================================");
    }

    /**
     * 第一部分与第二部分合流精讲：
     * java创建对象有哪些方式?
     * Java创建对象除了new还有别的什么方式?
     *
     * 面试核心考点：5种创建对象方式及其是否调用构造函数的底层差异。
     */
    public static void explainObjectCreationWays() {
        System.out.println("1. java创建对象有哪些方式? / Java创建对象除了new还有别的什么方式?");

        /*
         * 汇总对比：Java 创建对象的五大途径及构造函数调用情况
         * -----------------------------------------------------------------------------------------
         * 创建途径             具体语法实现                                  底层是否执行构造函数
         * -----------------------------------------------------------------------------------------
         * 1. new 关键字        new TargetUser("张三", 20)                    是（invokespecial <init>）
         * 2. 反射机制          Constructor.newInstance(args)                 是（反射调用构造器）
         * 3. clone() 方法      (TargetUser) sourceObj.clone()                否（直接内存二进制复制）
         * 4. 反序列化          ObjectInputStream.readObject()                否（由 JVM 直接分配组装数据）
         * 5. Unsafe 机制       sun.misc.Unsafe.allocateInstance(Class)       否（完全绕过构造器分配物理内存）
         * -----------------------------------------------------------------------------------------
         */

        // 方式一：new 关键字（最基础常规方式，调用构造器）
        System.out.println("\n--- [方式一] new 关键字创建对象（触发构造函数） ---");
        TargetUser user1 = new TargetUser("张三", 22);
        System.out.println("   创建成功: " + user1);

        // 方式二：反射机制（通过 Constructor.newInstance 反射调用构造器）
        System.out.println("\n--- [方式二] 反射机制创建对象（除了 new 之外的方式，触发构造函数） ---");
        try {
            Class<?> userClass = Class.forName("objects.TargetUser");
            Constructor<?> constructor = userClass.getConstructor(String.class, int.class);
            TargetUser user2 = (TargetUser) constructor.newInstance("李四(反射版)", 25);
            System.out.println("   反射创建成功: " + user2);
        } catch (Exception e) {
            System.out.println("   反射创建失败: " + e.getMessage());
        }

        // 方式三：clone() 方法（除了 new 之外的方式，完全不触发构造函数！）
        System.out.println("\n--- [方式三] clone() 克隆创建对象（除了 new 之外的方式，不执行构造函数！） ---");
        TargetUser user3 = user1.clone();
        System.out.println("   clone 创建成功: " + user3);
        System.out.println("   (内存地址对比: user1 == user3 为 " + (user1 == user3) + "，说明堆中确实产生了全新对象！)");

        // 方式四：反序列化（除了 new 之外的方式，从字节流重建，完全不触发构造函数！）
        System.out.println("\n--- [方式四] 反序列化创建对象（除了 new 之外的方式，不执行构造函数！） ---");
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(byteOut)) {
                oos.writeObject(user1);
            }
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            try (ObjectInputStream ois = new ObjectInputStream(byteIn)) {
                TargetUser user4 = (TargetUser) ois.readObject();
                System.out.println("   反序列化创建成功: " + user4);
                System.out.println("   (内存地址对比: user1 == user4 为 " + (user1 == user4) + "，彻底全新的堆对象！)");
            }
        } catch (Exception e) {
            System.out.println("   反序列化创建失败: " + e.getMessage());
        }

        // 方式五：sun.misc.Unsafe 底层分配（底层黑魔法，Spring/Objenesis/CGLIB 动态代理底层常用）
        System.out.println("\n--- [方式五] Unsafe 底层内存直接分配（不调用构造器，Spring/CGLIB 代理底层使用） ---");
        System.out.println("   原理：Unsafe.allocateInstance(Class) 绕过所有安全检查与构造函数，直接在堆上开辟内存并赋予默认零值。");
    }

    /**
     * 第三部分：
     * New出的对象什么时候回收?
     *
     * 面试核心考点：可达性分析算法、GC Roots、两次标记过程、分代垃圾收集时机。
     */
    public static void explainWhenNewObjectIsRecycled() {
        System.out.println("\n\n3. New出的对象什么时候回收?");

        /*
         * 1. 核心判断标准：可达性分析算法（Reachability Analysis）
         *    - JVM 不是用引用计数法（避免循环引用无法回收的问题），而是采用【可达性分析算法】！
         *    - 以一组被称为【GC Roots】的对象作为起始根集合，从这些节点开始向下搜索其所走过的引用链（Reference Chain）。
         *    - 当一个对象到任何 GC Roots 之间【没有任何引用链相连】（即从 GC Roots 不可达）时，证明该对象不可能再被任何活着的逻辑使用！
         *
         * 2. 哪些对象可以作为 GC Roots？（高频必考）
         *    - JVM 虚拟机栈（栈帧中的局部变量表）中引用的对象；
         *    - 方法区中类静态属性引用的对象；
         *    - 方法区中常量引用的对象（如字符串常量池、static final 常量）；
         *    - 本地方法栈中 JNI（Native 方法）引用的对象；
         *    - Java 虚拟机内部的引用（如系统类加载器、基本数据类型的 Class 对象）；
         *    - 所有被同步锁（synchronized）持有的对象；
         *    - 当前存活的运行中线程（Thread 对象）。
         *
         * 3. 对象的真正死亡与两次标记（finalize 自救机会）：
         *    - 第一次标记：可达性分析发现不可达，被第一次标记并进行筛选。
         *    - 筛选条件：该对象是否有必要执行 finalize() 方法？（若没有重写 finalize 或 finalize 已被执行过一次，则无需执行，直接回收）。
         *    - 第二次标记：若有必要执行，放入 F-Queue 队列，由低优先级的 Finalizer 线程去触发执行；
         *      若对象在 finalize() 中重新与 GC Roots 引用链挂钩（如 this 赋值给某个全局变量），则自救成功；否则进行第二次标记被物理回收！
         *      （注意：Java 9 已将 finalize 废弃，不可依赖其释放资源）。
         *
         * 4. 物理回收触发的时机（分代垃圾回收触发条件）：
         *    - Young GC (Minor GC)：当年轻代中的 Eden 伊甸园区空间不足时触发，绝大多数存活时间极短的对象在此被迅速回收。
         *    - Old GC (Major GC / Full GC)：
         *      - 当对象经历多次 Minor GC（默认 15 次，可通过 -XX:MaxTenuringThreshold 调整）晋升到老年代；
         *      - 当老年代剩余空间不足以容纳晋升的大对象时，触发老年代垃圾回收或者全局 Full GC；
         *      - 元空间（Metaspace）空间不足时；
         *      - 代码中显式调用 System.gc()（向 JVM 建议执行 Full GC，但不保证立即执行）。
         */
        System.out.println("   核心准则：当 new 出的对象与【GC Roots】之间失去所有引用链连接（不可达）时，该对象被判定为垃圾对象。");
        System.out.println("   回收时机：当 JVM 分代内存（Eden 区、老年代、元空间）不足触发 Minor GC 或 Full GC 时，垃圾收集器回收其占用的物理堆内存。");
    }

    /**
     * 第四部分：
     * 如何获取私有对象?
     *
     * 面试核心考点：反射暴力破解 setAccessible(true)、内部私有字段提取、私有内部类实例化。
     */
    public static void explainHowToAccessPrivateObject() {
        System.out.println("\n\n4. 如何获取私有对象?");

        /*
         * 场景一：获取类内部声明为 private 的私有成员对象/私有属性值
         * 破解核心：
         * 1. 通过 Class.getDeclaredField("fieldName") 获取私有字段声明；
         * 2. 调用 field.setAccessible(true) 暴力压制 JVM 访问权限检查；
         * 3. 调用 field.get(targetInstance) 强行读取私有对象。
         */
        System.out.println("--- [场景一] 通过反射暴力读取实例内部的 private 私有字段对象 ---");
        TargetUser user = new TargetUser("王五", 28);
        try {
            Field privateSecretField = TargetUser.class.getDeclaredField("secretSecurityCode");
            // 关键动作：压制访问权限检查
            privateSecretField.setAccessible(true);
            String privateValue = (String) privateSecretField.get(user);
            System.out.println("   [成功抓取私有属性] 突破 private 限制读取到的内部安全私钥: " + privateValue);

            // 甚至可以直接修改私有属性！
            privateSecretField.set(user, "HACKED-SECRET-888888");
            System.out.println("   [成功篡改私有属性] 强行修改私有字段后对象的最新状态: " + user);
        } catch (Exception e) {
            System.out.println("   获取私有字段失败: " + e.getMessage());
        }

        /*
         * 场景二：单例模式或工具类中构造函数私有化（private constructor），如何强行实例化该私有对象？
         * 破解核心：
         * 1. 通过 Class.getDeclaredConstructor(...) 获取私有构造方法；
         * 2. 调用 constructor.setAccessible(true) 强行打破封装；
         * 3. 调用 constructor.newInstance(...) 创建全新实例对象！
         */
        System.out.println("\n--- [场景二] 强行调用 private 私有构造函数创建私有特权对象 ---");
        try {
            Constructor<TargetUser> privateConstructor = TargetUser.class.getDeclaredConstructor(String.class);
            // 关键动作：破坏私有构造函数封装性
            privateConstructor.setAccessible(true);
            TargetUser privateConstructedUser = privateConstructor.newInstance("ROOT-SUPER-ADMIN-KEY-999");
            System.out.println("   [成功创建私有构造对象] " + privateConstructedUser);
        } catch (Exception e) {
            System.out.println("   调用私有构造函数失败: " + e.getMessage());
        }

        /*
         * 场景三：强行执行 private 私有方法
         */
        System.out.println("\n--- [场景三] 强行执行 private 私有方法 ---");
        try {
            Method privateMethod = TargetUser.class.getDeclaredMethod("privateInternalMethod");
            privateMethod.setAccessible(true);
            privateMethod.invoke(user);
        } catch (Exception e) {
            System.out.println("   调用私有方法失败: " + e.getMessage());
        }

        System.out.println("\n   总结：获取私有对象的核心武器是 Java 反射中的 getDeclaredXxx() + setAccessible(true) 机制！");
    }
}
