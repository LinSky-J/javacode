package oop.features;

/**
 * 动物抽象基类：专门用于演示面向对象三大核心特性之【继承（Inheritance）】与【多态（Polymorphism）】。
 *
 * 核心设计定位：
 * 1. 作为所有具体动物子类的血缘基类（is-a 关系）。
 * 2. 抽取所有动物共有的状态（name 属性）与通用行为（sleep 方法）。
 * 3. 声明 abstract 抽象方法 makeSound()，强制要求每个具体子类去实现特异性发声行为。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public abstract class Animal {

    /**
     * 动物名称（父类统一管理状态）
     */
    private String name;

    /**
     * 默认构造函数：供子类无参构造时调用
     */
    public Animal() {
        this("未知动物");
    }

    /**
     * 抽象父类带参构造函数：
     * 虽然抽象类无法直接 new 实例化，但必须提供构造函数供子类在初始化时通过 super() 级联调用！
     *
     * @param name 动物名称
     */
    public Animal(String name) {
        this.name = name;
        System.out.println("   [抽象父类构造方法触发] 初始化动物公有属性: " + name);
    }

    /**
     * 通用具体行为：所有动物都具有相似的休眠行为，子类可直接继承复用
     */
    public void sleep() {
        System.out.println("   [父类具体方法] " + name + " 正在安静地休眠...");
    }

    /**
     * 抽象行为规范：不同动物叫声差异巨大，父类无法给出通用实现，
     * 故声明为 abstract，强制子类重写（Override）！
     */
    public abstract void makeSound();

    /**
     * 获取动物名称
     *
     * @return 动物名称
     */
    public String getName() {
        return name;
    }
}
