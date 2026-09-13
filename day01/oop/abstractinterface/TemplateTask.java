package oop.abstractinterface;

/**
 * 任务调度抽象模板基类：专门用于演示抽象类的语法特性与设计模式（模板方法模式）。
 *
 * 核心设计考点：
 * 1. 为什么抽象类不能加 final？
 *    abstract 要求必须被继承，final 禁止被继承，二者水火不容。
 * 2. 抽象类可以有构造函数吗？
 *    有！子类 new 时通过 super() 级联调用它来初始化抽象父类的成员状态。
 * 3. 抽象类能被直接实例化吗？
 *    绝对不能！new TemplateTask(...) 会直接产生编译错误。
 *    必须通过继承它的具体子类（无论是独立 .java 文件中声明的具体子类，还是匿名内部类）来创建实例。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public abstract class TemplateTask {

    /**
     * 任务名称（抽象类完全可以拥有普通私有变量，具备状态维护能力）
     */
    private String taskName;

    /**
     * 抽象类构造函数：
     * 即使抽象类不能直接 new，它依然可以定义构造函数供具体子类调用！
     *
     * @param taskName 任务名称
     */
    public TemplateTask(String taskName) {
        this.taskName = taskName;
        System.out.println("   [抽象父类构造函数触发] 初始化任务名成员变量: " + taskName);
    }

    /**
     * 骨架模板方法（Template Method）：
     * 规定任务执行的标准生命周期流程，子类直接复用骨架逻辑。
     */
    public void start() {
        System.out.println("   [模板骨架方法] === 准备启动任务: " + taskName + " ===");
        long startTime = System.currentTimeMillis();

        // 调用由具体子类实现的抽象业务逻辑
        executeJob();

        long costTime = System.currentTimeMillis() - startTime;
        System.out.println("   [模板骨架方法] === 任务: " + taskName + " 执行结束，耗时: " + costTime + " ms ===\n");
    }

    /**
     * 核心抽象方法（abstract）：
     * 父类不清楚具体子类的业务细节，强制要求每个具体子类去实现！
     */
    public abstract void executeJob();

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    public String getTaskName() {
        return taskName;
    }
}
