package copy;

import java.io.Serializable;

/**
 * 地址实体类：嵌套引用对象，用于深拷贝与浅拷贝的对比实测载体。
 *
 * 实现了 Cloneable 接口以支持 clone() 方法，
 * 实现了 Serializable 接口以支持序列化深拷贝。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class Address implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private String province;
    private String city;

    public Address(String province, String city) {
        this.province = province;
        this.city = city;
    }

    /**
     * 拷贝构造函数（深拷贝方案三的组成部分）：
     * 接收已有 Address 对象，手动分配新内存并克隆字段。
     *
     * @param other 源地址对象
     */
    public Address(Address other) {
        if (other != null) {
            this.province = other.province;
            this.city = other.city;
        }
    }

    /**
     * 重写 Object 的 clone 方法以支持级联深拷贝
     */
    @Override
    public Address clone() {
        try {
            return (Address) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("克隆异常，当前类已实现 Cloneable", e);
        }
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "Address{province='" + province + "', city='" + city + "'}@0x"
                + Integer.toHexString(System.identityHashCode(this)).toUpperCase();
    }
}
