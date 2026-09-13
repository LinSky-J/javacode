package oop.features;

/**
 * 具体动物子类：小猫。
 *
 * 继承 Animal 抽象父类，重写 makeSound() 抽象方法，
 * 与 Dog 形成鲜明的同态不同行（同一个方法签名，产生不同具体结果）。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class Cat extends Animal {

    /**
     * 无参构造函数：赋予默认名称
     */
    public Cat() {
        super("小猫");
    }

    /**
     * 构造函数：级联调用父类构造函数
     *
     * @param name 宠物小猫的名字
     */
    public Cat(String name) {
        super(name);
    }

    /**
     * 重写父类抽象方法：实现猫特有的叫声
     */
    @Override
    public void makeSound() {
        System.out.println("   [子类重写方法] " + getName() + " 温柔地叫着：喵喵喵！");
    }
}
