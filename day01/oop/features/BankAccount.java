package oop.features;

/**
 * 银行账户实体类：专门用于演示面向对象三大核心特性之一【封装（Encapsulation）】。
 *
 * 封装核心思想：
 * 将对象的属性（数据）和行为（方法）结合为一个独立的整体，
 * 并尽可能隐藏对象的内部实现细节，仅对外暴露受控的访问与操作接口。
 *
 * 封装的收益：
 * 1. 安全性：防止外部代码随意破坏对象内部合法状态（如非法负数余额、越权篡改）。
 * 2. 灵活性：内部实现细节修改时，调用方无需感知或改动。
 * 3. 规范性：统一收口业务校验与日志审计逻辑。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class BankAccount {

    /**
     * 账户所有人姓名（私有成员变量，隐藏内部状态）
     */
    private String owner;

    /**
     * 账户可用余额（严禁外部代码直接 account.balance = -9999 进行非法赋值）
     */
    private double balance;

    /**
     * 构造函数：初始化账户所有人与初始本金
     *
     * @param owner 账户所有者姓名
     * @param initialBalance 初始存款金额
     */
    public BankAccount(String owner, double initialBalance) {
        this.owner = owner;
        if (initialBalance >= 0) {
            this.balance = initialBalance;
        } else {
            this.balance = 0;
            System.out.println("   [封装安全拦截] 初始金额不能为负数，已自动重置为 0 元！");
        }
    }

    /**
     * 存款操作：封装入账业务逻辑与校验规则
     *
     * @param amount 存款金额
     */
    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            System.out.println("   [存款成功] " + owner + " 成功存入: " + amount + " 元，当前余额: " + this.balance + " 元");
        } else {
            System.out.println("   [封装安全拦截] 存款金额必须大于 0！");
        }
    }

    /**
     * 取款操作：封装扣款核心业务逻辑与风控校验规则
     *
     * @param amount 取款金额
     */
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("   [封装安全拦截] 取款金额必须大于 0！");
            return;
        }
        if (amount <= this.balance) {
            this.balance -= amount;
            System.out.println("   [取款成功] " + owner + " 成功取出: " + amount + " 元，剩余余额: " + this.balance + " 元");
        } else {
            System.out.println("   [封装安全拦截] 取款失败：账户余额不足！当前余额: " + this.balance + " 元，申请提现: " + amount + " 元");
        }
    }

    /**
     * 获取账户所有人（对外提供只读访问器）
     *
     * @return 账户所有人姓名
     */
    public String getOwner() {
        return owner;
    }

    /**
     * 获取当前账户余额（只读 getter，不提供 public setBalance 方法，彻底切断外部篡改途径）
     *
     * @return 当前账户余额
     */
    public double getBalance() {
        return balance;
    }
}
