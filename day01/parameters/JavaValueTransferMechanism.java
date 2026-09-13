package parameters;

/**
 * 面试专题：Java 参数传递机制——值传递与引用传递的本质区别与内存地址实证。
 *
 * 本类对应面试核心题目：
 * 值传递和引用传递的区别?
 *
 * 核心面试结论：
 * Java 中【只有且仅有值传递（Pass by Value）】，绝对不存在引用传递！
 *
 * 教学核心解析与认知升级：
 * 很多初学者甚至有多年经验的工程师都会产生误区，认为：
 * “传基本数据类型是值传递，传对象类型是引用传递”。
 * 这种理解是完全错误的！
 *
 * 产生误解的根本原因：混淆了【引用类型（Reference Type）】与【引用传递（Pass by Reference）】！
 * - 引用类型：Java 的一种数据类型分类（与基本类型相对，变量存储的是堆内存的寻址指针）。
 * - 引用传递：一种参数传递方式，实参直接将自身的“变量内存单元（别名 Alias）”传给形参。
 *
 * 在 Java 中传递对象时，传递的本质依然是【值传递】：
 * 只不过传递的“值（Value）”，恰好是【该对象在堆内存中的内存地址编号（数值副本）】！
 *
 * -------------------------------------------------------------------------------------
 * 图解一：修改对象属性（为什么外部看得见修改？依然是值传递！）
 * -------------------------------------------------------------------------------------
 * [JVM 虚拟机栈（Stack）]                       [JVM 堆内存（Heap）]
 *
 * main 栈帧:
 * ┌───────────┐
 * │ user 变量  │ ─── 存放地址值 0x15DB9742 ────┐
 * └───────────┘                              │
 *                                            ▼
 * modifyUserName 栈帧:                 ┌───────────────────────────┐
 * ┌───────────┐                        │ 堆内存物理对象 (User 实例)    │
 * │ targetUser│ ─── 拷贝地址值 0x15DB9742 ───▶│ 地址: 0x15DB9742          │
 * └───────────┘                        │ name = "李四" (属性被修改)  │
 * (两个独立的局部变量，持有相同的地址值)        │ age  = 20                 │
 *                                      └───────────────────────────┘
 *
 * -------------------------------------------------------------------------------------
 * 图解二：形参重新赋值（为什么外部毫无变化？彻底粉碎“引用传递”谣言的铁证！）
 * -------------------------------------------------------------------------------------
 * [JVM 虚拟机栈（Stack）]                       [JVM 堆内存（Heap）]
 *
 * main 栈帧:
 * ┌───────────┐
 * │ user 变量  │ ─── 牢牢持有地址 0x6D06D69C ─▶ ┌───────────────────────────┐
 * └───────────┘ (外部实参毫无影响！)            │ 地址: 0x6D06D69C (王五)    │
 *                                              └───────────────────────────┘
 * reassignUser 栈帧:
 * ┌───────────┐
 * │ targetUser│ ─── 内部被改写为 0x4EEC7777 ─▶ ┌───────────────────────────┐
 * └───────────┘ (仅形参私有副本被改变)          │ 地址: 0x4EEC7777 (赵六)    │
 *                                              └───────────────────────────┘
 *
 * @author InterviewGuide
 * @version 2.0
 */
public class JavaValueTransferMechanism {

    public static void main(String[] args) {
        initConsoleEncoding();

        System.out.println("======================================================================");
        System.out.println("        Java 参数传递机制深度解析：打印堆内存地址 铁证揭秘值传递        ");
        System.out.println("======================================================================");

        explainCoreTheory();

        System.out.println("\n----------------------- 实验一：基本数据类型值传递验证 -----------------------");
        testPrimitivePassing();

        System.out.println("\n----------------- 实验二：对象引用类型传递（修改属性）+ 地址追踪 ----------------");
        testObjectPropertyMutation();

        System.out.println("\n----------------- 实验三：对象引用类型传递（重新赋值）+ 终极铁证 ----------------");
        testObjectReferenceReassignment();

        System.out.println("\n======================================================================");
        System.out.println("     总结：Java 只有值传递！基本类型传数值副本，引用类型传堆地址数值副本     ");
        System.out.println("======================================================================");
    }

