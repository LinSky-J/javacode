package exceptions;

/**
 * 模拟数据库连接资源类：实现了 AutoCloseable 接口，
 * 专门用于演示 Java 7 引入的顶级异常处理实践【try-with-resources 自动资源回收机制】。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class MockDatabaseResource implements AutoCloseable {

    private final String connectionUrl;

    public MockDatabaseResource(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        System.out.println("   [资源分配] 成功建立数据库连接: " + connectionUrl);
    }

    public void executeQuery(String sql) {
        System.out.println("   [执行 SQL] 成功执行查询: " + sql);
    }

    /**
     * 实现 AutoCloseable 的核心方法：
     * try-with-resources 代码块退出时，无论是否发生异常，JVM 保证自动触发此方法关闭资源！
     */
    @Override
    public void close() {
        System.out.println("   [资源自动释放] try-with-resources 触发：数据库连接 " + connectionUrl + " 已安全关闭！");
    }
}
