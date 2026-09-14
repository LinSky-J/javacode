package objects.methods;

import java.util.Objects;

/**
 * 反面教材实体类：只重写了 equals，但没有重写 hashCode。
 * 用于现场演示在 HashMap / HashSet 中查找失败与数据重复的严重隐患。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class UserWithoutHashCode {

    private final Long id;
    private final String name;

    public UserWithoutHashCode(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserWithoutHashCode that = (UserWithoutHashCode) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    // 注意：此处故意不重写 hashCode()，使对象继承 java.lang.Object 的默认基于内存地址的实现

    @Override
    public String toString() {
        return "UserWithoutHashCode{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", identityHashCode=" + System.identityHashCode(this) +
                '}';
    }
}
