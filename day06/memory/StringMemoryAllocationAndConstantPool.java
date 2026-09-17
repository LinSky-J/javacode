package memory;

/**
 * String 存储位置演进、字符串常量池底层机制与 String s = new String("abc") 内存全景深度解析
 *
 * 涵盖面试核心题目：
 * 1. String保存在哪里呢?
 * 2. String s = new String (“abc”) 执行过程中分别对应哪些内存区域？
 */
public class StringMemoryAllocationAndConstantPool {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("        String 内存分布演进、字符串常量池与 new String 内存解剖        ");
        System.out.println("======================================================================");

        explainWhereIsStringSaved();
        explainNewStringMemoryRegions();
        demonstrateStringInternMechanisms();

        System.out.println("======================================================================");
        System.out.println("                 String 内存存储与常量池解析完成                      ");
        System.out.println("======================================================================");
    }

    /**
     * 问题 1：String保存在哪里呢?
     *
     * 核心回答维度：
     * 1. 普通 new 出来的 String 实例：永远在【Java 堆】中。
     * 2. 字面量与字符串常量池（StringTable）：
     *    - JDK 6 及以前：在方法区的【永久代（PermGen）】中。
     *    - JDK 7 及以后：被迁移到【Java 堆（Heap）】中。
     * 3. 内部数据载体（JDK 8 char[] -> JDK 9 byte[] + coder）。
     */
    public static void explainWhereIsStringSaved() {
        System.out.println("\n--- 1. String 到底保存在哪里？（历史演进与物理分布） ---");
        System.out.println("权威回答分为两个层面：");
        System.out.println("一、对象实例层面：");
        System.out.println("   - 只要是通过 new 关键字创建的 String 对象实例，无论在哪个 JDK 版本，都分配在【Java 堆（Heap）】中。");
        System.out.println("   - 方法中声明的局部变量名（如 String s）存储在【虚拟机栈的栈帧局部变量表】中，作为指针指向堆中的对象。");

        System.out.println("\n二、字符串常量池（String Constant Pool / StringTable）存储位置的重大演进：");
        System.out.println("   1. JDK 6 及之前：保存在方法区的【永久代（PermGen）】中！");
        System.out.println("      - 致命痛点：永久代默认大小只有几十兆（-XX:MaxPermSize），且只有在 Full GC 时才会对其进行垃圾回收。");
        System.out.println("      - 若程序中大量使用 String.intern() 动态驻留字符串，极易触发 java.lang.OutOfMemoryError: PermGen space。");
        System.out.println("   2. JDK 7 开始及以后（JDK 8 / 11 / 17 / 21）：正式迁移到【Java 堆（Heap）】中！");
        System.out.println("      - 迁移原因：堆内存空间大（可弹性扩展），且拥有年轻代 Minor GC 的高效回收机制。");
        System.out.println("      - 当驻留在 StringTable 中的字符串无任何强引用时，可以随年轻代或老年代 GC 迅速被垃圾回收器回收，大幅减少 OOM 风险。");

        System.out.println("\n三、底层存储结构的重大优化（JDK 8 到 JDK 9+）：");
        System.out.println("   - JDK 8 及之前：String 内部使用 private final char[] value; 存储，每个字符固定占用 2 个字节（16 位）。");
        System.out.println("   - JDK 9 开始采用 Compact Strings（紧凑字符串）：改为 private final byte[] value; + byte coder;。");
        System.out.println("   - 对于只包含 Latin-1（单字节编码）的英文/数字字符串，每个字符仅占 1 个字节，使堆内存占用直接减半！");
    }

    /**
     * 问题 2：String s = new String (“abc”) 执行过程中分别对应哪些内存区域？
     *
     * 对应内存区域：
     * 1. 虚拟机栈（JVM Stack）：栈帧局部变量表中的引用变量 s。
     * 2. Java 堆（Java Heap）：new 操作分配的堆内存中的 String 实例对象。
     * 3. 字符串常量池（String Constant Pool，堆中）：字面量 "abc" 对应的字符串实例对象。
     * 4. 创建的对象数量分析（1 个还是 2 个？）。
     */
    public static void explainNewStringMemoryRegions() {
        System.out.println("\n--- 2. String s = new String(\"abc\") 内存对应全景透视 ---");
        System.out.println("代码执行过程对应的三大物理/逻辑内存区域：");
        System.out.println("1. 虚拟机栈（Stack）：");
        System.out.println("   - 在当前线程虚拟机栈的当前栈帧中，局部变量表分配一个槽位（Slot），存放变量名 s。");
        System.out.println("   - s 中存储的是一个引用指针（Reference），指向堆中刚刚 new 出来的 String 实例对象的内存地址。");
        System.out.println("2. Java 堆（Heap）：");
        System.out.println("   - 执行 new 字节码指令时，JVM 在堆内存中开辟了一块空间，实例化并创建了一个全新的 String 对象。");
        System.out.println("   - 该 String 对象的底层 byte[] / char[] 数据引用指向实际的字符内容。");
        System.out.println("3. 字符串常量池（String Constant Pool，位于堆内）：");
        System.out.println("   - 在类加载阶段或执行到该语句时，JVM 会检查字符串常量池中是否存在值为 \"abc\" 的字符串对象：");
        System.out.println("     a. 若不存在：在常量池中创建一个值为 \"abc\" 的 String 对象，并驻留到 StringTable 哈希表中；");
        System.out.println("     b. 若已存在：则直接复用常量池中原有的 \"abc\" 对象。");

        System.out.println("\n[经典面试追问] 这行代码总共创建了几个对象？");
        System.out.println("   - 严谨回答：如果常量池中之前【不存在】\"abc\"，则一共创建了 2 个对象（常量池中 1 个，堆中 new 出 1 个）；");
        System.out.println("   - 如果常量池中之前【已经存在】\"abc\"，则一共只创建了 1 个对象（即堆中 new 出的那个实例）！");

        // 现场实测验证
        System.out.println("\n--- [现场实测] 引用地址与常量池比对 ---");
        String literal = "abc";
        String s = new String("abc");
        String interned = s.intern();

        System.out.println("   literal == s ?           " + (literal == s) + " (false，字面量在常量池，s 在普通堆，引用地址不同)");
        System.out.println("   literal == interned ?    " + (literal == interned) + " (true，intern() 返回常量池中的同一个对象引用)");
        System.out.println("   s == interned ?          " + (s == interned) + " (false，堆中 new 对象与常量池对象地址不同)");
        System.out.println("   literal.equals(s) ?      " + literal.equals(s) + " (true，字符序列内容完全一致)");
    }

    /**
     * 补充实战考点：String.intern() 在 JDK 6 与 JDK 7+ 的关键区别演示
     */
    public static void demonstrateStringInternMechanisms() {
        System.out.println("\n--- 3. String.intern() 底层演进（JDK 6 vs JDK 7+ 引用常驻） ---");
        System.out.println("核心原理对比：");
        System.out.println("   - JDK 6：常量池在永久代。调用 s.intern() 时，若常量池无此字符串，会在永久代【复制一份全新的对象】并返回永久代地址。");
        System.out.println("   - JDK 7+：常量池在堆中。调用 s.intern() 时，若常量池无此字符串，只需在常量池记录【堆中该对象的引用地址（指针）】，无需重复拷贝！");

        // 拼接字符串不会自动将组合字面量放入常量池
        String s1 = new StringBuilder("java").append("interview").toString();
        // 此时常量池中此前没有 "javainterview"
        String s2 = s1.intern();
        System.out.println("   s1 == s2 ? " + (s1 == s2) + " (JDK 7+ 为 true，因为常量池直接记录 s1 在堆中的引用，无重复拷贝)");

        String s3 = new StringBuilder("ja").append("va").toString();
        String s4 = s3.intern();
        System.out.println("   s3 == s4 ? " + (s3 == s4) + " (通常为 false，因为 \"java\" 关键字在 JVM 启动初始化类加载时早已被放入常量池)");
    }
}
