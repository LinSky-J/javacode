package serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * 面试专题：Java 对象跨 JVM 迁移、序列化/反序列化自定义实现与底层二进制流转换全景解析。
 *
 * 本类对应面试核心题目：
 * 1. 怎么把一个对象从一个jvm转移到另一个jvm?
 * 2. 序列化和反序列化让你自己实现你会怎么做?
 * 3. 将对象转为二进制字节流具体怎么实现？
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class ObjectSerializationExplanation {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("       Java 对象跨 JVM 转移原理、自定义序列化引擎与二进制流实现        ");
        System.out.println("======================================================================");

        explainTransferObjectBetweenJvms();
        explainHowToImplementCustomSerialization();
        explainHowToConvertToBinaryByteStream();

        System.out.println("\n======================================================================");
        System.out.println("        序列化机制全景解析完毕，请细读类中源码与详细实现注释          ");
        System.out.println("======================================================================");
    }

    /**
     * 问题一：怎么把一个对象从一个jvm转移到另一个jvm?
     *
     * 面试核心考点：
     * 1. 物理本质认知：两个 JVM 是独立的操作系统进程，内存完全物理隔离，堆中内存地址/指针无法直接跨进程共享。
     * 2. 转移的三步标准闭环：【JVM A 序列化抽取状态】 -> 【跨进程/网络介质传输字节】 -> 【JVM B 反序列化在本地堆重建对象】。
     * 3. 常见传输通道（网络 TCP/RPC、消息队列 MQ、共享存储 Redis/MySQL）。
     * 4. 工业级序列化选型对比（Java 原生 vs JSON vs Protobuf/Hessian2/Kryo）。
     */
    public static void explainTransferObjectBetweenJvms() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题一：怎么把一个对象从一个jvm转移到另一个jvm?");
        System.out.println("--------------------------------------------------");

        /*
         * 【核心理论与传输闭环】：
         *
         * 1. 为什么不能直接传递对象？
         *    - JVM A 与 JVM B 运行在不同的操作系统进程甚至不同的物理机器上。
         *    - 一个 Java 对象在 JVM A 堆中由【对象头（Mark Word/Klass Pointer）】+【实例数据】组成。
         *    - 对象变量保存的是当前进程虚拟地址空间的一个【堆内存指针（如 0x7f884100）】。
         *    - 物理地址无法跨进程传递，直接传指针在 JVM B 中会引发内存越界或读到完全不相干的数据。
         *
         * 2. 跨 JVM 转移的三步标准闭环流程：
         *    - 阶段一【序列化（Serialization）在源端 JVM A】：
         *      将内存中结构复杂的对象状态（字段键值、类型描述），编码转换为脱离具体内存地址、自包含、平台无关的【连续字节流（Byte Stream）】或通用文本（JSON）。
         *    - 阶段二【传输通道（Transport Layer）】：
         *      通过 I/O 通道将二进制字节流发送至目标机器：
         *      * RPC 远程过程调用（Dubbo、gRPC、Feign 底层基于 Netty TCP Socket）。
         *      * 消息队列（Kafka、RabbitMQ、RocketMQ 异步消息投递）。
         *      * 共享持久化介质（Redis 缓存共享、MySQL 数据库 BLOB 字段、分布式文件系统）。
         *    - 阶段三【反序列化（Deserialization）在目标端 JVM B】：
         *      目标 JVM B 接收到连续字节流后，依据其中的协议头和类信息，在 JVM B 自己的堆内存中开辟全新空间，
         *      实例化出全新的对象，并将字节流中解析出的字段数据填充进去，完成对象状态的完美重建！
         *
         * 3. 跨 JVM 转移的先决条件与保障：
         *    - 契约一致性：JVM B 必须存在该类的 Class 文件（或基于 Protobuf IDL 编译的代码）。
         *    - serialVersionUID 校验：Java 原生序列化要求两端版本的 serialVersionUID 必须完全匹配，
         *      否则抛出 InvalidClassException 阻止反序列化。
         */

        System.out.println("1. [原理验证：模拟 JVM A 序列化 -> 字节流传输 -> JVM B 反序列化重建全过程]");

        // 模拟 JVM A 端生产对象
        UserAccount sourceUser = new UserAccount(10086L, "AdminUser", 99999.0, "SecretPassword123");
        UserAccount.companyName = "ANTIGRAVITY_HQ";
        System.out.println("   [JVM A 源端对象]: " + sourceUser);

        try {
            // JVM A：将对象序列化为字节数组（脱离具体内存地址）
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(sourceUser);
            oos.flush();
            byte[] networkPacket = baos.toByteArray();
            System.out.println("   [网络/传输介质]: 生成连续二进制网络数据包，大小 = " + networkPacket.length + " 字节");

            // 模拟网络传输到 JVM B...
            // JVM B：从字节数组反序列化构建堆内存对象
            ByteArrayInputStream bais = new ByteArrayInputStream(networkPacket);
            ObjectInputStream ois = new ObjectInputStream(bais);
            UserAccount destinationUser = (UserAccount) ois.readObject();

            System.out.println("   [JVM B 目标端重建对象]: " + destinationUser);
            System.out.println("   [内存同一性检验]: sourceUser == destinationUser -> " + (sourceUser == destinationUser) + " (完全不同的两个堆对象！)");
            System.out.println("   [transient 敏感字段检验]: destinationUser.getPassword() -> " + destinationUser.getPassword() + " (被安全置为 null，未传输！)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 问题二：序列化和反序列化让你自己实现你会怎么做?
     *
     * 面试核心考点：
     * 1. 架构师思维：不能仅停留在 API 调用，要具备自研轻量级 RPC 序列化框架的顶层设计能力。
     * 2. 协议头设计（Wire Protocol）：魔数（Magic Number）、协议版本（Version）、序列化算法标识、数据包长度。
     * 3. 元数据与字段编码（TLV 格式：Tag-Length-Value 或 Schema 映射）。
     * 4. 复杂场景处理（循环引用检测防栈溢出、反射获取私有字段、类版本兼容、无需无参构造函数的堆分配黑科技）。
     */
    public static void explainHowToImplementCustomSerialization() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题二：序列化和反序列化让你自己实现你会怎么做?");
        System.out.println("--------------------------------------------------");

        /*
         * 【自研序列化引擎（如 Mini-Protobuf / Mini-Kryo）顶层设计指南】：
         *
         * 1. 制定二进制通信协议帧格式（Wire Format Protocol）：
         *    +-------------------+--------------------+------------------+-------------------+
         *    | 1. 魔数 (4 Byte)  | 2. 协议版本 (2 Byte)| 3. 序列化器类型(1B)| 4. 类元信息长度+类名|
         *    +-------------------+--------------------+------------------+-------------------+
         *    | 5. 数据载荷长度   | 6. 对象字段键值对 (TLV: Tag-Length-Value 格式存储)        |
         *    +-------------------+-----------------------------------------------------------+
         *    - 魔数：快速拦截非法恶意请求，防止非法字节消耗解析资源。
         *    - 版本号：支持未来协议向后兼容平滑升级。
         *
         * 2. 序列化实现逻辑（Serialize）：
         *    - 反射提取元数据：通过 clazz.getDeclaredFields() 获取所有属性，过滤掉 static（类变量）和 transient 字段。
         *    - 类型映射与编码：为常见类型（Int, Long, Double, String, List）定义类型编号（Type Tag）。
         *    - 循环引用防护机制（Cyclic Reference）：维护一个 IdentityHashMap<Object, Integer> 记录已写入对象的 handle 序号，
         *      若遇到 A->B->A 循环引用，直接写入引用 handle ID，彻底防止递归引发 StackOverflowError。
         *
         * 3. 反序列化实现逻辑（Deserialize）：
         *    - 校验魔数与协议版本。
         *    - 反射加载类：Class.forName(className)。
         *    - 对象实例化黑科技：
         *      * 优先调用类的无参构造器：clazz.getDeclaredConstructor().newInstance()。
         *      * 若该类没有无参构造函数：使用 JVM 底层 sun.misc.Unsafe.allocateInstance(clazz) 或 Objenesis 库，
         *        直接在堆内存开辟空间分配实例，绕过构造函数的限制（Kryo 和各大 RPC 框架的底层标准方案）！
         *    - 字段注入：通过 field.setAccessible(true) 将解码还原的数值赋给对象对应属性。
         */

        System.out.println("1. [自研序列化器实战：使用 MiniBinarySerializer 手动打包与解包]");
        UserAccount originalAccount = new UserAccount(88888L, "LinSky_Architect", 1314.52, "PassWord@2026");

        try {
            // 触发自定义手写的二进制序列化
            byte[] customBytes = MiniBinarySerializer.serialize(originalAccount);
            System.out.println("   [自研二进制流大小]: " + customBytes.length + " 字节 (对比 Java 原生数百字节，极其紧凑！)");
            System.out.println("   [自研协议十六进制查看]:\n      " + MiniBinarySerializer.toHexString(customBytes));

            // 触发自定义反序列化
            UserAccount restoredAccount = MiniBinarySerializer.deserialize(customBytes);
            System.out.println("   [自研反序列化重建实例]: " + restoredAccount);
            System.out.println("   [校验字段精度还原]: ID=" + restoredAccount.getId() + ", 余额=" + restoredAccount.getBalance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 问题三：将对象转为二进制字节流具体怎么实现？
     *
     * 面试核心考点：
     * 1. 掌握底层 I/O 流与内存缓冲区的组合：ByteArrayOutputStream + DataOutputStream / ObjectOutputStream。
     * 2. 理解基本数据类型到底层字节（Byte）的拆解原理（大端序 Big-Endian、位移运算位运算、UTF-8 字符编码）。
     * 3. NIO 的高性能替代方案：ByteBuffer（allocateDirect 直接内存操作）与零拷贝传输。
     */
    public static void explainHowToConvertToBinaryByteStream() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("问题三：将对象转为二进制字节流具体怎么实现？");
        System.out.println("--------------------------------------------------");

        /*
         * 【将对象转为二进制字节流的具体落地实现方式】：
         *
         * 方式一：Java 标准 I/O 流包装（ObjectOutputStream -> ByteArrayOutputStream）
         * - 步骤：
         *   1. 创建内存字节输出流 ByteArrayOutputStream baos = new ByteArrayOutputStream();
         *   2. 包装为对象输出流 ObjectOutputStream oos = new ObjectOutputStream(baos);
         *   3. 调用 oos.writeObject(obj);
         *   4. 获取最终的字节数组 byte[] data = baos.toByteArray();
         * - 机制：ObjectOutputStream 会自动写入 STREAM_MAGIC（0xACED）、STREAM_VERSION（5）、
         *   类名长度与类签名，再逐个字段按大端序（Big-Endian）写入二进制。
         *
         * 方式二：纯手动位运算底层字节编码（深入到硬件与位运算本质）
         * - 基本类型转换字节规则：
         *   * int（4 字节）：通过 >>> 无符号右移将其切分为 4 个 byte：
         *     bytes[0] = (byte)((val >>> 24) & 0xFF);
         *     bytes[1] = (byte)((val >>> 16) & 0xFF);
         *     bytes[2] = (byte)((val >>> 8)  & 0xFF);
         *     bytes[3] = (byte)(val & 0xFF);
         *   * long（8 字节）：右移 56, 48, 40, 32, 24, 16, 8, 0 位。
         *   * String：先写入 2 字节或 4 字节的长度 len，紧接着写入 string.getBytes(StandardCharsets.UTF_8)。
         *   * double：先通过 Double.doubleToRawLongBits(val) 转成 64 位整型 long，再按 long 的 8 字节写入。
         */

        System.out.println("1. [底层位运算演示：手动将一个 int 整数转换为 4 字节二进制数组]");
        int number = 0x12345678;
        byte[] manualBytes = new byte[4];
        manualBytes[0] = (byte) ((number >>> 24) & 0xFF);
        manualBytes[1] = (byte) ((number >>> 16) & 0xFF);
        manualBytes[2] = (byte) ((number >>> 8) & 0xFF);
        manualBytes[3] = (byte) (number & 0xFF);

        System.out.printf("   原始整数: 0x%X\n", number);
        System.out.println("   按大端序位移拆解后的 4 字节数组: " + Arrays.toString(manualBytes));
        System.out.println("   对应十六进制表示: " + MiniBinarySerializer.toHexString(manualBytes));

        // 逆向位运算还原
        int restoredNumber = ((manualBytes[0] & 0xFF) << 24) |
                ((manualBytes[1] & 0xFF) << 16) |
                ((manualBytes[2] & 0xFF) << 8) |
                (manualBytes[3] & 0xFF);
        System.out.printf("   通过逆向按位或与左移还原整数: 0x%X (精确无误！)\n", restoredNumber);
    }
}
