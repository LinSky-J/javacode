package exceptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * 面试专题：Java 异常体系全景架构、异常处理五大机制、异常转译、执行流程与 finally return 深度解析。
 *
 * 本类对应面试核心题目：
 * 1. 介绍一下Java异常
 * 2. Java异常处理有哪些?
 * 3. 抛出异常为什么不用throws?
 * 4. try catch中的语句运行情况
 * 5. try{return “a”} finally{return “b”}
 *    这条语句返回啥。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaExceptionHierarchyAndHandling {

    public static void main(String[] args) {
        initConsoleEncoding();

        System.out.println("======================================================================");
        System.out.println("        Java 异常体系、处理机制、throws 辨析与 finally return 实测     ");
        System.out.println("======================================================================");

        explainJavaExceptionHierarchy();
        explainJavaExceptionHandlingWays();
        explainWhyNotAlwaysUseThrows();
        explainTryCatchExecutionFlow();
        explainTryReturnFinallyReturn();

        System.out.println("\n======================================================================");
        System.out.println("            异常全景体系解析完毕，请细读类中源码与注释                ");
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
     * Java异常处理有哪些?
     *
     * 面试核心考点：try-catch-finally、throws、throw、try-with-resources（Java 7+）、全局异常切面。
     */
    public static void explainJavaExceptionHandlingWays() {
        System.out.println("\n\n2. Java异常处理有哪些?");

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
         * 机制五：企业级全局统一异常处理（Spring Boot @RestControllerAdvice）
         * - 架构落地：业务代码抛出 BusinessException，由全局拦截切面统一捕获并转换为统一的 JSON 报文（如 ResultWrapper.fail()）。
         */
        System.out.println("\n--- 机制五：企业级框架全局统一异常处理体系 ---");
        System.out.println("   Spring Web 体系通过 @RestControllerAdvice + @ExceptionHandler，");
        System.out.println("   全局拦截未捕获的 Controller 层异常，避免将带有堆栈信息的 500 页面直接暴露给前端用户，保证系统高可用与安全性。");
    }

    /**
     * 第三部分：
     * 抛出异常为什么不用throws?
     *
     * 面试拔高考点：职责划分、调用链与契约污染、底层细节泄漏、异常转译（Exception Translation）与异常链（Exception Chaining）。
     */
    public static void explainWhyNotAlwaysUseThrows() {
        System.out.println("\n\n3. 抛出异常为什么不用throws?");

        /*
         * 核心原因解析：为什么企业级现代架构严禁盲目、层层滥用 throws？
         *
         * 1. 破坏接口抽象，暴露底层实现细节（严重破坏面向对象封装性）：
         *    - 假设一个 UserService.getUser() 方法在签名上声明了 throws SQLException；
         *    - 调用者立刻知道底层使用了关系型数据库！如果未来底层改造为 Redis、MongoDB 或 RPC 远程调用，
         *      所有调用方的代码都必须跟着修改！
         *    - 面向抽象编程原则要求：高层业务接口不应受到底层持久化细节的绑架。
         *
         * 2. 调用链污染与代码腐化（Throws Pollution）：
         *    - 如果底层 DAO 方法 throws SQLException，Service 层不想处理就接着 throws，
         *      导致上层的 Controller 也必须跟着 throws，最终整个调用链路上的每一个方法都被迫加上无意义的 throws 声明。
         *
         * 3. 丧失故障恢复与降级的最佳时机：
         *    - 当前方法往往是离异常发生现场最近的地方，最清楚上下文业务语义（例如重试、读备用缓存）；
         *    - 一旦盲目 throws 向上甩锅，上层方法通常只知道发生了系统错误，根本无法做到精准恢复。
         *
         * 4. 堆栈泄露与安全隐患：
         *    - 若异常一路 throws 到 Web 最外层，直接把原始异常堆栈抛给前端展示，会泄露表名、SQL 语句等敏感信息。
         *
         * 企业级标准解法：【异常转译（Exception Translation）+ 异常链（Exception Chaining）】
         * - 在当前层通过 try-catch 捕获底层的技术异常；
         * - 转换为业务语义明确的运行时异常（如 throw new BusinessException(500, "支付通道异常", cause)）；
         * - 既保留了原始根因异常（root cause）用于排查日志，又切断了技术受检异常对上层调用链的污染！
         */
        System.out.println("--- [异常转译最佳实践实测] 对比 AccountPaymentService ---");
        AccountPaymentService service = new AccountPaymentService();

        try {
            service.payWithExceptionTranslation(1999.0);
        } catch (BusinessException e) {
            System.out.println("   [上层业务捕获] 成功捕获转译后的业务异常: " + e.getMessage());
            System.out.println("   [排查根因证据] 获取原始底层异常 Cause: " + e.getCause());
            System.out.println("   (实证结论：使用异常转译替代 throws，切断了受检异常的传播，同时完美保留了底层排查线索！)");
        }
    }

    /**
     * 第四部分：
     * try catch中的语句运行情况
     *
     * 面试核心考点：无异常、有异常并捕获、异常未捕获、finally 不执行的极端特例全分支走查。
     */
    public static void explainTryCatchExecutionFlow() {
        System.out.println("\n\n4. try catch中的语句运行情况");

        /*
         * 场景一：正常流程（无异常发生）
         * 执行路线：try 块全部语句 -> 跳过所有 catch 块 -> finally 块（若有） -> try-catch 结构之后的后续代码。
         */
        System.out.println("\n--- [分支 1] 正常运行流程（无异常） ---");
        try {
            System.out.println("   1. try 块语句执行中...");
        } catch (Exception e) {
            System.out.println("   2. catch 块（被跳过，不执行）");
        } finally {
            System.out.println("   3. finally 块必定执行！");
        }
        System.out.println("   4. 执行 try-catch 结构之后的后续语句。");

        /*
         * 场景二：发生异常且被 catch 成功捕获
         * 执行路线：try 块执行到异常行（异常行后面的 try 语句被中断不再执行！）
         *         -> 按照自上而下顺序匹配进入第一个兼容的 catch 块
         *         -> finally 块必定执行
         *         -> 继续执行后续外部代码。
         */
        System.out.println("\n--- [分支 2] 发生异常且被成功捕获 ---");
        try {
            System.out.println("   1. try 块开始执行，准备触发算术异常...");
            int error = 1 / 0; // 发生异常，后续代码被立即阻断！
            System.out.println("   (此行代码绝对不会被执行！)");
        } catch (ArithmeticException e) {
            System.out.println("   2. catch 块命中，成功捕获处理: " + e.getMessage());
        } finally {
            System.out.println("   3. finally 块必定执行！");
        }
        System.out.println("   4. 继续执行后续外部代码。");

        /*
         * 场景三：发生异常但未能被任何 catch 捕获（类型不匹配）
         * 执行路线：try 块执行到异常行立即中断 -> 遍历 catch 均不匹配
         *         -> 【依然会执行 finally 块！】
         *         -> 异常直接向外层调用栈抛出，try-catch 结构之后的后续代码绝不会执行！
         */
        System.out.println("\n--- [分支 3] 发生异常但未被 catch 匹配捕获（演示外层兜底） ---");
        try {
            runUncaughtExceptionDemo();
        } catch (NullPointerException e) {
            System.out.println("   [外层捕获兜底] 外层调用方最终截获了该未捕获异常: " + e);
        }

        /*
         * 场景四：finally 块绝对不会被执行的极端特例（面试高分彩蛋）：
         * 1. 在进入 try 块之前就已经抛出异常或返回；
         * 2. 在 try 或 catch 块中显式调用了 System.exit(0)（直接强行杀掉当前 JVM 进程）；
         * 3. 宿主线程被直接中断或强制 kill（如 kill -9、系统断电、JVM 遭遇致命 OOM 崩溃）；
         * 4. 处于无限死循环或永久死锁中，执行流永远无法到达 finally。
         */
        System.out.println("\n--- [分支 4] finally 绝对不会被执行的四种极端特例 ---");
        System.out.println("   1. 执行到 System.exit(0) 强制终止 JVM；");
        System.out.println("   2. 虚拟机遭遇致命物理断电、kill -9 或宿主 OS 崩溃；");
        System.out.println("   3. 进入 try 之前线程被中断杀死；");
        System.out.println("   4. try 块中存在死循环或无解死锁。");
    }

    /**
     * 第五部分：
     * try{return “a”} finally{return “b”}
     * 这条语句返回啥。
     *
     * 面试超高频必考题：返回值、操作数栈底层原理与吞异常致命陷阱。
     */
    public static void explainTryReturnFinallyReturn() {
        System.out.println("\n\n5. try{return “a”} finally{return “b”}");
        System.out.println("这条语句返回啥。");

        // 执行代码实测
        String result = testTryReturnAFinallyReturnB();
        System.out.println("\n   >>> 实测执行结果: 返回值是 \"" + result + "\" <<<");

        /*
         * 深入 JVM 字节码与操作数栈剖析为什么返回 "b"：
         *
         * 1. 字节码执行全流程：
         *    - 当执行到 try 块中的 return "a" 时，JVM 会先将常量 "a" 加载并暂存到局部变量表的临时存储槽中；
         *    - 但由于代码中存在 finally 块，JVM 编译器在生成字节码时，会在所有 return 或异常跳出路径前强制插入 finally 块的指令；
         *    - 程序跳转进入 finally 块，执行 return "b"，此时 JVM 将常量 "b" 加载到操作数栈顶，并执行 areturn 返回指令！
         *    - areturn 指令会立即弹出当前方法栈帧并返回栈顶数据（即 "b"），原先在 try 中暂存的 "a" 被彻底覆盖并丢弃！
         *
         * 2. 致命副作用警示（为什么阿里开发规范严禁在 finally 中写 return？）：
         *    - 如果 try 块中抛出了未捕获的严重异常（例如 throw new RuntimeException("系统崩溃")）；
         *    - 只要 finally 块中包含 return 语句，这个 return 会把 try 块中抛出的异常【完全吞掉、抹杀（Suppress）】！
         *    - 导致外层调用方不仅完全不知道内部崩溃，还会得到一个莫名其妙的 "b"，极难排查！
         */
        System.out.println("   [原理解析] JVM 执行流程：try 块中的 return \"a\" 会将 \"a\" 暂存；但 finally 中的 return \"b\" 属于真正的栈帧退出点，彻底覆盖并丢弃了 \"a\"。");

        // 致命吞异常实测
        System.out.println("\n--- [生产致命踩坑实测] finally 中写 return 吞掉异常的严重事故演示 ---");
        String swallowResult = testFinallySwallowException();
        System.out.println("   [吞异常实测] try 块明明抛出了严重异常，最终返回值却是: \"" + swallowResult + "\" (异常被彻底吞噬！)");
        System.out.println("   [阿里规约警示] 严禁在 finally 块中使用 return 语句！");
    }

    /**
     * 实测方法：try{return "a"} finally{return "b"}
     */
    @SuppressWarnings("finally")
    public static String testTryReturnAFinallyReturnB() {
        try {
            return "a";
        } finally {
            return "b"; // 编译器通常会给出警告：finally block does not complete normally
        }
    }

    /**
     * 实测方法：finally return 吞异常
     */
    @SuppressWarnings("finally")
    private static String testFinallySwallowException() {
        try {
            System.out.println("   [try 块内] 故意抛出运行时严重异常...");
            throw new RuntimeException("数据库磁盘损坏致命异常！");
        } finally {
            System.out.println("   [finally 块内] 执行了 return \"正常返回兜底\"！");
            return "正常返回兜底"; // 致命错误：这行 return 会将上面的 RuntimeException 彻底吃掉！
        }
    }

    /**
     * 辅助演示未捕获异常的执行流
     */
    private static void runUncaughtExceptionDemo() {
        try {
            System.out.println("   [内部 try] 准备抛出 NullPointerException...");
            throw new NullPointerException("未捕获的空指针测试异常");
        } catch (ArithmeticException e) {
            System.out.println("   (类型不匹配，catch 无法命中)");
        } finally {
            System.out.println("   [内部 finally] 虽然没有匹配的 catch，但 finally 依然严格执行完毕！");
        }
        System.out.println("   (异常未被内部捕获，此行代码绝不会被执行！)");
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
     * 初始化控制台字符编码，解决 Windows 环境终端输出中文乱码的问题。
     */
    private static void initConsoleEncoding() {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}
