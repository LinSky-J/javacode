package map;

import java.util.Objects;

/**
 * 可变 Key 实体类（反面教材演示）
 * 演示将可变对象作为 HashMap 的 Key，属性被修改后导致 hashCode 改变，
 * 引发数据无法检索与内存泄漏。
 */
public class MutableKey {

    private String identifier;

    public MutableKey(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MutableKey that = (MutableKey) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return "MutableKey{" + identifier + "}";
    }
}
