package oop;

/**
 * 具体任务实现类：数据导出任务。
 *
 * 继承 TemplateTask 抽象基类，并放在独立的 DataExportTask.java 文件中。
 * 演示通过独立具体子类的方式实现抽象类并完成对象实例化。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class DataExportTask extends TemplateTask {

    /**
     * 构造函数：级联调用父类 TemplateTask 的构造函数
     *
     * @param taskName 任务名称
     */
    public DataExportTask(String taskName) {
        super(taskName);
    }

    /**
     * 实现父类的抽象方法 executeJob()
     */
    @Override
    public void executeJob() {
        System.out.println("   [具体子类 DataExportTask 执行] 正在从 MySQL 数据库分页检索 100,000 条业务数据...");
        System.out.println("   [具体子类 DataExportTask 执行] 转换为 Excel 格式流并上传至 OSS 云存储完成！");
    }
}
