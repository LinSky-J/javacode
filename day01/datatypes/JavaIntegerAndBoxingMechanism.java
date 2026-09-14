package datatypes;

/**
 * 面试专题：Java 包装类、自动装箱拆箱、Integer 与 int 全方位对比及缓存机制深度解析。
 *
 * 本类对应面试核心题目：
 * 1. 装箱和拆箱是什么?
 * 2. Java为什么要有Integer?
 * 3. Integer相比int有什么优点?
 * 4. 那为什么还要保留int类型?
 * 5. 说一下 integer的缓存
 *
 * 设计目标：
 * 从底层字节码指令（valueOf 与 intValue）、64位对象头内存结构、
 * CPU 缓存局部性开销，到 IntegerCache 享元设计模式与阿里开发规约，建立深入通透的掌握。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaIntegerAndBoxingMechanism {

    public static void main(String[] args) {

        System.out.println("======================================================================");
        System.out.println("      Java 包装类、装箱拆箱、Integer vs int 与缓存机制深度解析报告      ");
        System.out.println("======================================================================");

        explainAutoboxingAndUnboxing();
        explainWhyJavaNeedsIntegerAndAdvantages();
        explainWhyRetainIntPrimitiveType();
        explainIntegerCacheMechanism();

        System.out.println("\n======================================================================");
        System.out.println("             包装类与装箱机制解析完毕，请细读类中源码与注释           ");
        System.out.println("======================================================================");
    }

    /**
     * 第一部分：装箱和拆箱是什么?
     *
     * 面试核心考点：语法糖本质、底层调用的方法，以及生产中致命的自动拆箱 NPE。
     */
    public static void explainAutoboxingAndUnboxing() {
        System.out.println("1. 装箱与拆箱机制解析：");

        /*
         * 1. 什么是装箱（Boxing）？
         *    - 定义：将【基本数据类型】转换为对应的【包装类引用对象】（如 int -> Integer）。
         *    - 底层本质：编译器在字节码层面自动调用 Integer.valueOf(int) 方法。
         */
        int primitiveInt = 42;
        Integer boxedInteger = primitiveInt; // 自动装箱，底层等价于 Integer.valueOf(primitiveInt);

        /*
         * 2. 什么是拆箱（Unboxing）？
         *    - 定义：将【包装类引用对象】转换为对应的【基本数据类型】（如 Integer -> int）。
         *    - 底层本质：编译器在字节码层面自动调用 integerObject.intValue() 方法。
         */
        Integer numberObj = Integer.valueOf(88);
        int unboxedInt = numberObj; // 自动拆箱，底层等价于 numberObj.intValue();

        System.out.println("   [装箱演示] 基本类型 42 自动装箱为 Integer 对象: " + boxedInteger);
        System.out.println("   [拆箱演示] Integer 对象 88 自动拆箱为基本类型 int: " + unboxedInt);

        /*
         * 3. 生产环境中自动拆箱的【致命地雷】：NullPointerException（NPE）
         *    - 当包装类对象为 null 时，如果进行数学计算、比较或赋值给基本类型，
         *      由于底层调用 null.intValue()，会直接在运行时抛出空指针异常！
         */
        System.out.println("   [生产避坑提醒]：当 Integer count = null 时，若执行 int total = count，");
        System.out.println("   底层调用 null.intValue() 将直接引发灾难性的 NullPointerException！");
    }

    /**
     * 第二部分：Java为什么要有Integer? 以及 Integer相比int有什么优点?
     *
     * 面试核心考点：泛型支持、反射框架兼容、表达 null 空值语义与丰富工具方法。
     */
    public static void explainWhyJavaNeedsIntegerAndAdvantages() {
        System.out.println("\n2. Java 为什么需要 Integer 以及相比 int 的核心优点：");

        /*
         * 优点一：支持泛型容器（Java 集合框架只接受对象）
         *   - Java 的泛型是基于类型擦除的伪泛型，类型参数上限必须是 Object。
         *   - 你可以写 List<Integer>、Map<String, Integer>，但绝对无法写 List<int>！
         *   - 没有 Integer，Java 的整个集合类库（List/Set/Map）将无法存放整数。
         */

        /*
         * 优点二：能够表达“空状态 / 未填写 / 不存在（null）”语义
         *   - 在企业级业务开发和数据库交互中，这是一个决定性优势：
         *   - 场景：用户表中的“高考成绩”或“推荐人ID”字段。
         *     * 如果用 int：默认值为 0，无法区分用户是“真的考了 0 分”还是“根本未参加考试/未填写”！
         *     * 如果用 Integer：未参加考试存为 null，考了 0 分存为 0，语义精准清晰。
         *   - 阿里巴巴 Java 开发手册规定：POJO / 数据实体类中的属性必须使用包装数据类型，严禁使用基本类型。
         */

        /*
         * 优点三：反射与企业级框架的通用处理
         *   - Spring MVC 参数绑定、MyBatis 结果映射、Jackson / Fastjson JSON 序列化反序列化，
         *     底层都是基于 Java 反射机制操作 Object 实例。包装类能无缝融入所有企业级框架。
         */

        /*
         * 优点四：内置极其丰富的常用工具方法与常量
         *   - 常量：Integer.MAX_VALUE (2147483647), Integer.MIN_VALUE (-2147483648)
         *   - 转换：Integer.parseInt("123"), Integer.toHexString(255), Integer.toBinaryString(10)
         *   - 比较：Integer.compare(a, b)
         */
        System.out.println("   优点一：支持泛型集合（可写 List<Integer>，不可写 List<int>）。");
        System.out.println("   优点二：能够表达 null 空值语义（精准区分'0值'与'未填写/不存在'）。");
        System.out.println("   优点三：无缝兼容 Spring/MyBatis/JSON 序列化等反射框架体系。");
        System.out.println("   优点四：内置进制转换、字符串解析（parseInt）与极限值常量。");
    }

    /**
     * 第三部分：那为什么还要保留int类型?
     *
     * 面试拔高亮点：从 64 位对象头内存开销、CPU 缓存局部性（Cache Line）、GC 压力三个底层维度秒杀面试官。
     */
    public static void explainWhyRetainIntPrimitiveType() {
        System.out.println("\n3. 既然 Integer 这么好，为什么 Java 还要保留 int 基本类型？");

        /*
         * 核心理由：【极致的内存空间节省】与【极其卓越的 CPU 运行性能】！
         *
         * 维度一：内存占用开销对比（4 字节 vs 16~24 字节，相差 4~6 倍！）
         * 1. 基本类型 int：
         *    - 纯粹占用 4 个字节，在虚拟机栈的局部变量表或连续数组中紧凑存放，零任何附加开销。
         * 2. 包装类 Integer：
         *    - 作为一个完整的堆内存 Java 对象，在 64 位 HotSpot 开启指针压缩（-XX:+UseCompressedOops）下：
         *      * Mark Word（对象标记头）：8 字节
         *      * Klass Pointer（类型指针）：4 字节
         *      * int 数据字段：4 字节
         *      * 对齐填充：0 字节（合计 16 字节）
         *      * 栈上的引用指针变量：4 字节
         *    - 存储同一个整数，Integer 总计要消耗 20 个字节，是 int 的整整 5 倍！
         *    - 如果有一个 1000 万个整数的数组，int[] 仅占约 40MB 内存，而 Integer[] 将直接吞噬近 200MB 内存！
         *
         * 维度二：CPU 缓存命中率（Cache Line）与访问性能
         * 1. int[] 数组在物理内存中是绝对连续存储的一整块内存：
         *    - CPU 能够利用预取技术（Prefetching）将数据成行（Cache Line，通常 64 字节）载入 L1/L2 极速缓存，
         *      连续遍历速度快到极致，并支持 SIMD 向量化指令。
         * 2. Integer[] 数组本质上是指针数组：
         *    - 数组里存的全是散落在堆内存各处的对象地址指针，访问时需要“二次寻址（指针解引用）”，
         *      极易引发频繁的 CPU Cache Miss，性能大幅衰退。
         *
         * 维度三：垃圾回收器（GC）压力
         * 1. int 变量作为局部变量随栈帧出栈即销毁，作为类成员随所属宿主对象一并回收，无需 GC 单独追踪。
         * 2. 如果大量进行数学计算都使用 Integer，会在堆中产生海量的临时碎片对象，急剧加重 Minor GC 负担。
         */
        System.out.println("   理由一 [内存占用]：int 仅占 4 字节；Integer 包含对象头高达 16 字节，内存相差 4~5 倍！");
        System.out.println("   理由二 [CPU 性能]：int[] 内存连续排列，CPU 缓存命中极高；Integer 离散存储需二次寻址。");
        System.out.println("   理由三 [GC 开销]：基本类型栈中分配无 GC 压力；海量临时包装类会导致频繁垃圾回收。");
    }

    /**
     * 第四部分：说一下 integer的缓存 (IntegerCache)
     *
     * 面试核弹级高频题目：享元模式、-128~127 范围、== 与 equals 的经典对比验证。
     */
    @SuppressWarnings("deprecation")
    public static void explainIntegerCacheMechanism() {
        System.out.println("\n4. Integer 缓存机制（IntegerCache）全景深度剖析：");

        /*
         * 1. 为什么要有 IntegerCache 缓存？
         *    - 在日常开发中，小范围整数（如 0, 1, 2, -1, 100 等）被高频重复使用。
         *    - 为了避免频繁在堆中 new 对象，Java 采用了【享元设计模式（Flyweight Pattern）】。
         *    - 在 JVM 类加载 Integer 类时，其内部静态类 IntegerCache 就预先在堆内存中创建了
         *      【-128 到 127】共 256 个 Integer 对象并放入 cache 数组中。
         *
         * 2. 缓存触发的先决条件：
         *    - 只有调用 Integer.valueOf(int)（包括自动装箱）时才会走缓存！
         *    - 如果显式调用 new Integer(100)，则强制在堆中新建独立对象，绝对不走缓存！
         */

        System.out.println("--- 经典面试代码陷阱实测 ---");

        // 场景一：数值在 [-128, 127] 范围内（自动装箱命中缓存）
        Integer a = 100;
        Integer b = 100;
        System.out.println("数值 100 [范围内]：a == b 结果为: " + (a == b) + " (命中缓存，指向同一个预创建的单例对象！)");

        // 场景二：数值超出 [-128, 127] 范围（自动装箱未命中缓存，在堆中各 new 了一个新对象）
        Integer c = 128;
        Integer d = 128;
        System.out.println("数值 128 [超出范围]：c == d 结果为: " + (c == d) + " (未命中缓存，各自 new 新对象，堆地址不同！)");
        System.out.println("数值 128 [比较内容]：c.equals(d) 结果为: " + c.equals(d) + " (equals 比较的是内部存储的数值，为 true)");

        // 场景三：显式 new Integer 强制创建新对象（绕过缓存）
        Integer e = new Integer(100);
        Integer f = new Integer(100);
        System.out.println("显式 new 出来：e == f 结果为: " + (e == f) + " (显式 new 无论数值大小必建新对象，永远为 false)");

        /*
         * 3. 缓存调优参数：
         *    - 下限固定为 -128，不可更改。
         *    - 上限默认为 127，但可以通过 JVM 启动参数修改：-XX:AutoBoxCacheMax=<size>
         *      例如配置 -XX:AutoBoxCacheMax=1000，则 -128 到 1000 均会被缓存。
         *
         * 4. 其他包装类的缓存情况：
         *    - Byte, Short, Long：同样缓存 [-128, 127]。
         *    - Character：缓存 [0, 127]。
         *    - Boolean：缓存 TRUE 与 FALSE 两个单例。
         *    - Float, Double：【没有缓存】！因为在任意区间内，浮点数都是无限稠密的，无法离散预创建。
         */
        System.out.println("\n【阿里巴巴开发规范金科玉律】：");
        System.out.println("所有整型包装类对象之间值的比较，全部使用 equals 方法比较，严禁直接使用 '==' 比较！");
    }
}
