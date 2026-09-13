package exceptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * 面试专题：Java 异常体系全景架构、受检与非受检异常辨析、异常处理五大机制与 finally 踩坑实测。
 *
 * 本类对应面试核心题目：
 * 1. 介绍一下Java异常
 * 2. Java异常处理有哪些？
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaExceptionHierarchyAndHandling {

    public static void main(String[] args) {
        initConsoleEncoding();

        System.out.println("======================================================================");
        System.out.println("          Java 异常体系架构剖析与异常处理机制实战深度解析             ");
        System.out.println("======================================================================");

        explainJavaExceptionHierarchy();
        explainJavaExceptionHandlingWays();

        System.out.println("\n======================================================================");
        System.out.println("            异常体系与处理机制解析完毕，请细读类中源码与注释          ");
        System.out.println("======================================================================");
    }

    /**
     * 第一部分：
     * 介绍一下Java异常
     *
     * 面试核心考点：Throwable 顶层继承树、Error vs Exception、Checked（受检）vs Unchecked（运行时）。
     */
    public static void explainJavaExceptionHierarchy() {
        System.out.println("1. 介绍一下Java异常");

        /*
         * -----------------------------------------------------------------------------------------
         *                                   Throwable（顶级父类）
         *                                      /             \
         *                            Error（严重系统错误）    Exception（程序异常）
         *                             /          \                 /            \
         *                 OutOfMemoryError  StackOverflowError   Checked Exception   RuntimeException（运行时异常）
         *                                                        (受检/编译时异常)        /            \
         *                                                       IOException   NullPointerException  ArrayIndexOutOfBounds
         * -----------------------------------------------------------------------------------------
         *
         * 1. 根基：java.lang.Throwable
         *    - Java 语言中所有错误（Error）和异常（Exception）的超类。只有 Throwable 及其子类才能被 throw 抛出或 catch 捕获。
         *
         * 2. 分支一：Error（严重错误）
         *    - 表示程序无法处理的严重故障，绝大部分与程序员的代码逻辑无关，而是 JVM 虚拟机本身或底层物理资源耗尽引发的崩溃！
         *    - 典型代表：
         *      - OutOfMemoryError (OOM)：堆内存溢出、元空间溢出、直接内存溢出；
         *      - StackOverflowError：方法调用深度过大导致线程虚拟机栈溢出（如死循环递归）；
         *    - 处理原则：应用程序不应该试图使用 try-catch 去捕获 Error，而应排查 JVM 参数、内存泄漏或优化系统架构。
         *
         * 3. 分支二：Exception（程序异常，程序本身可以预料并处理）
         *    Exception 又划分为两大核心阵营：
         *
         *    (1) 受检异常（Checked Exception / 编译时异常）：
         *        - 定义：除了 RuntimeException 及其子类以外的 Exception（如 IOException, SQLException, ClassNotFoundException）。
         *        - 特点：【编译器在编译期强制检查】！方法若可能抛出受检异常，必须显式在签名上 throws 声明，或者使用 try-catch 捕获，
         *          否则代码直接编译报错！通常代表外部不可控因素（如文件不存在、网络中断、数据库断开）。
         *
         *    (2) 非受检异常（Unchecked Exception / 运行时异常）：
         *        - 定义：继承自 RuntimeException 的所有子类（如 NullPointerException, IndexOutOfBoundsException, IllegalArgumentException）。
         *        - 特点：【编译器不强制检查】！编译期无需显式声明或捕获。它通常是程序员代码编写不严密引起的逻辑 Bug，
         *          应当通过完善前置判空与入参校验（Defensive Programming）在代码层面规避，而非滥用 try-catch 掩盖。
         */
        System.out.println("\n--- [受检异常 vs 非受检异常 现场演示] ---");

        // 演示非受检运行时异常（RuntimeException，编译器完全不强制处理）
        try {
            String nullStr = null;
            System.out.println(nullStr.length()); // 触发 NullPointerException
        } catch (NullPointerException e) {
            System.out.println("   [非受检异常拦截] 捕获到运行时空指针异常（程序逻辑漏洞）: " + e);
        }

        // 演示受检编译时异常（必须显式 try-catch 或 throws，否则编译报错）
        try {
            FileInputStream fis = new FileInputStream("non_existent_file.txt");
        } catch (IOException e) {
            System.out.println("   [受检异常拦截] 捕获到编译期强制检查的 IOException: " + e.getMessage());
        }
    }

    /**
     * 第二部分：
     * Java异常处理有哪些？
     *
     * 面试核心考点：try-catch-finally、throws、throw、try-with-resources（Java 7+）、finally 与 return 的执行顺序陷阱。
     */
    public static void explainJavaExceptionHandlingWays() {
        System.out.println("\n\n2. Java异常处理有哪些？");

        /*
         * 机制一：try-catch 捕获处理
         * - 使用 try 监控风险代码块；使用 catch 捕获并处理异常；
         * - 支持多重 catch（注意：子类异常必须写在前面，父类异常写在后面，否则触发编译不可达错误）；
         * - Java 7+ 支持多异常联合捕获：catch (IOException | SQLException e)。
         */
        System.out.println("\n--- 机制一：try-catch 块捕获与处理 ---");
        try {
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            System.out.println("   [try-catch 处理] 成功捕获除以零算术异常: " + e.getMessage());
        }

        /*
         * 机制二：throws 声明向上传播
         * - 标记在方法签名后，表示当前方法不直接处理该异常，而是向上抛给调用方去解决。
         */
        System.out.println("\n--- 机制二：throws 声明抛出（调用方兜底） ---");
        try {
            methodWithThrows();
        } catch (ClassNotFoundException e) {
            System.out.println("   [throws 上层捕获] 成功捕获下层方法 throws 上传的异常: " + e.getMessage());
        }

        /*
         * 机制三：throw 主动显式抛出异常
         * - 在业务逻辑中，遇到非法状态主动 new 一个异常抛出（常用于抛出业务异常 BusinessException）。
         */
        System.out.println("\n--- 机制三：throw 主动抛出业务异常 ---");
        try {
            deductAccountBalance(-500.0);
        } catch (BusinessException e) {
            System.out.println("   [业务异常拦截] 捕获到自定义业务异常: " + e.getMessage());
        }

        /*
         * 机制四：try-with-resources 自动资源回收（Java 7+ 黄金规范）
         * - 在 try(...) 小括号内声明实现 AutoCloseable 接口的资源；
         * - 无论 try 块是正常结束还是发生异常，JVM 保证自动调用 close() 方法释放资源！
         * - 彻底告别过去在 finally 中写繁重且容易漏判空的 reader.close() 代码。
         */
        System.out.println("\n--- 机制四：try-with-resources 自动资源释放实测 ---");
        try (MockDatabaseResource db = new MockDatabaseResource("jdbc:mysql://cloud-cluster:3306/db")) {
            db.executeQuery("SELECT * FROM users WHERE status = 'ACTIVE'");
            // 执行完毕退出 try 作用域时，自动触发 db.close()！
        }

        /*
         * 机制五：finally 块与【经典面试大坑——finally 与 return 的执行顺序】
         * - 无论是否发生异常，finally 块【必定会执行】！
         * - 经典大坑一：如果 finally 块中包含 return 语句，它会彻底覆盖并吞掉 try 块中的 return 或异常！
         * - 经典大坑二：当 try 中 return 基本类型变量时，finally 对该变量的修改【不会影响返回值】（因为返回值已被压入操作数栈）。
         */
        System.out.println("\n--- 机制五：finally 块的执行顺序与 return 经典大坑实测 ---");
        int testResult = testFinallyExecutionOrder();
        System.out.println("   [实测返回值] testFinallyExecutionOrder() 最终得到的返回值是: " + testResult);
        System.out.println("   (核心原理解析：try 块中的 return x 会先将 x 的值 10 暂存到操作数栈中，然后再去执行 finally 块；");
        System.out.println("    finally 中的 x = 20 只修改了局部变量，并不影响已经暂存到栈顶的返回值 10！)");

        /*
         * 机制六：企业级全局统一异常处理（Spring Boot @RestControllerAdvice）
         * - 架构落地：业务代码抛出 BusinessException，由全局拦截切面统一捕获并转换为统一的 JSON 报文（如 ResultWrapper.fail()）。
         */
        System.out.println("\n--- 机制六：企业级框架全局统一异常处理体系 ---");
        System.out.println("   Spring Web 体系通过 @RestControllerAdvice + @ExceptionHandler，");
        System.out.println("   全局拦截未捕获的 Controller 层异常，避免将带有堆栈信息的 500 页面直接暴露给前端用户，保证系统高可用与安全性。");
    }

    /**
     * 演示 throws 声明抛出
     */
    private static void methodWithThrows() throws ClassNotFoundException {
        throw new ClassNotFoundException("无法定位指定的驱动类 com.oracle.jdbc.Driver");
    }

    /**
     * 演示 throw 主动抛出自定义异常
     */
    private static void deductAccountBalance(double amount) {
        if (amount <= 0) {
            throw new BusinessException(100101, "扣款金额非法，金额必须大于 0！");
        }
    }

    /**
     * 经典面试题测试：finally 与 return 的执行时序
     */
    private static int testFinallyExecutionOrder() {
        int x = 10;
        try {
            System.out.println("   [1. try 块] 执行，准备 return x (当前 x=" + x + ")");
            return x; // 此时 10 已经被暂存到返回值栈中！
        } finally {
            x = 20; // 修改局部变量 x
            System.out.println("   [2. finally 块] 执行完毕，修改 x 后的局部变量为: " + x);
        }
    }

    /**
     * 初始化控制台字符编码，解决 Windows 环境终端输出中文乱码的问题。
     */
    private static void initConsoleEncoding() {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}
