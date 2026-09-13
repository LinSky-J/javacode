package generics;

/**
 * 用户业务实体类：作为泛型容器和泛型接口操作的具体业务数据载体。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class UserEntity {

    private Long id;
    private String username;
    private int age;

    public UserEntity(Long id, String username, int age) {
        this.id = id;
        this.username = username;
        this.age = age;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "UserEntity{id=" + id + ", username='" + username + "', age=" + age + "}";
    }
}
