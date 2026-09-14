package objects.methods;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 面试专题：Java 根类 java.lang.Object 核心机制、方法体系与等于/哈希契约深度解析。
 *
 * 本类对应面试核心题目：
 * 1. Object类有哪些方法?
 * 2. == 与 equals 有什么区别?
 * 3. hashcode和equals方法有什么关系?
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class ObjectMethodsAndComparisonExplanation {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("       Java Object 类体系、== 与 equals 对比及 hashCode 契约深度解析   ");
        System.out.println("======================================================================");

        explainObjectMethods();
        explainOperatorVsEquals();
        explainHashCodeAndEqualsContract();

        System.out.println("\n======================================================================");
        System.out.println("           Object 体系与契约解析完毕，请细读类中源码与详细注释        ");
        System.out.println("======================================================================");
    }

    /**
     * 问题一：Object类有哪些方法?
     *
     * 面试核心考点：
     * 1. 知道 Object 是 Java 中所有类的直接或间接超类（类层级根节点）。
     * 2. 能够清晰列出 11~12 个方法，并明确其分类（生命周期/克隆/哈希对比/线程通信）。
     * 3. 能够解释各个方法的关键字修饰（native、final、protected 等）及版本演变（如 finalize 的弃用）。
     */
    public static void explainObjectMethods() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题一：Object类有哪些方法?");
        System.out.println("--------------------------------------------------");

        /*
         * 理论系统梳理：
         * Object 类共有 12 个方法（包含私有静态 registerNatives），按业务功能划分为 4 大类：
         *
         * 1. 对象基础操作与反射元数据：
         *    - public final native Class<?> getClass()
         *      作用：返回当前对象在 JVM 中的运行时 Class 类元数据，用于反射和类型透视。
         *    - public String toString()
         *      作用：返回对象的字符串表示。默认格式为 getClass().getName() + "@" + Integer.toHexString(hashCode())。
         *    - protected native Object clone() throws CloneNotSupportedException
         *      作用：创建并返回当前对象的内存副本（浅拷贝）。必须实现 Cloneable 标记接口，否则抛出 CloneNotSupportedException。
         *
         * 2. 对象等价性判定与哈希散列：
         *    - public boolean equals(Object obj)
         *      作用：指示另一个对象是否与此对象相等。Object 默认实现是 (this == obj)，即只比较内存引用地址。
         *    - public native int hashCode()
         *      作用：返回对象的哈希码（整型值）。默认由 JVM 本地生成（与对象物理内存地址相关），主要服务于哈希表散列定位。
         *
         * 3. 线程监视器（Monitor）与进程间通信：
         *    - public final native void wait(long timeoutMillis) throws InterruptedException
         *    - public final void wait(long timeoutMillis, int nanos) throws InterruptedException
         *    - public final void wait() throws InterruptedException
         *      作用：使当前获得该对象 Monitor 监视器锁的线程进入 WAITING / TIMED_WAITING 状态并释放锁，直到被唤醒或超时。
         *    - public final native void notify()
         *      作用：随机唤醒在此对象 Monitor 上等待的单个线程（进入 BLOCKED 重新竞争锁）。
         *    - public final native void notifyAll()
         *      作用：唤醒在此对象 Monitor 上等待的所有线程。
         *    注意：wait/notify/notifyAll 必须在 synchronized 代码块中调用，否则抛出 IllegalMonitorStateException。
         *
         * 4. 垃圾回收析构与系统注册：
         *    - protected void finalize() throws Throwable
         *      作用：垃圾回收器在确定对象没有任何引用时调用的析构勾子方法。
         *      演进：Java 9 标记为 @Deprecated，Java 18 标记为 forRemoval。因其执行时机不可控、会引发对象复活及严重内存泄露，严禁在生产中使用。
         *    - private static native void registerNatives()
         *      作用：静态初始化块中执行，向 JVM 注册本地 C/C++ 实现的方法符号。
         */

        System.out.println("1. [反射实测] 遍历 java.lang.Object 声明的所有方法信息列表：");
        Method[] methods = Object.class.getDeclaredMethods();
        int index = 1;
        for (Method method : methods) {
            String modifiers = Modifier.toString(method.getModifiers());
            String returnType = method.getReturnType().getSimpleName();
            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < paramTypes.length; i++) {
                params.append(paramTypes[i].getSimpleName());
                if (i < paramTypes.length - 1) {
                    params.append(", ");
                }
            }
            System.out.printf("   [%02d] %-35s %s %s(%s)\n",
                    index++, modifiers, returnType, name, params);
        }
    }

    /**
     * 问题二：== 与 equals 有什么区别?
     *
     * 面试核心考点：
     * 1. 概念本质：== 是运算符，equals 是类方法。
     * 2. 基本数据类型 vs 引用数据类型的比较维度。
     * 3. Object 默认 equals 的实现与自定义重写的差异。
     * 4. 包装类与常量池（Integer 缓存池、字符串常量池）带来的易错陷阱。
     */
    public static void explainOperatorVsEquals() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题二：== 与 equals 有什么区别?");
        System.out.println("--------------------------------------------------");

        /*
         * 核心维度对比：
         * 1. 语法定位：
         *    - '==' 是 Java 语言的核心操作符（Operator）。
         *    - 'equals' 是 java.lang.Object 中定义的方法（Method）。
         *
         * 2. 比较内容：
         *    - 对于【基本数据类型】（byte, short, int, long, float, double, char, boolean）：
         *      只能使用 '==' 进行比较，比较的是变量所保存的【数值字面量（Value）】是否相等。
         *      基本类型没有方法，不能调用 equals。
         *    - 对于【引用数据类型】（对象）：
         *      - '==' 比较的是两个引用在内存中的【物理堆地址（Reference）】是否指向同一块内存空间（是否为同一个对象）。
         *      - 'equals()' 方法默认行为（未重写时）等价于 '=='，即比较内存地址：
         *        public boolean equals(Object obj) { return (this == obj); }
         *      - 'equals()' 被重写后（如 String, Integer, Date, 或自定义 JavaBean），通常根据业务规则比较两个对象的【逻辑内容/属性值】是否相同。
         */

        System.out.println("1. [实测 1：基本类型 vs 引用类型]");
        int a = 100;
        int b = 100;
        System.out.println("   基本类型 int 比较 a == b: " + (a == b) + " (比较数值字面量)");

        System.out.println("\n2. [实测 2：String 对象的 == 与 equals 行为分析]");
        String str1 = new String("hello");
        String str2 = new String("hello");
        String str3 = "hello";
        String str4 = "hello";

        System.out.println("   new String('hello') == new String('hello'): " + (str1 == str2) + " (两个不同堆对象，地址不同)");
        System.out.println("   str1.equals(str2): " + str1.equals(str2) + " (String重写了equals，逐字符比较内容)");
        System.out.println("   字符串常量池 'hello' == 'hello': " + (str3 == str4) + " (指向常量池中同一个字符串实例)");

        System.out.println("\n3. [实测 3：自定义实体类是否重写 equals 的本质差异]");
        UserWithEqualsAndHashCode u1 = new UserWithEqualsAndHashCode(1001L, "张三", "研发部");
        UserWithEqualsAndHashCode u2 = new UserWithEqualsAndHashCode(1001L, "张三", "研发部");

        System.out.println("   两个不同 new 出来的用户对象 u1 == u2: " + (u1 == u2) + " (物理地址不同)");
        System.out.println("   重写了 equals 的用户对象 u1.equals(u2): " + u1.equals(u2) + " (业务属性一致，逻辑相等)");
        System.out.println("   防御空指针推荐用法 Objects.equals(u1, u2): " + Objects.equals(u1, u2));
    }

    /**
     * 问题三：hashcode和equals方法有什么关系?
     *
     * 面试核心考点：
     * 1. Object 规范中的 Hash Code General Contract（三大铁律）。
     * 2. 为什么重写 equals() 时必须同时重写 hashCode()？
     * 3. 只重写 equals 不重写 hashCode 在 HashMap / HashSet 中的灾难性后果（实测演示）。
     * 4. 哈希碰撞（Hash Collision）与两阶段查找机制。
     */
    public static void explainHashCodeAndEqualsContract() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题三：hashcode和equals方法有什么关系?");
        System.out.println("--------------------------------------------------");

        /*
         * 【官方 Java 语言规范（The General Contract）三大铁律】：
         * 1. 强一致性约束：若两个对象通过 equals(Object o) 比较返回 true，则它们的 hashCode() 必须返回完全相同的整数！
         * 2. 弱逆向关系：若两个对象的 hashCode() 返回相同整数，它们通过 equals() 比较并不一定返回 true（这被称为“哈希冲突/哈希碰撞”）。
         * 3. 不等性质：若两个对象的 hashCode() 不相等，则它们通过 equals() 比较一定返回 false。
         *
         * 【为什么在哈希集合（HashMap / HashSet）中必须保持一致？】
         * HashMap 的 get(key) 和 put(key, value) 采用两阶段高效查找算法：
         * 阶段一（哈希定位）：利用 key.hashCode() 计算 hash 值，快速定位数组桶下标（bucket index = (n - 1) & hash）。
         * 阶段二（精确比对）：如果桶内存在元素，再通过 (e.hash == hash && ((k = e.key) == key || key.equals(k))) 精确核对。
         *
         * 如果只重写了 equals 而没有重写 hashCode：
         * 两个内容一模一样（equals 为 true）的对象，会因为继承自 Object 产生两个完全不同的随机 hashCode！
         * 导致 HashMap 将它们散列到不同的数组桶中，出现【明明存进去了，用等价的 key 却查出 null】以及【在 HashSet 中插入了重复数据】的严重 Bug！
         */

        System.out.println("1. [实战验证 1：规范实体 UserWithEqualsAndHashCode 在 Map 中的正常表现]");
        Map<UserWithEqualsAndHashCode, String> correctMap = new HashMap<>();
        UserWithEqualsAndHashCode u1 = new UserWithEqualsAndHashCode(1L, "李四", "架构部");
        UserWithEqualsAndHashCode u2 = new UserWithEqualsAndHashCode(1L, "李四", "架构部");

        correctMap.put(u1, "高级架构师");
        System.out.println("   u1.equals(u2): " + u1.equals(u2));
        System.out.println("   u1.hashCode(): " + u1.hashCode() + " | u2.hashCode(): " + u2.hashCode());
        System.out.println("   使用新实例 u2 从 Map 中查询结果: " + correctMap.get(u2) + " (成功定位命中！)");

        System.out.println("\n2. [实战灾难演示 2：违规实体 UserWithoutHashCode（只重写 equals，没重写 hashCode）]");
        Map<UserWithoutHashCode, String> bugMap = new HashMap<>();
        UserWithoutHashCode bad1 = new UserWithoutHashCode(2L, "王五");
        UserWithoutHashCode bad2 = new UserWithoutHashCode(2L, "王五");

        bugMap.put(bad1, "中级工程师");
        System.out.println("   bad1.equals(bad2): " + bad1.equals(bad2) + " (equals 判定内容相同)");
        System.out.println("   bad1.hashCode(): " + bad1.hashCode() + " | bad2.hashCode(): " + bad2.hashCode() + " (hashCode 产生分歧！)");
        System.out.println("   使用内容相同的 bad2 尝试从 Map 获取: " + bugMap.get(bad2) + " [致命问题：返回 null！查找丢失]");

        Set<UserWithoutHashCode> badSet = new HashSet<>();
        badSet.add(bad1);
        badSet.add(bad2);
        System.out.println("   向 HashSet 插入两个逻辑相等的 bad1 和 bad2 后集合大小: " + badSet.size() + " [致命问题：Set 出现了重复元素，唯一性被破坏！]");
    }
}
