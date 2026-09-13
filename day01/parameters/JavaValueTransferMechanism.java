package parameters;

/**
 * 面试专题：Java 参数传递机制——值传递与引用传递的本质区别与硬核验证。
 *
 * 本类对应面试核心题目：
 * 值传递和引用传递的区别？
 *
 * 核心面试结论：
 * Java 中【只有且仅有值传递（Pass by Value）】，绝对不存在所谓的引用传递！
 *
 * 设计目标：
 * 通过规范的内存模型推导与可运行的对比实验代码，
 * 彻底扫清开发人员在“形参修改对象属性生效”与“形参重新赋值失效”上的核心认知混淆。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaValueTransferMechanism {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     Java 参数传递机制：值传递 vs 引用传递深度剖析   ");
        System.out.println("==================================================");

        explainDefinitionsAndCoreDifference();

        System.out.println("\n----------------- 实验一：基本数据类型传递 -----------------");
        testPrimitivePassing();

        System.out.println("\n----------------- 实验二：引用类型属性修改 -----------------");
        testObjectPropertyMutation();

        System.out.println("\n----------------- 实验三：引用类型重新赋值 -----------------");
        testObjectReferenceReassignment();

        System.out.println("\n==================================================");
        System.out.println("             实验验证完成，请阅读注释深度内化       ");
        System.out.println("==================================================");
    }

    /**
     * 第一部分：值传递与引用传递的学术定义与本质区别
     *
     * 面试标准概念拆解：
     */
    public static void explainDefinitionsAndCoreDifference() {
        /*
         * 1. 什么是【值传递（Pass by Value）】？
         *    - 方法调用时，实参（调用方传入的变量）将其所保存的【值内容】复制一份（Copy），
         *      将这个副本传递给被调用方法的形参（接收参数的局部变量）。
         *    - 核心特性：方法内部对形参的任何操作，操作的都是那份局部副本，绝不可能改变调用方实参变量本身所保存的值。
         *
         * 2. 什么是【引用传递（Pass by Reference）】？
         *    - 方法调用时，实参将其本身的【内存存储单元地址（别名 Alias）】直接传递给方法。
         *    - 核心特性：形参和实参完全共享同一个变量存储单元（形参就是实参的真身别名）。
         *      如果在方法内部让形参指向一个全新的对象或者赋空，外部的实参变量本身会同步改变！
         *    - 代表语言：C++ 中的引用传递（如 void swap(int& a, int& b) 或 void update(Person*& p)）。
         *
         * 3. 为什么很多初学者误以为 Java 对象传递是“引用传递”？
         *    - 致命误区混淆点：
         *      初学者常混淆【引用类型（Reference Type）】与【引用传递（Pass by Reference）】。
         *      Java 中的变量类型确实分为“基本数据类型”和“引用数据类型”；
         *      但是，当传递引用数据类型时，传递机制依然是【值传递】！
         *      传递的“值”是什么？就是【该对象在堆内存中的内存地址编号（如 0x7FFF1234）的数值副本】！
         */
        System.out.println("核心论点：Java 只有值传递！基本类型传递数值副本，引用类型传递堆内存地址数值副本。");
    }

    /**
     * 第二部分：实验一——基本数据类型的参数传递验证
     *
     * 场景：试图在 swap 方法中交换两个 int 变量的值。
     */
    public static void testPrimitivePassing() {
        int a = 10;
        int b = 20;
        System.out.println("调用 swap 前: a = " + a + ", b = " + b);

        swapPrimitives(a, b);

        System.out.println("调用 swap 后: a = " + a + ", b = " + b);
        /*
         * 运行结果分析：
         * 调用 swap 之后，a 依然是 10，b 依然是 20，完全没有改变！
         *
         * 内存栈帧原理解析：
         * 1. main 方法执行时，main 栈帧的局部变量表中分配了 a=10, b=20。
         * 2. 调用 swapPrimitives 时，JVM 在当前线程的虚拟机栈顶压入一个新的栈帧（swapPrimitives 栈帧）。
         * 3. JVM 将 main 栈帧中 a 和 b 的值（10 和 20）各复制一份，写入 swap 栈帧的局部变量 x 和 y 中。
         * 4. swap 栈帧内执行 temp 交换，交换的纯粹是 x 和 y 这两个副本。
         * 5. swap 方法执行完毕出栈销毁，main 栈帧中的 a 和 b 毫发无损。
         */
    }

    private static void swapPrimitives(int x, int y) {
        int temp = x;
        x = y;
        y = temp;
        System.out.println("swap 内部操作后: x = " + x + ", y = " + y);
    }

    /**
     * 第三部分：实验二——引用类型修改对象内部属性
     *
     * 场景：传入 User 对象，在方法内部修改对象的 name 属性。
     */
    public static void testObjectPropertyMutation() {
        User user = new User("张三", 20);
        System.out.println("调用 modifyName 前: " + user);

        modifyUserName(user);

        System.out.println("调用 modifyName 后: " + user);
        /*
         * 运行结果分析：
         * user 的名字被成功改为了“李四”。
         *
         * 为什么修改生效了？这是“引用传递”吗？
         * 绝对不是！这依然是“值传递”！
         *
         * 内存栈堆原理解析：
         * 1. main 方法中 new User("张三", 20) 在【堆内存（Heap）】中开辟了一块空间（假设内存地址为 0x1000）。
         * 2. main 栈帧的局部变量 user 保存的值就是这个地址 0x1000。
         * 3. 调用 modifyUserName(user) 时，JVM 将地址值 0x1000 复制一份，传递给该方法的形参 targetUser。
         * 4. 此时，有两个独立的变量（main 中的 user 和 modify 中的 targetUser），
         *    但它们保存的数值完全相同，都指向堆内存中 0x1000 处的同一个真实对象。
         * 5. targetUser.setName("李四") 顺着地址找到了堆中该对象，修改了其属性。
         * 6. 因此，main 方法观察该对象时，属性确实变了。这是因为【操作了同一个堆对象】，而不是因为引用传递！
         */
    }

    private static void modifyUserName(User targetUser) {
        targetUser.setName("李四");
        System.out.println("modifyUserName 内部设置为李四后: " + targetUser);
    }

    /**
     * 第四部分：实验三——铁证！引用类型形参重新赋值（Reassignment）
     *
     * 场景：在方法内部将形参指向一个全新的 new User 对象。
     */
    public static void testObjectReferenceReassignment() {
        User user = new User("王五", 25);
        System.out.println("调用 reassignUser 前: " + user);

        reassignUser(user);

        System.out.println("调用 reassignUser 后: " + user);
        /*
         * 终极铁证原理解析（面试必杀技）：
         * 1. 如果 Java 是“引用传递”，那么形参 targetUser 和实参 user 应该是同一个变量指针。
         *    当 targetUser = new User("赵六", 30) 时，外部的 user 应该同步指向“赵六”。
         * 2. 实际运行结果是：外部的 user 依然是“王五”，完全没有改变！
         * 3. 原因剖析：
         *    - 实参 user 的值是 0x2000（指向王五）。
         *    - 形参 targetUser 最初拷贝了 0x2000。
         *    - 在 reassignUser 内部执行 new User("赵六", 30) 时，在堆中开辟了新的地址 0x3000。
         *    - targetUser = new User(...) 仅仅是将 targetUser 局部变量的值改为了 0x3000。
         *    - 这一赋值操作完全无法触碰、也无法影响 main 栈帧中的实参 user（其依然牢牢保存着 0x2000）。
         * 4. 结论：这个实验彻底粉碎了“Java 是引用传递”的谣言，铁证证明 Java 是纯粹的值传递。
         */
    }

    private static void reassignUser(User targetUser) {
        // 让形参指向一个新的堆对象
        targetUser = new User("赵六", 30);
        System.out.println("reassignUser 内部新赋对象后: " + targetUser);
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
}
