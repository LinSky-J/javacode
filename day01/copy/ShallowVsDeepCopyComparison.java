package copy;


/**
 * 面试专题：深拷贝和浅拷贝问题深度剖析与三大实现方案实战。
 *
 * 本类对应面试核心题目：
 * 1. 深拷贝和浅拷贝的区别?
 * 2. 实现深拷贝的三种方法是什么?
 *
 * 核心考点涵盖：
 * 1. 浅拷贝（Shallow Copy）的核心机制与生产隐患（引用字段地址共享导致的状态污染）。
 * 2. 深拷贝（Deep Copy）的核心机制（对象图递归独立复制，物理内存彻底隔离）。
 * 3. 实现深拷贝的三种经典方案：
 *    - 方案一：重写 clone() 方法并逐层级联克隆引用属性；
 *    - 方案二：基于字节流的序列化与反序列化（Serialization）；
 *    - 方案三：拷贝构造函数（Copy Constructor）或拷贝工厂方法。
 * 4. 面试高频踩坑点：Spring/Apache 的 BeanUtils.copyProperties 是浅拷贝还是深拷贝？（答案：浅拷贝！）。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class ShallowVsDeepCopyComparison {

    public static void main(String[] args) {

        System.out.println("======================================================================");
        System.out.println("             深拷贝 vs 浅拷贝 核心机制、三大方案与内存地址实测        ");
        System.out.println("======================================================================");

        explainShallowVsDeepCopyDifference();
        explainThreeMethodsOfDeepCopy();

        System.out.println("\n======================================================================");
        System.out.println("           深拷贝与浅拷贝解析完毕，请细读类中源码与注释                ");
        System.out.println("======================================================================");
    }

    /**
     * 第一部分：
     * 深拷贝和浅拷贝的区别?
     *
     * 面试核心考点：基本类型 vs 引用类型的拷贝行为，新旧对象内存共享与隔离状态。
     */
    public static void explainShallowVsDeepCopyDifference() {
        System.out.println("1. 深拷贝和浅拷贝的区别?");

        /*
         * -------------------------------------------------------------------------------------------------
         * 比较维度         浅拷贝（Shallow Copy）                      深拷贝（Deep Copy）
         * -------------------------------------------------------------------------------------------------
         * 基本数据类型     直接复制变量的具体值（八种基本类型数据安全）    直接复制变量的具体值（数据完全独立）
         * 引用数据类型     【仅复制对象的引用地址（指针）】               【在堆中开辟全新内存，递归克隆引用对象】
         * 内部对象共享     新老对象的引用成员变量【指向堆中同一个对象】    新老对象的引用成员变量【指向完全不同的新对象】
         * 相互影响程度     修改新对象内部属性，【老对象会被连带修改破坏】  新老对象【彻底解耦，互不干扰，完全隔离】
         * 实现复杂度与性能 默认 Object.clone() 即浅拷贝，速度快开销小     实现较复杂，涉及递归克隆或流序列化，开销稍大
         * -------------------------------------------------------------------------------------------------
         */

        System.out.println("\n--- [浅拷贝现场实测与状态污染事故重现] ---");
        Address originalAddr = new Address("北京市", "海淀区中关村南大街");
        Person originalPerson = new Person("张三", 25, originalAddr);
        System.out.println("   [初始源对象] " + originalPerson);

        // 1. 执行浅拷贝
        Person shallowCopyPerson = originalPerson.shallowClone();
        shallowCopyPerson.setName("张三副本(浅拷贝)"); // 修改基本类型/不可变String
        System.out.println("   [浅拷贝副本] " + shallowCopyPerson);

        // 比对内部 Address 对象的物理地址
        boolean isAddressSame = (originalPerson.getAddress() == shallowCopyPerson.getAddress());
        System.out.println("   [内存地址比对] original.address 与 shallow.address 是否指向同一内存: " + isAddressSame);

        // 2. 灾难重现：修改浅拷贝对象的内部城市属性
        System.out.println("\n   >>> 正在通过浅拷贝副本修改地址: 将城市改为 '上海市浦东新区' <<<");
        shallowCopyPerson.getAddress().setCity("上海市浦东新区");

        // 3. 观察源对象受损情况
        System.out.println("   [修改后副本] " + shallowCopyPerson);
        System.out.println("   [修改后源对象] " + originalPerson);
        System.out.println("   [实测警示] 源对象中张三的地址被悄悄篡改为了: " + originalPerson.getAddress().getCity()
                + " (这是生产环境严重的并发/数据污染 Bug！)");
    }

    /**
     * 第二部分：
     * 实现深拷贝的三种方法是什么?
     *
     * 面试高频考点：方案一重写 clone() 级联、方案二序列化、方案三拷贝构造函数。
     */
    public static void explainThreeMethodsOfDeepCopy() {
        System.out.println("\n\n2. 实现深拷贝的三种方法是什么?");

        // 重新初始化干净的源对象
        Address sourceAddr = new Address("浙江省", "杭州市滨江区网商路");
        Person sourcePerson = new Person("李四", 28, sourceAddr);
        System.out.println("   [基准测试源对象] " + sourcePerson);

        /*
         * 方法一：重写 clone() 方法（层层级联 clone）
         * 1. 机制：
         *    - 宿主类与所有引用的子类全部实现 Cloneable 接口。
         *    - 宿主类重写 clone() 方法，先通过 super.clone() 拷贝外层对象，
         *      然后显式对内部引用对象递归调用 clone() 方法（如 cloned.address = this.address.clone();）。
         * 2. 优缺点：
         *    - 优点：性能极高，属于 JVM 原生内存块拷贝（Direct Native Copy）。
         *    - 缺点：代码侵入性极高！如果对象嵌套 5 层，必须 5 个类都实现 Cloneable 并逐层手工编写 clone 级联代码，
         *      一旦漏写某一个引用字段，该字段就退化成浅拷贝！
         */
        System.out.println("\n--- [方法一实测] 重写 clone() 方法（逐层级联克隆） ---");
        Person deepPerson1 = sourcePerson.deepCloneByCascade();
        deepPerson1.setName("李四-级联克隆版");
        deepPerson1.getAddress().setCity("广州市天河区"); // 修改副本内部地址

        System.out.println("   [级联克隆副本] " + deepPerson1);
        System.out.println("   [检查源对象]   " + sourcePerson);
        System.out.println("   [地址独立性验证] 引用对象是否完全隔离: " + (sourcePerson.getAddress() != deepPerson1.getAddress()));
        System.out.println("   [结论] 源对象依然保持为: " + sourcePerson.getAddress().getCity() + "，丝毫未受影响！");

        /*
         * 方法二：序列化与反序列化（Serialization & Deserialization）
         * 1. 机制：
         *    - 将源对象通过 ObjectOutputStream 写入 ByteArrayOutputStream 内存字节流；
         *    - 再通过 ObjectInputStream 从字节流中重新反序列化组装为一个全新对象。
         * 2. 优缺点：
         *    - 优点：【彻底深拷贝的最佳实践】！无论对象嵌套多少层（例如包含 List、Map、自定义多层嵌套），
         *      序列化流会自动遍历整张对象引用图（Object Graph），全自动完成真正的物理深拷贝，完全不需要手工一层层写 clone。
         *    - 缺点：
         *      1. 性能相对较慢（涉及 CPU 编解码与字节流构建）；
         *      2. 涉及的所有对象类必须实现 java.io.Serializable 接口；
         *      3. transient 修饰的瞬态字段不会被序列化，反序列化后为默认零值（null/0）。
         *      （注：工业界也常用 JSON 序列化反序列化库如 Jackson/Fastjson 实现相同效果）。
         */
        System.out.println("\n--- [方法二实测] 序列化与反序列化（通用内存字节流深拷贝） ---");
        Person deepPerson2 = DeepCopyUtils.deepCopyBySerialization(sourcePerson);
        deepPerson2.setName("李四-序列化克隆版");
        deepPerson2.getAddress().setCity("深圳市南山区高新园"); // 修改副本内部地址

        System.out.println("   [序列化克隆副本] " + deepPerson2);
        System.out.println("   [检查源对象]     " + sourcePerson);
        System.out.println("   [地址独立性验证] 引用对象是否完全隔离: " + (sourcePerson.getAddress() != deepPerson2.getAddress()));
        System.out.println("   [结论] 源对象依然保持为: " + sourcePerson.getAddress().getCity() + "，彻底深度物理隔离！");

        /*
         * 方法三：拷贝构造函数（Copy Constructor）或静态拷贝工厂
         * 1. 机制：
         *    - 提供一个专门的构造函数 public Person(Person other)，
         *      在构造函数中为引用对象重新 new 一个独立实例：this.address = new Address(other.address);
         * 2. 优缺点：
         *    - 优点（《Effective Java》作者 Joshua Bloch 极其推荐的模式）：
         *      1. 完全抛弃了设计糟糕的 Cloneable 接口和脆弱的 Object.clone() 机制；
         *      2. 不存在任何复杂的向下转型（Downcasting），强类型编译期安全；
         *      3. 不强制要求实现 Serializable 接口；
         *      4. 能够非常优雅地处理 final 修饰的成员变量！
         *    - 缺点：同样需要程序员手动在构造函数中逐个字段编码深拷贝逻辑。
         */
        System.out.println("\n--- [方法三实测] 拷贝构造函数（Copy Constructor 推荐模式） ---");
        Person deepPerson3 = new Person(sourcePerson);
        deepPerson3.setName("李四-拷贝构造版");
        deepPerson3.getAddress().setCity("成都市高新区天府三街"); // 修改副本内部地址

        System.out.println("   [拷贝构造副本] " + deepPerson3);
        System.out.println("   [检查源对象]   " + sourcePerson);
        System.out.println("   [地址独立性验证] 引用对象是否完全隔离: " + (sourcePerson.getAddress() != deepPerson3.getAddress()));
        System.out.println("   [结论] 源对象依然保持为: " + sourcePerson.getAddress().getCity() + "，完全安全独立！");

        /*
         * 经典面试延伸避坑点：
         * 问：Spring 的 BeanUtils.copyProperties(source, target) 或者 Apache 的 PropertyUtils 是深拷贝吗？
         * 答：【绝对不是！它们全都是浅拷贝！】
         * 底层仅仅是通过反射 getter/setter 把源对象的引用指针直接塞给目标对象，
         * 一旦修改目标对象内部的集合或嵌套实体，源对象必然产生状态污染！
         */
        System.out.println("\n--- [面试必考延伸警示] ---");
        System.out.println("   切记：Spring 和 Apache 的 BeanUtils.copyProperties() 全部都是【浅拷贝】！");
        System.out.println("   在涉及嵌套集合或引用对象的多线程/异步处理场景中，必须使用上述三种深拷贝方案进行安全防线隔离。");
    }
}
