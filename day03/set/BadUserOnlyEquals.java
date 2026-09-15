package set;

/**
 * 未重写 hashCode 的反面教材实体类（导致 HashSet 去重失效）
 */
public class BadUserOnlyEquals {

    private final long id;
    private final String name;

    public BadUserOnlyEquals(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BadUserOnlyEquals that = (BadUserOnlyEquals) o;
        return id == that.id;
    }
    // 刻意不重写 hashCode()，使用默认基于对象内存地址的哈希码
}
