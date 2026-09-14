package serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 自定义轻量级二进制序列化与反序列化引擎实现。
 * 用于直接回答面试官：如果让你自己实现一套序列化与反序列化，你会怎么做？
 *
 * 【自定义二进制协议结构（Protocol Frame）】：
 * +----------------+-----------------+------------------+------------------+
 * | 魔数 Magic(4B) | 协议版本 Ver(2B) | 类名长度+类名    | 对象字段有效载荷 |
 * | 0xCAFEBABE     | 0x0001          | short len + utf8 | id, name, balance|
 * +----------------+-----------------+------------------+------------------+
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class MiniBinarySerializer {

    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    private static final short PROTOCOL_VERSION = 1;

    /**
     * 将 UserAccount 对象手动转换为紧凑的二进制字节流
     *
     * @param account 待序列化对象
     * @return 连续二进制字节数组 byte[]
     */
    public static byte[] serialize(UserAccount account) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // 1. 写入协议头：魔数（用于快速过滤非法请求，防止垃圾数据消耗解析资源）
        dos.writeInt(MAGIC_NUMBER);

        // 2. 写入协议版本号（用于保证后续升级协议的向前/向后兼容性）
        dos.writeShort(PROTOCOL_VERSION);

        // 3. 写入目标类全限定名（用于反序列化端动态 Class 加载）
        byte[] classNameBytes = account.getClass().getName().getBytes(StandardCharsets.UTF_8);
        dos.writeShort(classNameBytes.length);
        dos.write(classNameBytes);

        // 4. 写入对象实际有效载荷数据（Payload，采用紧凑字节布局）
        // 4.1 写入 id（8 字节 Long）
        dos.writeLong(account.getId() != null ? account.getId() : 0L);

        // 4.2 写入 username（2 字节长度 + UTF-8 变长字节）
        String username = account.getUsername();
        if (username == null) {
            dos.writeShort(-1); // -1 表示 null
        } else {
            byte[] nameBytes = username.getBytes(StandardCharsets.UTF_8);
            dos.writeShort(nameBytes.length);
            dos.write(nameBytes);
        }

        // 4.3 写入 balance（8 字节 Double）
        dos.writeDouble(account.getBalance() != null ? account.getBalance() : 0.0);

        // 注意：敏感字段 password 属于瞬态，此处绝不向字节流中写入！

        dos.flush();
        return baos.toByteArray();
    }

    /**
     * 从二进制字节流反向解析并还原重建 UserAccount 对象
     *
     * @param bytes 二进制字节数组
     * @return 重新装配完成的全新对象实例
     */
    public static UserAccount deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        // 1. 验证魔数
        int magic = dis.readInt();
        if (magic != MAGIC_NUMBER) {
            throw new IllegalArgumentException("非法的协议数据包，魔数校验失败: 0x" + Integer.toHexString(magic));
        }

        // 2. 校验协议版本
        short version = dis.readShort();
        if (version != PROTOCOL_VERSION) {
            throw new IllegalArgumentException("不受支持的序列化协议版本: " + version);
        }

        // 3. 读取类名并动态反射加载
        short classNameLen = dis.readShort();
        byte[] classNameBytes = new byte[classNameLen];
        dis.readFully(classNameBytes);
        String className = new String(classNameBytes, StandardCharsets.UTF_8);

        Class<?> clazz = Class.forName(className);
        UserAccount account = (UserAccount) clazz.getDeclaredConstructor().newInstance();

        // 4. 按协议顺序反序列化填充字段数据
        long id = dis.readLong();
        account.setId(id);

        short nameLen = dis.readShort();
        if (nameLen != -1) {
            byte[] nameBytes = new byte[nameLen];
            dis.readFully(nameBytes);
            account.setUsername(new String(nameBytes, StandardCharsets.UTF_8));
        }

        double balance = dis.readDouble();
        account.setBalance(balance);

        return account;
    }

    /**
     * 辅助工具：将字节数组格式化为十六进制字符串便于排查与分析
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
