package classloading;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 类加载器体系结构、双亲委派模型深度剖析与破坏双亲委派实战
 *
 * 涵盖面试核心题目：
 * 3. 类的加载器有那些。
 * 拓展：双亲委派机制原理、设计目的与三大破坏双亲委派场景。
 */
public class ClassLoaderHierarchyAndDelegationMechanism {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          类加载器层级体系、双亲委派机制与破坏双亲委派全景实测          ");
        System.out.println("======================================================================");

        explainClassLoaderHierarchy();
        inspectRuntimeClassLoaderTree();
        explainParentsDelegationModel();
        explainBreakDelegationScenarios();

        System.out.println("======================================================================");
        System.out.println("                 类加载器体系与双亲委派解析完成                      ");
        System.out.println("======================================================================");
    }

    /**
     * 问题 3：类的加载器有那些。
     *
     * 体系四层：
     * 1. 启动类加载器（Bootstrap ClassLoader）
     * 2. 扩展类加载器 / 平台类加载器（Extension / Platform ClassLoader）
     * 3. 应用程序类加载器（Application ClassLoader）
     * 4. 自定义类加载器（Custom ClassLoader）
     */
    public static void explainClassLoaderHierarchy() {
        System.out.println("\n--- 1. JVM 四大类加载器分层体系与职责 ---");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.println("类加载器名称             | 实现语言 | 加载目录范围                                | 代码中获取方式");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.println("1. 启动类加载器          | C/C++    | <JAVA_HOME>/lib、-Xbootclasspath 参数指定   | 调用 getClassLoader() 返回 null");
        System.out.println("   (Bootstrap ClassLoader)|         | 核心类库（如 rt.jar、java.base 等）         |");
        System.out.println("2. 扩展/平台类加载器     | Java     | <JAVA_HOME>/lib/ext、java.ext.dirs 指定目录 | PlatformClassLoader / ExtClassLoader");
        System.out.println("   (Platform/ExtClassLoader)       | 扩展类库（JDK 9 模块化后更名为 Platform）   |");
        System.out.println("3. 应用程序类加载器      | Java     | ClassPath（用户类路径）、-cp 参数指定目录   | ClassLoader.getSystemClassLoader()");
        System.out.println("   (AppClassLoader)       |         | 开发者日常编写的全部业务类与第三方依赖 jar   |");
        System.out.println("4. 自定义类加载器        | Java     | 自定义路径（磁盘目录、网络 URL、数据库等）   | new CustomClassLoader()");
        System.out.println("   (Custom ClassLoader)   |         | 继承 ClassLoader，用于解密、隔离、热部署     |");
        System.out.println("---------------------------------------------------------------------------------------------------------");
    }

    /**
     * 运行期现场打印当前 JVM 的类加载器委派树
     */
    public static void inspectRuntimeClassLoaderTree() {
        System.out.println("\n--- 2. [代码现场实测] 运行时打印类加载器层级关系 ---");

        // 1. 获取当前业务类的类加载器
        ClassLoader currentClassLoader = ClassLoaderHierarchyAndDelegationMechanism.class.getClassLoader();
        System.out.println("1. 当前业务类的类加载器: " + currentClassLoader);

        // 2. 获取其父类加载器
        ClassLoader parentClassLoader = currentClassLoader.getParent();
        System.out.println("2. 其父类加载器（平台/扩展类加载器）: " + parentClassLoader);

        // 3. 获取父类加载器的父加载器（启动类加载器）
        ClassLoader bootstrapClassLoader = parentClassLoader.getParent();
        System.out.println("3. 顶层父类加载器（Bootstrap ClassLoader）: " + bootstrapClassLoader + " (返回 null 代表由 C++ 实现的原生启动类加载器)");

        // 4. 探查 JDK 核心类的类加载器（如 String、Object）
        ClassLoader stringClassLoader = String.class.getClassLoader();
        System.out.println("4. JDK 核心类 String.class 的类加载器: " + stringClassLoader + " (返回 null，说明由 Bootstrap ClassLoader 根加载器加载)");
    }

    /**
     * 深入讲解：双亲委派机制（Parents Delegation Model）
     *
     * 原理：自底向上检查缓存，自顶向下尝试加载。
     * 目的：防止核心 API 被篡改（沙箱安全）、避免类重复加载、保证类型唯一性。
     */
    public static void explainParentsDelegationModel() {
        System.out.println("\n--- 3. 双亲委派机制底层工作原理与三大设计目的 ---");
        System.out.println("一、双亲委派的工作流程（两阶段流转）：");
        System.out.println("   阶段 1：自底向上检查缓存（委派阶段）");
        System.out.println("     - 当 AppClassLoader 收到一个类加载请求时，首先检查自己是否已经加载过此类，若有直接返回；");
        System.out.println("     - 若没有，它绝不自己尝试加载，而是将请求向上委派给父加载器 PlatformClassLoader；");
        System.out.println("     - PlatformClassLoader 同样向上委派给 Bootstrap ClassLoader；");
        System.out.println("     - 逐层向上，直到到达顶层的 Bootstrap 启动类加载器。");
        System.out.println("   阶段 2：自顶向下尝试加载（执行阶段）");
        System.out.println("     - Bootstrap 尝试在其负责的 <JAVA_HOME>/lib 目录下搜索此类；");
        System.out.println("       * 若找到：加载此类并返回，加载结束！");
        System.out.println("       * 若未找到：向下抛出 ClassNotFoundException，通知子加载器 PlatformClassLoader 去尝试加载；");
        System.out.println("     - PlatformClassLoader 重复上述过程，若也找不到，再向下通知 AppClassLoader 去 ClassPath 中搜索；");
        System.out.println("     - 若所有加载器都无法加载，最终抛出 java.lang.ClassNotFoundException。");

        System.out.println("\n二、为什么必须采用双亲委派模型？（三大核心设计意图）");
        System.out.println("   1. 沙箱安全防篡改（保护 Java 核心 API 的安全）：");
        System.out.println("      - 假设有黑客编写了一个名为 java.lang.String 的恶意类，试图篡改密码校验；");
        System.out.println("      - 由于双亲委派机制的存在，加载请求最终都会委派给 Bootstrap ClassLoader；");
        System.out.println("      - Bootstrap 加载的永远是 JDK 原生 rt.jar 中的 String 类，黑客的恶意类永远没有机会被加载！");
        System.out.println("   2. 避免类的重复加载：");
        System.out.println("      - 父类加载器加载过的类会放入缓存，子加载器无需重复解析与加载，极大节约元空间内存。");
        System.out.println("   3. 保证基础类的唯一类型标志（JVM 类型识别规则）：");
        System.out.println("      - 在 JVM 中，确定一个类的唯一性，必须由【全限定类名 + 加载它的 ClassLoader 实例】共同决定！");
        System.out.println("      - 双亲委派确保无论在系统的哪个角落引用 Object，它都是同一个 Class 实例。");
    }

    /**
     * 深入讲解：破坏双亲委派的三大经典场景（面试压轴高频题）
     */
    public static void explainBreakDelegationScenarios() {
        System.out.println("\n--- 4. 破坏双亲委派机制的三大经典历史与生产场景 ---");

        System.out.println("场景一：JDK 历史历史包袱（JDK 1.2 引入双亲委派之前的遗留兼容）");
        System.out.println("   - 双亲委派在 JDK 1.2 才被正式提出，而 java.lang.ClassLoader 从 JDK 1.0 就已存在；");
        System.out.println("   - JDK 1.2 之前用户通过重写 loadClass() 自定义加载逻辑；");
        System.out.println("   - [官方最佳实践] 为向前兼容，JDK 1.2 起推荐用户重写 findClass()，保留 loadClass() 中的双亲委派逻辑！");

        System.out.println("\n场景二：SPI 机制与线程上下文类加载器（Thread Context ClassLoader）");
        System.out.println("   - 典型代表：JDBC 数据库驱动管理 DriverManager。");
        System.out.println("   - 核心矛盾：");
        System.out.println("     * java.sql.Driver 核心标准接口位于 rt.jar 中，由顶层的【Bootstrap ClassLoader】加载；");
        System.out.println("     * 但具体的实现类（如 MySQL 的 com.mysql.cj.jdbc.Driver）位于第三方依赖 jar 包中，属于【AppClassLoader】的管辖范围；");
        System.out.println("     * 按照双亲委派原则，Bootstrap ClassLoader 根本无法访问、也无法加载用户路径下的实现类！");
        System.out.println("   - 破解之道（反向委派）：");
        System.out.println("     * Java 团队引入了【线程上下文类加载器（Thread Context ClassLoader）】；");
        System.out.println("     * Bootstrap 核心类通过 Thread.currentThread().getContextClassLoader()（默认即为 AppClassLoader）");
        System.out.println("       反向委托子类加载器去加载第三方厂商实现类，巧妙打破了双亲委派由下至上的固定单向流转！");

        System.out.println("\n场景三：Web 容器隔离与代码热替换（Tomcat & OSGi）");
        System.out.println("   - 典型代表：Tomcat 容器中独立 WebApp 的隔离部署。");
        System.out.println("   - 核心痛点：");
        System.out.println("     * 一个 Tomcat 下部署两个 Web 应用：WebApp1 使用 Spring 4.x，WebApp2 使用 Spring 5.x；");
        System.out.println("     * 如果遵循双亲委派，父加载器加载了 Spring 4.x 后，WebApp2 将无法加载自身的 Spring 5.x，发生类版本冲突！");
        System.out.println("   - Tomcat 的解决方案：");
        System.out.println("     * Tomcat 为每一个部署的 Web 应用创建了一个专属的【WebappClassLoader】；");
        System.out.println("     * WebappClassLoader 重写了 loadClass() 方法：优先在自身的 WEB-INF/classes 和 WEB-INF/lib 下加载类；");
        System.out.println("     * 只有自己找不到时，才委派给 SharedClassLoader 或 CommonClassLoader 父加载器（除核心 JRE 类库外优先自己加载）；");
        System.out.println("     * 实现了不同应用之间相同类库不同版本的彻底物理隔离，以及不重启 Tomcat 即可热替换 WebApp 的能力！");
    }
}
