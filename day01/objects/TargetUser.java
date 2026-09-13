package objects;

import java.io.Serializable;

/**
 * 目标用户实体类：专门用于演示对象的各种创建方式（包括私有构造、clone、反序列化）以及私有成员读取。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class TargetUser implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 私有成员变量
     */
    private String username;
    private int age;
    private String secretSecurityCode;

    /**
     * 公开普通构造函数（用于 new 方式）
     *
     * @param username 用户名
     * @param age 年龄
     */
    public TargetUser(String username, int age) {
        this.username = username;
        this.age = age;
        this.secretSecurityCode = "PUBLIC-DEFAULT-" + System.currentTimeMillis();
        System.out.println("   [公共构造函数执行] 成功构建对象: " + username + " (年龄: " + age + ")");
    }

    /**
     * 私有构造函数（专门用于测试反射获取私有构造器并实例化）
     *
     * @param secretSecurityCode 安全审计密钥
     */
    private TargetUser(String secretSecurityCode) {
        this.username = "内部特权保密用户";
        this.age = 99;
        this.secretSecurityCode = secretSecurityCode;
        System.out.println("   [私有特权构造函数执行] 成功通过私有构造函数创建特权对象，密钥: " + secretSecurityCode);
    }

    /**
     * 私有普通方法
     */
    private void privateInternalMethod() {
        System.out.println("   [私有方法触发] 内部核心机密校验通过，当前用户为: " + username);
    }

    /**
     * 实现 clone() 方法用于测试第 3 种对象创建方式（不走构造函数）
     */
    @Override
    public TargetUser clone() {
        try {
            System.out.println("   [clone() 方法执行] 底层直接进行堆内存二进制块复制（完全不调用构造函数）！");
            return (TargetUser) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("克隆不支持异常", e);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSecretSecurityCode() {
        return secretSecurityCode;
    }

    @Override
    public String toString() {
        return "TargetUser{username='" + username + "', age=" + age + ", secretSecurityCode='" + secretSecurityCode + "'}@0x"
                + Integer.toHexString(System.identityHashCode(this)).toUpperCase();
    }
}
