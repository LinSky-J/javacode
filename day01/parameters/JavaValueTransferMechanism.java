package parameters;

/**
 * 面试专题：Java 参数传递机制——值传递与引用传递的本质区别（极简通俗教学版）。
 *
 * 本类对应面试核心题目：
 * 值传递和引用传递的区别?
 *
 * =====================================================================================
 * 【核心结论：请牢记这一句话】
 * Java 中【只有值传递】，绝对没有引用传递！
 * 无论是基本数据类型（int、double等），还是对象引用类型（User、String等），全部都是【值传递】！
 * =====================================================================================
 *
 * 【终极生活化通俗比喻：房产与门牌号小纸条】
 *
 * 场景一：基本数据类型传递（如 int a = 10）
 * - 你的手里有一张便签纸，上面手写着数字 10。
 * - 你把便签纸【复印】了一份给朋友（方法形参）。
 * - 朋友拿橡皮擦把自己手里的复印件改成了 20。
 * - 请问：你手里的原件会变成 20 吗？
 * - 答：绝对不会！你手里依然是 10。
 * - 结论：这就是基本类型的值传递（传递的是数值本身的副本）。
 *
 * 场景二：对象类型传递之“修改属性”（如 user.setName("李四")）
 * - 你在真实世界里有一套物理房子（堆内存中的 User 对象实体）。
 * - 你手里有一张写着这套房子【门牌号 0x1000】的便签纸（实参 user 变量）。
 * - 你把写着门牌号的便签纸【复印】了一份给中介（形参 targetUser）。
 * - 中介拿着这张复印纸，顺着门牌号 0x1000 找到了你的物理房子，把客厅里的沙发换成了红色（修改内部属性）。
 * - 你回到家，看到沙发变成红色了吗？
 * - 答：变了！
 * - 提问：这是因为中介拿到了你手里的原版便签纸吗？
 * - 答：不是！是因为你们手里的两张便签纸上，写着【同一个门牌号 0x1000】，指向的是同一套物理房子！
 * - 结论：这依然是值传递（传递的是门牌号地址数值的副本）！
 *
 * 场景三：对象类型传递之“重新赋值”（如 targetUser = new User("赵六")）
 * - 依然如上，中介手里有一张写着门牌号 0x1000 的复印便签纸。
 * - 中介突然自己去郊区新盖了一套房子（门牌号 0x2000）。
 * - 中介拿橡皮擦把自己手里那张复印便签纸上的门牌号改成了 0x2000（重新赋值）。
 * - 请问：你手里写着 0x1000 的便签纸，会被隔空自动修改成 0x2000 吗？
 * - 答：绝对不可能！你手里的便签纸依然牢牢写着 0x1000！
 * - 结论：如果是所谓的“引用传递”，中介改门牌号时你手里的纸条也必须跟着变成 0x2000。
 *         但在 Java 中，你的纸条完全不变！这铁证如山地证明了中介拿到的只是一份【复印件】！
 *
 * =====================================================================================
 * 【三维对比表：扫清一切困惑】
 * -------------------------------------------------------------------------------------
 * 传递机制         实参传递给形参的内容             形参修改内容对外部实参的影响
 * -------------------------------------------------------------------------------------
 * Java 基本类型    具体的数值（如 10）的复印件       修改形参，外部实参毫无变化
 * Java 对象类型    堆内存地址（如 0x1000）的复印件   1. 修改对象属性：外部能看到（操作同一房子）
 *                                                  2. 形参重新赋值：外部完全不受影响（只改了复印件）
 * 真正的引用传递   实参变量本体自身（内存物理别名）  形参一旦重新赋值，外部实参跟着瞬间变向新对象！
 * (如 C++ 引用)                                    (Java 语法中根本不存在这种机制)
 * -------------------------------------------------------------------------------------
 *
 * @author InterviewGuide
 * @version 3.0
 */
public class JavaValueTransferMechanism {

