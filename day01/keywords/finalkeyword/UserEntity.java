package keywords.finalkeyword;

/**
 * 用户实体载体类：专门用于演示【final 修饰引用数据类型变量时的核心本质】。
 *
 * 面试超高频踩坑考点：
 * "final 修饰一个对象引用时，该引用指向的对象属性是否可以被修改？"
 *
 * 核心答案：
 * 1. 引用变量本身被锁定：引用中存储的【堆内存地址】绝对不可改变（即不能再重新 new 或者指向其他对象）。
 * 2. 堆中对象内容未被锁定：该对象内部的成员属性【完全可以正常修改】（除非对象的属性自身也被声明为 final）！
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class UserEntity {

    private String name;
    private int age;

    public UserEntity(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "UserEntity{name='" + name + "', age=" + age + "}@" + Integer.toHexString(System.identityHashCode(this));
    }
}
