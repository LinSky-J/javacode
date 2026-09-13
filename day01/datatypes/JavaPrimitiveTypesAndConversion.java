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
        System.out.println("1. 八种基本数据类型代码实测与全景属性：");

        /*
         * 1. 整数类型声明与初始化（4 种）：
         */
        byte byteVal = 100;
        short shortVal = 10000;
        int intVal = 100000;
        long longVal = 10000000000L; // 超出 int 范围必须显式加 'L' 后缀

        /*
         * 2. 浮点类型声明与初始化（2 种）：
         */
        float floatVal = 3.14F; // 浮点字面量默认是 double，赋值给 float 必须显式加 'F' 后缀
        double doubleVal = 3.1415926535; // 浮点默认类型

        /*
         * 3. 字符类型声明与初始化（1 种）：
         */
        char charVal = 'A'; // ASCII 字符
        char chineseChar = '中'; // Unicode 中文字符

        /*
         * 4. 布尔类型声明与初始化（1 种）：
         */
        boolean boolVal = true;

        // 格式化输出 8 种类型的名称、位数、字节数、极值范围与实际取值
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.printf("%-8s %-10s %-10s %-25s %-25s %s%n", "类型", "占用位数", "占用字节", "最小值", "最大值", "代码示例值");
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.printf("%-10s %-12d %-10d %-27d %-27d %d%n", "byte", Byte.SIZE, Byte.BYTES, Byte.MIN_VALUE, Byte.MAX_VALUE, byteVal);
        System.out.printf("%-10s %-12d %-10d %-27d %-27d %d%n", "short", Short.SIZE, Short.BYTES, Short.MIN_VALUE, Short.MAX_VALUE, shortVal);
        System.out.printf("%-10s %-12d %-10d %-27d %-27d %d%n", "int", Integer.SIZE, Integer.BYTES, Integer.MIN_VALUE, Integer.MAX_VALUE, intVal);
        System.out.printf("%-10s %-12d %-10d %-27d %-27d %d%n", "long", Long.SIZE, Long.BYTES, Long.MIN_VALUE, Long.MAX_VALUE, longVal);
        System.out.printf("%-10s %-12d %-10d %-27e %-27e %f%n", "float", Float.SIZE, Float.BYTES, Float.MIN_VALUE, Float.MAX_VALUE, floatVal);
        System.out.printf("%-10s %-12d %-10d %-27e %-27e %f%n", "double", Double.SIZE, Double.BYTES, Double.MIN_VALUE, Double.MAX_VALUE, doubleVal);
        System.out.printf("%-10s %-12d %-10d %-27d %-27d '%c'(Unicode:%d)%n", "char", Character.SIZE, Character.BYTES, (int) Character.MIN_VALUE, (int) Character.MAX_VALUE, chineseChar, (int) chineseChar);
        System.out.printf("%-10s %-12s %-10s %-27s %-27s %b%n", "boolean", "依赖JVM", "通常1B/4B", "false", "true", boolVal);
        System.out.println("---------------------------------------------------------------------------------------------");

        // 核心代码演示一：byte 极值越界环绕
        byte maxByte = 127;
        maxByte++;
        System.out.println("   [byte 溢出代码实测] byte 最大值 127 加 1 触发补码溢出，变为: " + maxByte);

        // 核心代码演示二：char 的字符与数字本质互转
        int charAscii = charVal;
        char nextLetter = (char) (charVal + 1);
        System.out.println("   [char 运算代码实测] char 'A' 的底层 ASCII 码为: " + charAscii + "，加 1 后的字符是: '" + nextLetter + "'");

        // 核心代码演示三：浮点数后缀规范
        System.out.println("   [字面量规范代码说明] float 必须带 F (如 " + floatVal + "F)，超出 int 范围的 long 必须带 L (如 " + longVal + "L)");
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
         *
         * 2. long 类型：
         *    - 占用：64 位（bits），即 8 字节（Bytes）。
         *    - 计算公式：64 bits / 8 = 8 Bytes。
         */
        int bitOfInt = Integer.SIZE;
        int byteOfInt = Integer.BYTES;
        int bitOfLong = Long.SIZE;
        int byteOfLong = Long.BYTES;

        System.out.println("\n2. int 和 long 位与字节代码实测获取：");
        System.out.println("   [int]  位数: " + bitOfInt + " 位, 字节数: " + byteOfInt + " 字节, 取值范围: " + Integer.MIN_VALUE + " ~ " + Integer.MAX_VALUE);
        System.out.println("   [long] 位数: " + bitOfLong + " 位, 字节数: " + byteOfLong + " 字节, 取值范围: " + Long.MIN_VALUE + " ~ " + Long.MAX_VALUE);
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
        System.out.println("4. 数据类型五大转换方式代码实测：");

        // 方式一：自动类型转换（小范围 -> 大范围，安全无损）
        byte b = 10;
        int i = b; // 自动将 1 字节扩展为 4 字节
        double d = i; // 自动将整型转换为浮点型
        System.out.println("   方式一 [自动隐式转换] byte(10) -> int(" + i + ") -> double(" + d + ")");

        // 方式二：强制类型转换（大范围 -> 小范围，可能发生截断）
        double price = 99.99;
        int intPrice = (int) price; // 强转直接丢弃小数部分
        System.out.println("   方式二 [强制显式转换] double(99.99) -> (int) price -> " + intPrice + " (小数截断)");

        // 方式三：包装类装箱与拆箱
        Integer boxed = Integer.valueOf(66); // 装箱
        int unboxed = boxed.intValue(); // 拆箱
        System.out.println("   方式三 [包装类装箱拆箱] int 66 -> Integer.valueOf() -> intValue() -> " + unboxed);

        // 方式四：字符串与数值互相转换
        String strVal = String.valueOf(12345);
        int parsedInt = Integer.parseInt(strVal);
        System.out.println("   方式四 [字符串数值互转] 12345 -> String(\"" + strVal + "\") -> Integer.parseInt() -> " + parsedInt);

        // 方式五：多态引用类型向上与向下转型
        Object polyObj = "Java 多态转换";
        if (polyObj instanceof String) {
            String strObj = (String) polyObj; // 向下转型
            System.out.println("   方式五 [多态向上向下转型] Object 引用 -> (String) 强转向下转型 -> \"" + strObj + "\"");
        }
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