    public static void main(String[] args) {
        initConsoleEncoding();

        System.out.println("======================================================================");
        System.out.println("          Java 值传递与引用传递 本质区别清晰教学运行演示               ");
        System.out.println("======================================================================");

        printMetaphorIntroduction();

        System.out.println("\n----------------- 实验一：基本类型传递（复印数字便签） -----------------");
        testPrimitivePassing();

        System.out.println("\n----------------- 实验二：对象传递修改属性（按门牌号进屋换沙发） ---------");
        testObjectPropertyMutation();

        System.out.println("\n----------------- 实验三：对象传递重新赋值（中介擦改自己的门牌号便签） ----");
        testObjectReferenceReassignment();

        System.out.println("\n----------------- 实验四：对比真正的引用传递（C++ vs Java） ------------");
        compareWithTruePassByReference();

        System.out.println("\n======================================================================");
        System.out.println("          清晰教学完毕：请记住，Java 中任何传递全都是在【做复印】！         ");
        System.out.println("======================================================================");
    }

    /**
     * 第一部分：通俗比喻开篇导读
     */
    public static void printMetaphorIntroduction() {
        System.out.println("【一句话心法】：无论传递什么，Java 永远都在做【复印】！");
        System.out.println("1. 传基本类型：复印的是具体的【数值】（如 10 复制一份变成两个 10）。");
        System.out.println("2. 传对象类型：复印的是对象在堆里的【门牌号地址】（如 0x1000 复制一份变成两份 0x1000）。");
    }

    /**
     * 第二部分：实验一——基本数据类型的参数传递验证
     *
     * 场景：试图在 swap 方法中交换两个 int 变量。
     */
    public static void testPrimitivePassing() {
        int a = 10;
        int b = 20;
        System.out.println("【步骤 1】main 方法中定义原始数字：a = " + a + ", b = " + b);

        // 调用方法，将 a 和 b 的值复印一份传入
        swapPrimitives(a, b);

        System.out.println("【步骤 3】swap 执行完毕后回到 main：a = " + a + ", b = " + b);
        System.out.println("【清晰结论】：main 中的 a 和 b 依然是 10 和 20，完全没变。");
        System.out.println("             因为 swap 方法拿到的只是复印件，改复印件影响不到原件。");
    }

    private static void swapPrimitives(int x, int y) {
        System.out.println("   --> 进入 swap 方法，收到复印件：x = " + x + ", y = " + y);
        int temp = x;
        x = y;
        y = temp;
        System.out.println("【步骤 2】swap 内部将复印件交换为：x = " + x + ", y = " + y);
    }

    /**
     * 第三部分：实验二——对象传递修改属性（按门牌号进屋换沙发）
     *
     * 场景：传入 User 对象，在方法内部修改对象的 name 属性。
     * 关键疑惑：为什么外部名字变了？这算引用传递吗？
     */
    public static void testObjectPropertyMutation() {
        // 在堆中造了一套物理房子，得到门牌号地址
        User user = new User("张三", 20);
        String mainAddress = getMemoryAddress(user);

        System.out.println("【步骤 1】main 中建造了房子，拿到了门牌号便签：" + mainAddress + "，屋内住着：" + user);

        // 把门牌号便签复印一份交给 modifyUserName 方法
        modifyUserName(user);

        System.out.println("【步骤 3】方法执行完毕回到 main，检查房子：" + user);
        System.out.println("          main 拿的门牌号依然是：" + getMemoryAddress(user));
        System.out.println("【清晰结论】：为什么名字变成了'李四'？");
        System.out.println("             因为 main 和方法手里的便签写着【相同的门牌号 " + mainAddress + "】。");
        System.out.println("             方法顺着门牌号找到同一个房子换了沙发，main 回家自然看到了新沙发。");
        System.out.println("             这叫【通过地址副本访问同一个对象】，绝不是引用传递！");
    }

    private static void modifyUserName(User targetUser) {
        String calleeAddress = getMemoryAddress(targetUser);
        System.out.println("   --> 进入方法，收到复印的门牌号便签：" + calleeAddress);
        System.out.println("       对比验证：实参与形参保存的门牌号完全一致！指向同一栋房子。");

        // 进屋改名字
        targetUser.setName("李四");
        System.out.println("【步骤 2】方法内部顺着门牌号进屋，将名字改为李四：" + targetUser);
    }

