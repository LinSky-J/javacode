package java8;

/**
 * 自定义计算函数式接口。
 * 演示 Java 8 接口的新特性：单抽象方法、default 默认方法与 static 静态方法。
 *
 * @author InterviewGuide
 * @version 1.0
 */
@FunctionalInterface
public interface CustomCalculationService {

    /**
     * 单一核心抽象运算方法
     *
     * @param a 操作数1
     * @param b 操作数2
     * @return 运算结果
     */
    double calculate(double a, double b);

    /**
     * Java 8 新特性：default 默认方法。
     * 允许接口提供默认实现，子类/实现者可按需选择覆盖或直接继承使用。
     */
    default void logCalculation(double a, double b, double result) {
        System.out.println("   [计算日志] a=" + a + ", b=" + b + ", 运算结果=" + result);
    }

    /**
     * Java 8 新特性：static 静态方法。
     * 接口中可直接定义工具类静态方法，直接通过 接口名.方法名() 调用。
     */
    static boolean isPositive(double value) {
        return value > 0;
    }
}
