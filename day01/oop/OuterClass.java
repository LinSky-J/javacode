package oop;

/**
 * 外部类宿主实体：专门用于演示【非静态内部类和静态内部类的区别】以及【底层编译器如何通过 this$0 实现外部访问】。
 *
 * 核心考点：
 * 1. 静态内部类（Static Nested Class）：
 *    - 用 static 修饰，属于外部类本身，不依赖外部类实例即可独立存在。
 *    - 只能访问外部类的静态成员，无法访问外部类的实例成员。
 * 2. 非静态内部类（Non-Static Inner Class / Member Inner Class）：
 *    - 必须依附于某一个具体的外部类实例而存在（outerInstance.new InnerClass()）。
 *    - 可以直接无缝访问外部类的所有属性和方法（包括 private 私有属性和方法）。
 * 3. 编译器底层机制：
 *    - 编译器在编译非静态内部类时，会在内部类中悄悄合成一个 private final OuterClass this$0 成员字段。
 *    - 并在内部类的构造函数中追加一个外部类类型的形参，将外部类对象实例自动传入并赋值给 this$0。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class OuterClass {

    /**
     * 外部类私有实例变量
     */
    private String outerName;

    /**
     * 外部类私有静态变量
     */
    private static String staticOuterInfo = "外部类静态信息";

    /**
     * 构造函数
     *
     * @param outerName 外部类宿主名称
     */
    public OuterClass(String outerName) {
        this.outerName = outerName;
    }

    /**
     * 外部类私有实例方法
     */
    private void privateOuterMethod() {
        System.out.println("   [外部类私有方法触发] 来自宿主: " + outerName);
    }

    /**
     * 静态内部类：独立存在，无需持有外部类实例引用
     */
    public static class StaticNestedClass {
        public void display() {
            System.out.println("   [静态内部类] 访问外部静态变量: " + staticOuterInfo + " (无法直接访问非静态的 outerName)");
        }
    }

    /**
     * 非静态内部类：绝对依附于外部类实例，编译器合成 this$0
     */
    public class NonStaticInnerClass {
        public void display() {
            System.out.println("   [非静态内部类] 顺畅直接读取外部类私有属性: " + outerName);
            privateOuterMethod(); // 直接调用外部类私有方法
        }
    }

    /**
     * 获取外部类宿主名称
     *
     * @return 外部类宿主名称
     */
    public String getOuterName() {
        return outerName;
    }

    @Override
    public String toString() {
        return "OuterClass{outerName='" + outerName + "'}@" + Integer.toHexString(System.identityHashCode(this));
    }
}