    /**
     * 理论核心辨析：值传递 vs 引用传递
     */
    public static void explainCoreTheory() {
        /*
         * 1. 严格学术定义：
         *    - 值传递（Pass by Value）：
         *      调用函数时，实参将其所保存的内容【复制一份副本】压入被调用方法的栈帧局部变量表中。
         *      无论被调方法如何修改这个形参，调用的外部实参本身决不可能发生变化。
         *    - 引用传递（Pass by Reference）：
         *      调用函数时，直接将实参本身的【变量存储地址/别名】传入方法。
         *      形参和实参就是同一个变量的不同称呼。在被调方法中如果给形参赋予新对象，外部实参会跟着改变。
         *      典型代表：C++ 中的引用传递（void swap(int& a, int& b) 或 void reassign(Person*& p)）。
         *
         * 2. Java 的设计抉择：
         *    Java 从语法和虚拟机规范层面彻底去掉了引用传递，保证了极高的健壮性与安全性。
         *    Java 中所有的参数传递，无一例外，全都是【值传递】。
         */
        System.out.println("【核心理论】值传递复制变量内容副本；引用传递直接传递变量本体引用别名。Java 全程只有值传递！");
    }

    /**
     * 实验一：基本数据类型的参数传递验证
     *
     * 场景：试图在 swapPrimitives 方法中交换两个 int 变量。
     */
    public static void testPrimitivePassing() {
        int a = 10;
        int b = 20;
        System.out.println("1. [调用前 - main栈帧] a = " + a + ", b = " + b);

        swapPrimitives(a, b);

        System.out.println("3. [调用后 - main栈帧] a = " + a + ", b = " + b);
        System.out.println(">> 现象分析：main 方法中的 a 和 b 依然是 10 和 20，毫无变化。");
        System.out.println(">> 底层原理：swap 栈帧中操作的 x 和 y 是从 main 栈帧复制过去的纯数字副本，出栈后立即销毁。");
    }

    private static void swapPrimitives(int x, int y) {
        System.out.println("   [进入方法 - swap栈帧] 初始接收到数字副本: x = " + x + ", y = " + y);
        int temp = x;
        x = y;
        y = temp;
        System.out.println("2. [方法内部 - swap栈帧] 执行交换后副本为: x = " + x + ", y = " + y);
    }

    /**
     * 实验二：对象引用类型的属性修改 + 打印堆内存地址验证
     *
     * 场景：传入 User 对象，在方法内部通过形参修改 name 属性。
     * 考点：为什么外部实参的属性变了？这算不算引用传递？
     */
    public static void testObjectPropertyMutation() {
        User user = new User("张三", 20);
        String mainAddress = getMemoryAddress(user);
        System.out.println("1. [调用前 - main栈帧] 实参 user 保存的堆对象内存地址: " + mainAddress + ", 内容: " + user);

        modifyUserName(user);

        System.out.println("4. [调用后 - main栈帧] 实参 user 保存的堆对象内存地址: " + getMemoryAddress(user) + ", 内容: " + user);
        System.out.println(">> 深度剖析：为什么 user.getName() 变为了'李四'？");
        System.out.println("   原因不是引用传递，而是【地址值传递】！");
        System.out.println("   main 栈帧把地址数值(" + mainAddress + ")复制了一份给形参 targetUser。");
        System.out.println("   此时栈中有两个独立的变量(user 和 targetUser)，但它们保存的数值完全相同，都指向堆中同一个对象！");
        System.out.println("   targetUser.setName(\"李四\") 顺着该地址修改了堆内存中的数据，所以 main 再次访问该地址时，读到的自然是新名字。");
    }

