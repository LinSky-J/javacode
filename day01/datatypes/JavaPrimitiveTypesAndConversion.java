package datatypes;

import java.math.BigDecimal;

/**
 * 面试专题：Java 八种基本数据类型、类型转换与浮点精度深度剖析。
 *
 * 本类对应面试核心题目：
 * 1. 八种基本的数据类型
 * 2. int和long是多少位，多少字节的？
 * 3. long和int可以互转吗？
 * 4. 数据类型转换方式你知道哪些?
 * 5. 类型互转会出现什么问题吗?
 * 6. 为什么用bigDecimal 不用double?
 *
 * 设计目标：
 * 遵循企业级开发规范，从计算机底层比特位布局、IEEE 754 浮点数表示缺陷，
 * 到金融级高精度运算避坑指南，全面建立扎实严密的类型系统认知。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaPrimitiveTypesAndConversion {

    public static void main(String[] args) {
        initConsoleEncoding();

        System.out.println("======================================================================");
        System.out.println("       Java 八种基本数据类型、类型转换与浮点运算深度解析报告         ");
        System.out.println("======================================================================");

        explainEightPrimitiveTypes();
        explainIntAndLongBitsAndBytes();
        explainLongAndIntConversion();
        explainAllConversionModes();
        explainConversionPitfalls();
        explainWhyBigDecimalInsteadOfDouble();

        System.out.println("\n======================================================================");
        System.out.println("             基本数据类型与转换解析完毕，请细读类中源码与注释         ");
        System.out.println("======================================================================");
    }

    /**
     * 第一部分：八种基本的数据类型
     *
     * 面试核心考点：4 种整数型、2 种浮点型、1 种字符型、1 种布尔型。
     */
    public static void explainEightPrimitiveTypes() {
        /*
         * Java 中的 8 种基本数据类型（Primitive Data Types）：
         *
         * 1. 整数类型（4 种）：
         *    - byte：8 位（1 字节），取值范围 -128 到 127。常用于底层网络 I/O、二进制文件流传输。
         *    - short：16 位（2 字节），取值范围 -32768 到 32767。现代开发中极少直接使用。
         *    - int：32 位（4 字节），取值范围约 -21.47 亿 到 21.47 亿。Java 中整数的【默认类型】。
         *    - long：64 位（8 字节），取值范围约 -922 亿亿 到 922 亿亿。赋值时需加后缀 'L'（推荐大写）。
         *
         * 2. 浮点类型（2 种）：
         *    - float：32 位（4 字节），单精度浮点数，符合 IEEE 754 标准，赋值时需加后缀 'F' 或 'f'。
         *    - double：64 位（8 字节），双精度浮点数，Java 中浮点数的【默认类型】。
         *
         * 3. 字符类型（1 种）：
         *    - char：16 位（2 字节），采用 UTF-16 编码表示 Unicode 字符，取值范围 0 到 65535（无符号）。
         *
         * 4. 布尔类型（1 种）：
         *    - boolean：逻辑值，仅有两个取值 true 和 false。
         *      注：JVM 规范中没有明确规定其具体大小，编译后在 JVM 内部通常用 4 字节的 int 或 1 字节的 byte 存储。
         */
        System.out.println("1. 八种基本数据类型：byte(1B), short(2B), int(4B), long(8B), float(4B), double(8B), char(2B), boolean。");
    }

    /**
     * 第二部分：int和long是多少位，多少字节的？
     *
     * 面试核心考点：位（bit）与字节（Byte）的换算关系（1 字节 = 8 位）。
     */
    public static void explainIntAndLongBitsAndBytes() {
        /*
         * 1. int 类型：
         *    - 占用：32 位（bits），即 4 字节（Bytes）。
         *    - 计算公式：32 bits / 8 = 4 Bytes。
         *    - 最小值：Integer.MIN_VALUE = -2^31 (-2,147,483,648)
         *    - 最大值：Integer.MAX_VALUE = 2^31 - 1 (2,147,483,647)
         *
         * 2. long 类型：
         *    - 占用：64 位（bits），即 8 字节（Bytes）。
         *    - 计算公式：64 bits / 8 = 8 Bytes。
         *    - 最小值：Long.MIN_VALUE = -2^63 (-9,223,372,036,854,775,808)
         *    - 最大值：Long.MAX_VALUE = 2^63 - 1 (9,223,372,036,854,775,807)
         */
        System.out.println("2. 位与字节大小：int 占 32 位（4 字节）；long 占 64 位（8 字节）。");
    }

    /**
     * 第三部分：long和int可以互转吗？
     *
     * 面试核心考点：双向转换规则、自动拓宽 vs 强制截断。
     */
    public static void explainLongAndIntConversion() {
        /*
         * 结论：long 和 int 完全可以互相转换！
         *
         * 1. int 转 long（小范围转大范围）：
         *    - 属于【自动类型转换（拓宽转换 Widening Conversion）】。
         *    - 无需任何强转语法，编译器自动完成，绝对安全无数据丢失（小杯水倒入大桶）。
         */
        int smallInt = 1000;
        long autoLong = smallInt; // 自动提升为 long

        /*
         * 2. long 转 int（大范围转小范围）：
         *    - 属于【强制类型转换（缩小转换 Narrowing Conversion）】。
         *    - 必须显式添加强制类型转换符 `(int)`。
         *    - 安全隐患：如果 long 的数值超过了 int 的最大值范围（21 亿），高 32 位会被直接截断丢弃，导致数据失真甚至符号反转！
         */
        long bigLong = 2000L;
        int castInt = (int) bigLong; // 强制类型转换

        System.out.println("3. long 与 int 互转：完全可以！int 转 long 自动安全转换；long 转 int 需显式强转，存在截断风险。");
    }

    /**
     * 第四部分：数据类型转换方式你知道哪些?
     *
     * 面试核心考点：系统化归纳 Java 的所有类型转换维度。
     */
    public static void explainAllConversionModes() {
        /*
         * Java 中主流的数据类型转换方式主要有以下五种：
         *
         * 方式一：自动类型转换（隐式转换）
         *   - 触发条件：从低精度向高精度、从小范围向大范围转换。
         *   - 转换链路：byte -> short/char -> int -> long -> float -> double。
         *
         * 方式二：强制类型转换（显式转换）
         *   - 触发条件：从大范围向小范围强转。
         *   - 语法：(目标类型) 变量名。如 int x = (int) 3.14;。
         *
         * 方式三：包装类与基本类型互转（自动装箱与拆箱）
         *   - 装箱：int -> Integer（底层调用 Integer.valueOf()）。
         *   - 拆箱：Integer -> int（底层调用 integer.intValue()）。
         *
         * 方式四：字符串与基本数据类型互转
         *   - 基本类型转 String：String.valueOf(100) 或 "" + 100。
         *   - String 转基本类型：Integer.parseInt("123")、Double.parseDouble("3.14")。
         *
         * 方式五：引用类型多态转换（向上转型与向下转型）
         *   - 向上转型：Child c = new Child(); Parent p = c;（自动安全）。
         *   - 向下转型：Child c = (Child) p;（需结合 instanceof 防止 ClassCastException）。
         */
        System.out.println("4. 转换方式汇总：自动隐式转换、强制显式转换、自动装箱/拆箱、字符串/数值互转、父子类多态引用转换。");
    }

    /**
     * 第五部分：类型互转会出现什么问题吗?
     *
     * 面试核心考点：高位截断溢出、浮点小数截断、IEEE 754 精度丢失、符号位扩展。
     */
    public static void explainConversionPitfalls() {
        System.out.println("\n--- 类型互转的三大致命陷阱实测 ---");

        /*
         * 陷阱一：高位截断导致数据失真与正负颠倒（Overflow）
         * 30 亿超出了 int 的最大上限（约 21.47 亿）。
         * long 在二进制中占 64 位，强转为 int 时，高 32 位被强行削掉！
         * 剩下的低 32 位最高位（符号位）恰好成了 1，结果直接变成了负数！
         */
        long overflowLong = 3_000_000_000L;
        int ruinedInt = (int) overflowLong;
        System.out.println("陷阱一 [高位截断] long 30 亿强转 int: " + ruinedInt + " (正数直接变成负数！)");

        /*
         * 陷阱二：浮点数强转整数，小数部分被粗暴截断（不是四舍五入！）
         */
        double pi = 3.9999;
        int truncatedInt = (int) pi;
        System.out.println("陷阱二 [小数截断] double 3.9999 强转 int: " + truncatedInt + " (直接丢弃小数，并非四舍五入！)");

        /*
         * 陷阱三：long 转 float 虽是自动转换，却可能悄悄丢失低位精度！
         * 很多人误以为“自动转换绝对安全”，错！
         * long 拥有 64 位整型精度，而 float 虽然也占 32 位，但它只有 23 位的尾数有效位（约 7 位十进制有效数字）。
         * 超大 long 转 float 会发生不可逆的有效数字丢失！
         */
        long bigPreciseLong = 123456789012345678L;
        float impreciseFloat = bigPreciseLong;
        System.out.println("陷阱三 [有效位丢失] 原始 long: " + bigPreciseLong + " -> 自动转 float 后科学计数丢失低位: " + impreciseFloat);
    }

    /**
     * 第六部分：为什么用bigDecimal 不用double?
     *
     * 面试核弹级高频考点：二进制浮点数（IEEE 754）的天然缺陷与 BigDecimal 避坑指南。
     */
    public static void explainWhyBigDecimalInsteadOfDouble() {
        System.out.println("\n--- 为什么金融与电商交易必须用 BigDecimal，绝对禁用 double？---");

        /*
         * 1. 为什么 double 会算错？（IEEE 754 标准的天然宿命）
         *    - 人类使用的是【十进制】，0.1 意为 1/10。
         *    - 计算机物理底层是【二进制】，只能精确表示 2 的负数次幂（1/2=0.5, 1/4=0.25, 1/8=0.125, 1/16=0.0625...）。
         *    - 尝试用有限长度的二进制小数去凑出 0.1，就像十进制里用小数去表示 1/3（0.333333... 无限循环）一样，
         *      二进制表示 0.1 是一个【无限循环小数】！
         *    - 由于 double 只有 52 位尾数空间，超出部分只能在末尾强行截断舍入，产生微小的精度偏差。
         */
        double a = 0.1;
        double b = 0.2;
        double sum = a + b;
        System.out.println("double 经典翻车演示: 0.1 + 0.2 = " + sum);
        System.out.println("double 经典减法翻车: 1.0 - 0.9 = " + (1.0 - 0.9));
        System.out.println("判断 0.1 + 0.2 == 0.3 的结果是: " + (sum == 0.3) + " (在电商财务中会导致账目不平，直接资损！)");

        /*
         * 2. BigDecimal 是如何实现精确计算的？
         *    - BigDecimal 底层由一个无缩放的 BigInteger 整数 + 一个 32 位的整数 scale（表示小数点后位数）构成。
         *    - 比如 0.1 在 BigDecimal 内部被存储为：整数 1，scale = 1。
         *    - 完全避免了二进制截断误差，真正做到十进制高精度无损运算。
         */
        BigDecimal bd1 = new BigDecimal("0.1");
        BigDecimal bd2 = new BigDecimal("0.2");
        BigDecimal bdSum = bd1.add(bd2);
        System.out.println("BigDecimal 精确计算演示: new BigDecimal(\"0.1\").add(\"0.2\") = " + bdSum);

        /*
         * 3. 阿里巴巴 Java 开发手册【强制红线警示】：
         *    - 严禁使用 new BigDecimal(double) 构造器！
         *    - 为什么？因为 0.1 传入时已经是丢了精度的 double，new BigDecimal(0.1) 结果依然是一串长数字！
         *    - 必须使用：new BigDecimal(\"0.1\") 字符串构造器，或 BigDecimal.valueOf(0.1)！
         */
        System.out.println("致命错误写法演示: new BigDecimal(0.1) = " + new BigDecimal(0.1));
        System.out.println("标准规范正确写法: BigDecimal.valueOf(0.1) = " + BigDecimal.valueOf(0.1));
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