    /**
     * 第四部分：实验三——对象传递重新赋值（中介擦改自己的门牌号便签）
     *
     * 场景：在方法内部执行 targetUser = new User("赵六", 30)，把形参重新指向一个全新的堆对象。
     * 决定性提问：如果 Java 是引用传递，外部实参 user 的门牌号会不会被同步篡改？
     */
    public static void testObjectReferenceReassignment() {
        // main 里的原始房子：王五
        User user = new User("王五", 25);
        String originalAddress = getMemoryAddress(user);

        System.out.println("【步骤 1】main 中造了房子，拿到门牌号便签：" + originalAddress + "，屋内住着：" + user);

        // 把门牌号复印一份交给 reassignUser 方法
        reassignUser(user);

        String afterAddress = getMemoryAddress(user);
        System.out.println("【步骤 3】方法执行完毕回到 main，检查原版便签与房子：");
        System.out.println("          main 手里的门牌号便签依然是：" + afterAddress);
        System.out.println("          main 屋内住着的依然是：" + user);

        System.out.println("\n--------------------- 决定性证明：为什么断言只有值传递？ ---------------------");
        System.out.println("1. 如果 Java 存在【引用传递】：");
        System.out.println("   方法内部重新赋值时，外部 main 手里的便签必须被【同步改写为新门牌号】！");
        System.out.println("2. 真实运行结果：");
        System.out.println("   main 手里的便签依然死死指向旧门牌号 " + originalAddress + "（王五），");
        System.out.println("   方法内部无论换成什么新房子，对外部 main 毫无任何影响！");
        System.out.println("3. 核心大白话总结：");
        System.out.println("   形参拿到的只是一张【复印纸】！中介把自己的复印纸擦掉重写，根本伤不到你的原版纸条！");
    }

    private static void reassignUser(User targetUser) {
        System.out.println("   --> 进入方法，形参手里的复印便签最初写着：" + getMemoryAddress(targetUser));

        // 关键操作：在堆内存新盖一套房子（赵六），并把新房子的门牌号写在自己的复印便签上
        targetUser = new User("赵六", 30);
        String newAddress = getMemoryAddress(targetUser);

        System.out.println("【步骤 2】方法内部重新 new 了一套新房子，门牌号为：" + newAddress);
        System.out.println("          此时形参便签被改写为：" + newAddress + "，住着：" + targetUser);
        System.out.println("          (请注意：此操作仅仅修改了当前方法私有的复印便签！)");
    }

    /**
     * 第五部分：横向对比真正的“引用传递”（以 C++ 为例，彻底理解二者差异）
     */
    public static void compareWithTruePassByReference() {
        /*
         * 很多人不理解“引用传递”，是因为在纯 Java 的世界里从来没见过真正的引用传递。
         *
         * 在具备真正引用传递的语言（如 C++）中：
         * -------------------------------------------------------------
         * void testReference(User*& targetUser) {
         *     targetUser = new User("新用户"); // 真正的引用传递！
         * }
         * -------------------------------------------------------------
         * 如果在 C++ 中这样写：
         * 形参 targetUser 不是复印件，它就是外部实参 user 的【真身物理别名】！
         * 只要 targetUser = new User(...) 一执行，外部实参 user 的指针会立刻同步被改写！
         *
         * 而在 Java 中：
         * 无论你怎么写，都绝对不可能写出这种效果！
         * Java 强制所有参数传递都必须【拷贝一份数值副本】（无论是数字还是内存地址编号）。
         * 因此：Java 官方明确定义，Java 只有值传递（Pass by Value）！
         */
        System.out.println("【语言对比认知】：");
        System.out.println("真正引用传递（如 C++）：形参是实参的本尊别名，形参重赋值，外部实参同步变！");
        System.out.println("Java 的传递机制：全部都是拷贝副本，形参重赋值，外部实参纹丝不动！");
    }

    /**
     * 获取对象在 JVM 堆内存中的十六进制身份地址编号。
     *
     * @param obj 目标对象
     * @return 格式化的十六进制地址字符串，例如 "0x77ECA502"
     */
    public static String getMemoryAddress(Object obj) {
        if (obj == null) {
            return "null(0x0)";
        }
        return "0x" + Integer.toHexString(System.identityHashCode(obj)).toUpperCase();
    }

    /**
     * 辅助实体类：User 数据载体
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
