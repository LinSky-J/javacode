package set;

import java.util.Objects;

/**
 * 用户实体类，用于验证 Set 集合的去重原理、hashCode/equals 契约以及排序规则
 */
public class UserAccountItem implements Comparable<UserAccountItem> {

    private long userId;
    private String username;
    private int score;

    public UserAccountItem() {
    }

    public UserAccountItem(long userId, String username, int score) {
        this.userId = userId;
        this.username = username;
        this.score = score;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * 重写 equals 方法：根据业务主键 userId 判断对象是否逻辑相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAccountItem that = (UserAccountItem) o;
        return userId == that.userId && Objects.equals(username, that.username);
    }

    /**
     * 重写 hashCode 方法：与 equals 逻辑保持绝对一致，保证相同对象生成相同哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    /**
     * 实现 Comparable 接口自然排序：优先按分数降序，分数相同按 userId 升序
     */
    @Override
    public int compareTo(UserAccountItem other) {
        if (other == null) {
            return 1;
        }
        int scoreCompare = Integer.compare(other.score, this.score); // 降序
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return Long.compare(this.userId, other.userId); // 升序
    }

    @Override
    public String toString() {
        return String.format("UserAccountItem{userId=%d, username='%s', score=%d}", userId, username, score);
    }
}
