package objects.methods;

import java.util.Objects;

/**
 * 标准实现了 equals 与 hashCode 的用户实体类。
 * 用于演示重写规范以及在 HashMap/HashSet 中的正确行为。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class UserWithEqualsAndHashCode {

    private final Long id;
    private final String name;
    private final String department;

    public UserWithEqualsAndHashCode(Long id, String name, String department) {
        this.id = id;
        this.name = name;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public boolean equals(Object o) {
        // 1. 同一性检查（内存地址相同直接返回 true）
        if (this == o) {
            return true;
        }
        // 2. 空指针与运行时类型检查（严格使用 getClass() 确保满足对称性）
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        // 3. 核心业务主键/属性值比较
        UserWithEqualsAndHashCode that = (UserWithEqualsAndHashCode) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(department, that.department);
    }

    @Override
    public int hashCode() {
        // 遵循合同：若 equals 为 true，则参与 equals 计算的所有字段必须全部纳入 hashCode 计算
        return Objects.hash(id, name, department);
    }

    @Override
    public String toString() {
        return "UserWithEqualsAndHashCode{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
