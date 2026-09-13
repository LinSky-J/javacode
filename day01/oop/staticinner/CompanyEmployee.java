package oop.staticinner;

/**
 * 企业员工实体类：专门用于演示【Java 中的静态变量和静态方法】。
 *
 * 核心知识点：
 * 1. 静态变量（static 变量）：
 *    - 属于类本身，全类所有实例共享同一份内存（存放在 JVM 方法区/元空间中的 Class 镜像后部）。
 *    - 任何一个对象修改了静态变量，其他所有对象读取到的都是修改后的最新值。
 * 2. 实例变量：
 *    - 属于具体对象实例，每 new 一个对象都会在堆内存中独立分配一份空间。
 * 3. 静态方法：
 *    - 属于类本身，无需创建对象实例即可直接通过 类名.方法名() 调用。
 *    - 静态方法内部【绝对不能使用 this 或 super 关键字】，也【不能直接访问非静态的成员变量和非静态方法】。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class CompanyEmployee {

    /**
     * 静态变量：所属公司名称（全公司所有员工共享该属性）
     */
    public static String companyName;

    /**
     * 实例变量：员工个人姓名（属于每个具体员工对象独立私有）
     */
    private String employeeName;

    /**
     * 构造函数：初始化员工个人姓名
     *
     * @param employeeName 员工姓名
     */
    public CompanyEmployee(String employeeName) {
        this.employeeName = employeeName;
    }

    /**
     * 获取员工姓名
     *
     * @return 员工姓名
     */
    public String getEmployeeName() {
        return employeeName;
    }

    /**
     * 获取员工所在公司名称（实例方法中可以自由读取静态变量）
     *
     * @return 公司名称
     */
    public String getCompany() {
        return companyName;
    }

    /**
     * 静态方法：发布公司全员广播通知
     * 通过 CompanyEmployee.printCompanyNotice(...) 直接调用。
     *
     * @param notice 广播通知内容
     */
    public static void printCompanyNotice(String notice) {
        System.out.println("   [公司广播通知] " + companyName + " 发布通知: " + notice);
    }
}