    private static void modifyUserName(User targetUser) {
        String calleeAddress = getMemoryAddress(targetUser);
        System.out.println("2. [进入方法 - modify栈帧] 形参 targetUser 拷贝到的内存地址: " + calleeAddress + ", 内容: " + targetUser);
        System.out.println("   [内存地址比对] 实参与形参指向的堆内存地址完全一致: " + calleeAddress);

        targetUser.setName("李四");
        System.out.println("3. [方法内部 - modify栈帧] 通过形参修改对象属性后: " + targetUser + ", 地址仍为: " + getMemoryAddress(targetUser));
    }

    /**
     * 实验三：对象引用类型的重新赋值 + 打印堆内存地址验证（终极铁证！）
     *
     * 场景：在方法内部执行 targetUser = new User("赵六", 30)，把形参重新指向一个全新的堆对象。
     * 考点：如果 Java 是引用传递，外部实参 user 的指向必须同步变为“赵六”；如果是值传递，外部实参丝毫不会受影响。
     */
    public static void testObjectReferenceReassignment() {
        User user = new User("王五", 25);
        String originalAddress = getMemoryAddress(user);
        System.out.println("1. [调用前 - main栈帧] 实参 user 指向堆内存地址: " + originalAddress + ", 内容: " + user);

        reassignUser(user);

        String afterAddress = getMemoryAddress(user);
        System.out.println("4. [调用后 - main栈帧] 实参 user 指向堆内存地址: " + afterAddress + ", 内容: " + user);

        System.out.println("\n------------------------ 终极铁证推导结论 ------------------------");
        System.out.println("   调用前后实参地址对比: 调用前=" + originalAddress + " -> 调用后=" + afterAddress + " (完全未变!)");
        System.out.println("   调用前后实参内容对比: 依然是 '王五'，并未变成方法内部新创建的 '赵六'！");
        System.out.println("   【彻底断言】：");
        System.out.println("   如果 Java 是引用传递，形参 targetUser 重新指向新地址时，实参 user 也必须同步指向新地址；");
        System.out.println("   而现实中实参 user 依然牢牢指向 " + originalAddress + "，这确凿无疑地证明了：");
        System.out.println("   形参 targetUser 仅仅是实参 user 地址值的一个【栈内独立副本】！");
        System.out.println("   在方法内部给形参重新赋值，只是修改了副本变量的值，根本不可能影响到调用方 main 栈帧中的实参变量！");
    }

    private static void reassignUser(User targetUser) {
        System.out.println("2. [进入方法 - reassign栈帧] 形参 targetUser 初始拷贝到的地址: " + getMemoryAddress(targetUser) + ", 内容: " + targetUser);

        // 在堆内存中开辟全新空间，实例化新对象，并将新对象的内存地址赋给形参
        targetUser = new User("赵六", 30);
        String newAddress = getMemoryAddress(targetUser);

        System.out.println("3. [方法内部 - reassign栈帧] 执行 targetUser = new User(\"赵六\", 30) 之后:");
        System.out.println("   形参 targetUser 指向了全新堆内存地址: " + newAddress + ", 内容: " + targetUser);
        System.out.println("   (注意：此赋值操作仅改写了当前栈帧内 targetUser 局部变量存储的十六进制数值)");
    }

    /**
     * 获取对象在 JVM 堆内存中的身份哈希标识（十六进制堆内存逻辑地址）。
     *
     * 原理解析：
     * 在 HotSpot 虚拟机中，System.identityHashCode(obj) 返回对象未被重写时的原始 hashCode。
     * 该值在默认情况下直接与对象在堆内存中的内存物理布局/指针地址相关联，
     * 对应 Object 类默认 toString() 方法输出中 '@' 符号后面的十六进制字符串（如 java.lang.Object@15db9742）。
     *
     * @param obj 目标对象
     * @return 格式化的十六进制内存地址字符串，例如 "0x15DB9742"
     */
    public static String getMemoryAddress(Object obj) {
        if (obj == null) {
            return "null(0x0)";
        }
        return "0x" + Integer.toHexString(System.identityHashCode(obj)).toUpperCase();
    }

    /**
     * 辅助实体类：用于实验验证的 User 简易数据载体
     */
    static class User {
        private String name;
        private int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + "}";
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
