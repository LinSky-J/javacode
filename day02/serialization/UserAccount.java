package serialization;

import java.io.Serializable;

/**
 * 待序列化的用户账户实体类。
 * 用于演示 Java 原生序列化、transient 关键字屏蔽、serialVersionUID 校验及自定义二进制编码。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class UserAccount implements Serializable {

    /**
     * 序列化版本号：确保反序列化端与序列化端的 Class 版本兼容
     */
    private static final long serialVersionUID = 1L;

    /**
     * 静态变量：属于类的元数据，存储在方法区，不属于对象实例状态，因此绝对不会被序列化
     */
    public static String companyName = "ANTIGRAVITY_TECH";

    private Long id;
    private String username;
    private Double balance;

    /**
     * transient 关键字：瞬态修饰符，显式声明该字段敏感，不参与序列化传输与持久化
     */
    private transient String password;

    public UserAccount() {
    }

    public UserAccount(Long id, String username, Double balance, String password) {
        this.id = id;
        this.username = username;
        this.balance = balance;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", balance=" + balance +
                ", password='" + password + '\'' +
                ", [static]companyName='" + companyName + '\'' +
                '}';
    }
}
