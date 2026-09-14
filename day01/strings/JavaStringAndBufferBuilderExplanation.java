package strings;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 面试专题：Java String 核心常用方法大盘点，及 String、StringBuffer、StringBuilder 深度对比。
 *
 * 本类对应面试核心题目：
 * 1. java 里 string的常用方法有哪些？
 * 2. String、StringBuffer、StringBuilder的区别和联系？
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaStringAndBufferBuilderExplanation {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("   Java String 常用方法大全 与 String/StringBuffer/StringBuilder 对比 ");
        System.out.println("======================================================================");

        explainCommonStringMethods();
        explainStringVsBufferVsBuilder();

        System.out.println("\n======================================================================");
        System.out.println("         字符串体系核心知识解析完毕，请细读类中源码与详细注释         ");
        System.out.println("======================================================================");
    }

    /**
     * 问题一：java 里 string的常用方法有哪些？
     *
     * 面试核心考点：
     * 1. 结构化分类回答：按“查找、截取、替换转换、判空去空格、拼接格式化、比较”等维度作答。
     * 2. 区分容易混淆的方法（如 replace vs replaceAll，trim vs strip，isEmpty vs isBlank）。
     * 3. 掌握现代 Java 版本的新增实用方法（如 Java 11 的 isBlank/strip/repeat/lines）。
     */
    public static void explainCommonStringMethods() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题一：java 里 string的常用方法有哪些？");
        System.out.println("--------------------------------------------------");

        String text = "  Hello, Java Architecture Enterprise Framework!  ";

        /*
         * 维度一：字符串查找与位置获取
         * - charAt(int index): 获取指定索引处的 char 字符。
         * - indexOf(String str): 查找子串第一次出现的下标，未找到返回 -1。
         * - lastIndexOf(String str): 查找子串最后一次出现的下标。
         * - contains(CharSequence s): 判断是否包含指定子串序列。
         * - startsWith(String prefix): 判断是否以特定前缀开头。
         * - endsWith(String suffix): 判断是否以特定后缀结尾。
         */
        System.out.println("1. [查找与匹配方法]:");
        System.out.println("   text.charAt(9): '" + text.charAt(9) + "'");
        System.out.println("   text.indexOf(\"Java\"): " + text.indexOf("Java"));
        System.out.println("   text.contains(\"Enterprise\"): " + text.contains("Enterprise"));
        System.out.println("   text.trim().startsWith(\"Hello\"): " + text.trim().startsWith("Hello"));

        /*
         * 维度二：字符串截取与切分
         * - substring(int beginIndex, int endIndex): 左闭右开 [begin, end) 截取子串。
         * - split(String regex): 按照正则表达式切分成字符串数组。
         */
        System.out.println("\n2. [截取与切分方法]:");
        String trimmed = text.trim();
        System.out.println("   trimmed.substring(7, 11): \"" + trimmed.substring(7, 11) + "\"");
        String[] words = trimmed.split(" ");
        System.out.println("   trimmed.split(\" \") 切分后单词个数: " + words.length + "，数组内容: " + Arrays.toString(words));

        /*
         * 维度三：转换与替换
         * - toLowerCase() / toUpperCase(): 大小写转换。
         * - toCharArray(): 转换成底层的字符数组 char[]。
         * - getBytes(Charset charset): 按照指定编码（如 UTF-8）转换成 byte[]。
         * - replace(CharSequence target, CharSequence replacement): 字面量字串精确替换（非正则）。
         * - replaceAll(String regex, String replacement): 正则表达式批量替换。
         * - replaceFirst(String regex, String replacement): 替换首个匹配正则的子串。
         */
        System.out.println("\n3. [转换与替换方法]:");
        System.out.println("   trimmed.toLowerCase(): \"" + trimmed.toLowerCase() + "\"");
        System.out.println("   trimmed.replace(\"Java\", \"Cloud-Native\"): \"" + trimmed.replace("Java", "Cloud-Native") + "\"");
        System.out.println("   正则 replaceAll(\"\\\\s+\", \"_\"): \"" + trimmed.replaceAll("\\s+", "_") + "\"");

        /*
         * 维度四：判空与去除空白字符（包含现代 Java 11 增强方法）
         * - trim(): 传统去除头尾空白（仅能识别 ASCII 空格 <= U+0020）。
         * - strip(): Java 11 推荐方法，支持 Unicode 全角空格及复杂空白去除。
         * - isEmpty(): 判断 length() == 0。
         * - isBlank(): Java 11 推荐方法，不仅长度为 0，纯由空格构成的空白字符串也判定为 true。
         */
        System.out.println("\n4. [判空与空白处理方法（包含 Java 11 特性）]:");
        String emptyStr = "";
        String blankStr = "   \t\n  ";
        System.out.println("   emptyStr.isEmpty(): " + emptyStr.isEmpty());
        System.out.println("   blankStr.isEmpty(): " + blankStr.isEmpty() + " (长度不为0，返回 false)");
        System.out.println("   blankStr.isBlank(): " + blankStr.isBlank() + " (内容纯空白，返回 true)");

        /*
         * 维度五：拼接、格式化与重复（Java 11+ repeat）
         * - concat(String str): 末尾追加字符串。
         * - String.join(CharSequence delimiter, ...): 优雅拼接集合或可变参数。
         * - String.format(String format, Object... args): 格式化输出。
         * - repeat(int count): Java 11 重复构建字符串。
         */
        System.out.println("\n5. [拼接与格式化方法]:");
        System.out.println("   String.join(\"-\", \"2026\", \"09\", \"14\"): \"" + String.join("-", "2026", "09", "14") + "\"");
        System.out.println("   String.format(\"订单号: %s, 金额: %.2f 元\", \"ORD_1001\", 888.888): \"" +
                String.format("订单号: %s, 金额: %.2f 元", "ORD_1001", 888.888) + "\"");
        System.out.println("   \"*=\".repeat(10): \"" + "*=".repeat(10) + "\"");

        /*
         * 维度六：比较与字典序
         * - equals(Object anObject): 内容精确比较。
         * - equalsIgnoreCase(String anotherString): 忽略大小写内容比较。
         * - compareTo(String anotherString): 基于 Unicode 字典序比较（返回正、负或0）。
         */
        System.out.println("\n6. [比较方法]:");
        System.out.println("   \"java\".equalsIgnoreCase(\"JAVA\"): " + "java".equalsIgnoreCase("JAVA"));
        System.out.println("   \"apple\".compareTo(\"banana\"): " + "apple".compareTo("banana") + " (字典序在前，返回负数)");
    }

    /**
     * 问题二：String、StringBuffer、StringBuilder的区别和联系？
     *
     * 面试核心考点：
     * 1. 可变性（Immutability）的底层原理（JDK 8 char[] -> JDK 9+ byte[] Compact Strings）。
     * 2. 线程安全性（Thread-Safety）机制：为什么 StringBuffer 线程安全？（synchronized）。
     * 3. 运行性能（Performance）差异及循环拼接场景下的最佳实践。
     * 4. 三者的共同联系（CharSequence 接口、final 修饰、继承体系）。
     */
    public static void explainStringVsBufferVsBuilder() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题二：String、StringBuffer、StringBuilder的区别和联系？");
        System.out.println("--------------------------------------------------");

        /*
         * 【四大核心区别】：
         *
         * 1. 可变性（Mutability）区别：
         *    - String：不可变对象（Immutable）。
         *      底层在 JDK 8 前为 private final char value[]，JDK 9 起为 private final byte[] value 配合 coder 编码标识。
         *      所有看似修改字符串的方法（如 concat, substring, replace），底层都会返回一个全新创建的 String 对象，原对象绝不改变。
         *    - StringBuffer 和 StringBuilder：可变字符序列（Mutable）。
         *      都继承自 AbstractStringBuilder，内部维护可扩容的 byte[] value 数组。
         *      调用 append, insert, delete 等方法都是在原有内存数组上原地修改，容量不足时自动以 (oldCapacity * 2) + 2 扩容。
         *
         * 2. 线程安全性（Thread Safety）区别：
         *    - String：不可变对象天然具备绝对的线程安全性（Thread-Safe），任意多线程并发读取无需加锁。
         *    - StringBuffer：线程安全（Thread-Safe）。内部关键公共方法均使用了 synchronized 关键字加锁修饰。
         *    - StringBuilder：线程非安全（Thread-Unsafe）。内部方法没有加任何锁，多线程并发调用会导致数据覆盖或下标越界异常。
         *
         * 3. 执行性能（Performance）区别：
         *    - 在单线程大量修改/拼接字符串时：StringBuilder 性能最优（无锁开销） > StringBuffer（存在同步锁竞争开销） >> String（频繁对象创建与GC）。
         *    - 特别说明：对于单行常量拼接（如 String s = "a" + "b" + "c";），javac 编译期会做常量折叠，性能极高；
         *      但在 for 循环中反复使用 +=，每次循环都会产生新对象，严重拖慢系统吞吐，必须显式使用 StringBuilder。
         *
         * 4. 内存开销：
         *    - String 常量会进入 JVM 字符串常量池（String Pool），具有去重和内存复用优势。
         *    - StringBuffer/StringBuilder 作为堆内临时缓冲区，用完后需等待 GC 回收。
         *
         * 【三者的紧密联系】：
         * 1. 均实现了 java.lang.CharSequence 接口，都具备获取 length()、charAt() 和 subSequence() 的统一抽象能力。
         * 2. 均被 final 关键字修饰，不可被任何子类继承破坏其底层不变性或缓冲区语义。
         * 3. StringBuffer 与 StringBuilder 继承自同一个父类 AbstractStringBuilder，拥有几乎完全同构的 API 体系。
         */

        System.out.println("1. [实测 1：单线程 20000 次大批量循环拼接性能实测]");
        StringPerformanceBenchmark.benchmarkConcatenation(20000);

        System.out.println("\n2. [实测 2：多线程高并发环境下的数据安全性实测]");
        StringPerformanceBenchmark.testConcurrencySafety(10, 1000);

        System.out.println("\n3. [企业工程级技术选型建议]:");
        System.out.println("   - 操作少量字符串或只读定义常量：优先使用 String。");
        System.out.println("   - 单线程环境（绝大多数业务层 Controller/Service 内部组装 SQL、日志、报文）：强制优先使用 StringBuilder。");
        System.out.println("   - 跨多线程共享访问的全局字符缓冲区：使用 StringBuffer（或通过并发容器外层加锁）。");
    }
}
