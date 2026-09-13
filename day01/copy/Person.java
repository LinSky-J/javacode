package copy;

import java.io.Serializable;

/**
 * 人员实体类：宿主对象，内部持有可变引用类型 Address，
 * 用于完整演示浅拷贝缺陷以及深拷贝的多种实现方式。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class Person implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private int age;
    private Address address; // 可变引用类型对象：浅拷贝与深拷贝的关键分水岭

    public Person(String name, int age, Address address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }

    /**
     * 实现深拷贝方法三：拷贝构造函数（Copy Constructor）
     * 规范：由调用方通过 new Person(oldPerson) 显式调用，
     * 内部主动为引用类型的成员变量递归创建全新的对象实例。
     *
     * @param other 源 Person 对象
     */
    public Person(Person other) {
        if (other != null) {
            this.name = other.name;
            this.age = other.age;
            // 关键：不能直接 this.address = other.address，必须递归调用 Address 的拷贝构造函数！
            this.address = (other.address != null) ? new Address(other.address) : null;
        }
    }

    /**
     * 浅拷贝实现：
     * 仅调用 super.clone()。
     * 效果：基本数据类型复制值，引用数据类型仅仅复制引用指针，导致新老对象共享同一个 Address 实例！
     *
     * @return 浅拷贝出的新 Person 对象
     */
    public Person shallowClone() {
        try {
            return (Person) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("克隆异常，当前类已实现 Cloneable", e);
        }
    }

    /**
     * 实现深拷贝方法一：重写 clone() 并手动层层级联克隆引用字段
     *
     * 效果：在调用 super.clone() 拷贝宿主对象后，
     * 手动对每一个引用类型字段递归调用其自身的 clone() 方法，完成物理隔离。
     *
     * @return 深拷贝出的新 Person 对象
     */
    public Person deepCloneByCascade() {
        try {
            // 1. 先克隆宿主外层对象（此时 address 仍然是指向旧内存）
            Person cloned = (Person) super.clone();
            // 2. 手动对引用字段进行深度克隆并重新赋值
            if (this.address != null) {
                cloned.address = this.address.clone();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("克隆异常，当前类已实现 Cloneable", e);
        }
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + ", address=" + address + "}@0x"
                + Integer.toHexString(System.identityHashCode(this)).toUpperCase();
    }
}
