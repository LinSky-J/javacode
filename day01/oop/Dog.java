package oop;

/**
 * 具体动物子类：小狗。
 *
 * 继承 Animal 抽象父类，重写 makeSound() 抽象方法，
 * 体现面向对象的【继承】与【运行期动态多态（Dynamic Dispatch）】。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class Dog extends Animal {

    /**
     * 无参构造函数：赋予默认名称
     */
    public Dog() {
        super("小狗");
    }

    /**
     * 构造函数：必须通过 super() 第一行显式或隐式调用父类构造函数
     *
     * @param name 宠物小狗的名字
     */
    public Dog(String name) {
        super(name);
    }

    /**
     * 重写父类抽象方法：实现狗特有的叫声
     */
    @Override
    public void makeSound() {
        System.out.println("   [子类重写方法] " + getName() + " 欢快地叫着：汪汪汪！");
    }
}
