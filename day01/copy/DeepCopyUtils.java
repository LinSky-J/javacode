package copy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 深度克隆工具类：演示实现深拷贝的方法二【序列化与反序列化（Serialization）】。
 *
 * 核心考点：
 * 1. 原理：
 *    将对象图写入字节流（序列化），再从字节流中重新读取组装为一个全新的对象（反序列化）。
 *    由于字节流重建对象时必须重新分配堆内存，因此能天然实现【彻底的、递归的深拷贝】，
 *    无论对象层级嵌套多么深，都能自动递归拷贝完成！
 * 2. 优点：
 *    - 无论嵌套多少层引用对象、包含多少集合容器，无需编写繁重的级联代码，全自动深度克隆！
 * 3. 限制与注意事项：
 *    - 涉及的所有嵌套对象类都必须实现 java.io.Serializable 接口，否则会抛出 NotSerializableException。
 *    - transient 修饰的瞬态字段不会被序列化（反序列化后变为默认零值 null/0）。
 *    - 性能开销相对较高（涉及流的读写与反射开销）。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class DeepCopyUtils {

    /**
     * 基于 Java 原生内存序列化流实现的泛型深度拷贝工具方法
     *
     * @param <T> 目标对象泛型（必须实现 Serializable 接口）
     * @param originalObject 源对象
     * @return 深度克隆后的全新独立对象
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepCopyBySerialization(T originalObject) {
        if (originalObject == null) {
            return null;
        }

        try {
            // 1. 序列化：将内存对象写入字节数组输出流
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
                out.writeObject(originalObject);
                out.flush();
            }

            // 2. 反序列化：从字节数组输入流中重建一个全新的对象实例
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            try (ObjectInputStream in = new ObjectInputStream(byteIn)) {
                return (T) in.readObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("序列化深拷贝失败: " + e.getMessage(), e);
        }
    }
}
