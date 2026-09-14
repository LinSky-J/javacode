package parameters;

/**
 * 面试专题：真正的引用传递（Pass by Reference）深度解析与机制探秘。
 *
 * 本类对应面试核心题目：
 * 那真正的引用传递是什么讲解一下。
 *
 * =====================================================================================
 * 【核心认知：到底什么是“真正的引用传递”？】
 * =====================================================================================
 *
 * 1. 严格学术定义：
 *    在“真正引用传递”的语言机制中，方法调用时，实参【根本不复制任何东西】！
 *    既不复制数值，也不复制地址指针！
 *    而是直接把实参变量本身的【内存物理存储单元（别名 Alias）】传递给方法形参。
 *
 * 2. 通俗大白话比喻：【一个人的大名与小名】
 *    - 假设你叫“张三”，你的小名叫“狗蛋”。
 *    - “张三”（实参）和“狗蛋”（形参）指的是完完全全同一个人、同一个肉身！
 *    - 别人给“狗蛋”塞了 100 块钱，“张三”的口袋里立刻多了 100 块钱。
 *    - 别人给“狗蛋”换了一身西装，“张三”身上穿的直接就是这身西装。
 *    - 在方法内部给形参重新赋值，就等于直接给实参重新赋值，两者完全合二为一！
 *
 * 3. 真正的引用传递在代码中的两大约束标准（Java 都做不到）：
 *    - 标准一：基本类型 swap(a, b)，在方法内交换形参，外部实参【直接互换】。
 *    - 标准二：对象指针 reassign(u)，在方法内形参指向新对象，外部实参【直接同步变向新对象】。
 *
 * 4. 为什么 Java 的设计者（高斯林）坚决废除了引用传递？
 *    - 【副作用不可控（Side Effects）】：在大型系统中，如果方法能随意改动外部实参变量本身的指向，
 *      一个方法深层调用可能悄无声息地把外部实参篡改成 null 或其他对象，排查 Bug 将会是灾难。
 *    - 【安全与健壮性】：Java 崇尚健壮、安全、易读。强制只有值传递，保证了任何函数调用都绝不可能
 *      隔空改写调用方栈帧中的局部变量指针！
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class TruePassByReferenceExplanation {

    public static void main(String[] args) {

        System.out.println("======================================================================");
        System.out.println("           真正的引用传递（Pass by Reference）全景解析与实证          ");
        System.out.println("======================================================================");

        explainDefinitionAndCppSyntax();

        System.out.println("\n----------------- 演示一：真正的引用传递到底长什么样？（C++ 代码对照） ------");
        showCppReferencePassingCode();

        System.out.println("\n----------------- 演示二：在 Java 中如何模拟出“引用传递”的效果？ ------------");
        simulateReferencePassingInJava();

        System.out.println("\n----------------- 演示三：为什么 Java 模拟的本质依然是“值传递”？ ------------");
        explainWhySimulationIsStillValuePassing();

        System.out.println("\n======================================================================");
        System.out.println("     总结：真正的引用传递是【形参为实参的别名】；Java 永不提供，只有值传递！   ");
        System.out.println("======================================================================");
    }

    /**
     * 第一部分：真正的引用传递核心定义与特征
     */
    public static void explainDefinitionAndCppSyntax() {
        System.out.println("【真正的引用传递核心特征】：");
        System.out.println("1. 零拷贝：调用方法时不产生任何副本（既不复印数据，也不复印门牌号）。");
        System.out.println("2. 变量别名：形参就是实参在另一个方法里的“外号/代称”。");
        System.out.println("3. 双向绑定：形参被赋给任何新值或新对象，外部实参【瞬间同步被篡改】！");
    }

    /**
     * 第二部分：以具备真正引用传递的语言（C++）为例，讲解其语法与行为
     */
    public static void showCppReferencePassingCode() {
        /*
         * 在 C++ 语言中，可以通过 '&' 符号显式声明【真正的引用传递】：
         *
         * // ==========================================
         * // 场景 A：基本类型的真实引用传递（交换两数）
         * // ==========================================
         * void trueSwap(int& x, int& y) { // 注意这里的 '&'，代表 x 和 y 是引用传递！
         *     int temp = x;
         *     x = y;
         *     y = temp;
         * }
         *
         * int a = 10, b = 20;
         * trueSwap(a, b);
         * // 在 C++ 执行完这行后，外部的 a 真正变成了 20，b 真正变成了 10！
         * // 因为 x 就是 a 的别名，y 就是 b 的别名！
         *
         * // ==========================================
         * // 场景 B：对象指针的真实引用传递（篡改外部对象指向）
         * // ==========================================
         * void trueReassign(User*& target) { // 指针的引用传递！
         *     target = new User("赵六"); // 形参指向新对象
         * }
         *
         * User* myUser = new User("王五");
         * trueReassign(myUser);
         * // 在 C++ 执行完这行后，外部的 myUser 真正变成了 "赵六"！
         * // 原本指向 "王五" 的指针，被外部同步改成了指向 "赵六"！
         */
        System.out.println("【C++ 真引用传递代码特征】：");
        System.out.println("void swap(int& x, int& y) -> 形参带 & 符号，形参互换，外部实参 a 和 b 真正互换！");
        System.out.println("void reassign(User*& target) -> 形参换新对象，外部实参指针瞬间变向新对象！");
        System.out.println(">> 这，就是学术界和工程界所说的【真正的引用传递】！");
    }

    /**
     * 第三部分：Java 既然没有引用传递，现实开发中如果非要达到这种效果，该怎么做？
     *
     * 解决方案：使用【引用包装器（Reference Wrapper / Holder）】模拟真正的引用传递！
     */
    public static void simulateReferencePassingInJava() {
        System.out.println("在 Java 中，如果你非要实现一个方法能把外部对象的指向给换掉，该怎么办？");

        // 创建一个包装容器，里面装载着原始对象 "王五"
        RefHolder<User> holder = new RefHolder<>(new User("王五", 25));
        System.out.println("1. [调用前 - main] 容器内装载着原始用户：" + holder.get());

        // 调用模拟方法
        simulateReassign(holder);

        System.out.println("3. [调用后 - main] 容器内装载的用户变为：" + holder.get());
        System.out.println(">> 现象：通过包装器，外部感应到了对象指向被改变！");
    }

    private static void simulateReassign(RefHolder<User> targetHolder) {
        System.out.println("   [方法内部] 接收到包装器容器");
        // 改写容器内部维护的引用
        targetHolder.set(new User("赵六（新对象）", 30));
        System.out.println("2. [方法内部] 将容器内部的引用改为了赵六");
    }

    /**
     * 第四部分：剖析 Java 模拟的本质——依然是“值传递”！
     */
    public static void explainWhySimulationIsStillValuePassing() {
        /*
         * 为什么 Java 的 RefHolder 包装器看起来像引用传递，底层依然是值传递？
         *
         * 1. main 方法创建了一个 RefHolder 容器对象（地址假设为 0xAAAA）。
         * 2. 调用 simulateReassign(holder) 时，依然把容器的地址 0xAAAA 复制了一份副本给形参 targetHolder！
         * 3. 形参 targetHolder.set(...) 只是调用了 0xAAAA 这个容器的方法，修改了该容器内部的一个属性值！
         * 4. 如果在 simulateReassign 内部执行：
         *    targetHolder = new RefHolder<>(...);
         *    外部的 holder 变量依然丝毫不受影响！
         *
         * 结论：
         * Java 本身在语言语法设计上【彻底切断了引用传递的所有通道】！
         * 任何花哨的模拟，其底层全部建立在“复制地址数值的值传递”之上！
         */
        System.out.println("【底层剖析】：");
        System.out.println("Java 包装器能改内部对象，是因为传给方法的依然是【容器地址的副本（值传递）】！");
        System.out.println("Java 语言从根本上保证：方法形参不可能改掉外部调用方实参变量本身所保存的任何值！");
    }

    /**
     * 辅助类：引用包装器（Holder），用于模拟引用传递效果
     *
     * @param <T> 包装的数据类型
     */
    static class RefHolder<T> {
        private T value;

        public RefHolder(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    /**
     * 辅助实体类：User 业务对象
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

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + "}";
        }
    }
}
