package oop.features;

/**
 * 计算器实体类：专门用于演示【编译期静态多态 / 方法重载（Method Overloading）】。
 *
 * 重载（Overload）核心定义：
 * 在同一个类中，存在多个同名方法，但它们的参数列表互不相同（参数个数、参数类型、参数顺序至少有一个不同）。
 *
 * 底层执行机制：
 * 静态分派（Static Dispatch）：重载方法的调用目标在 Java 编译阶段就已经根据形参的静态类型完全确定，
 * 并直接将符号引用硬编码到 class 字节码指令中（invokevirtual / invokestatic）。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class Calculator {

    /**
     * 基础加法：两个整数相加
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @return 整数相加和
     */
    public int add(int a, int b) {
        return a + b;
    }

    /**
     * 重载加法：参数类型改变（两个双精度浮点数相加）
     *
     * @param a 第一个浮点数
     * @param b 第二个浮点数
     * @return 浮点数相加和
     */
    public double add(double a, double b) {
        return a + b;
    }

    /**
     * 重载加法：参数个数改变（三个整数相加）
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @param c 第三个整数
     * @return 三数相加和
     */
    public int add(int a, int b, int c) {
        return a + b + c;
    }
}
