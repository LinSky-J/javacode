package reflection;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;

/**
 * 面试专题：Java 反射机制、运行期类型透视与企业级框架应用实战。
 *
 * 本类对应面试核心题目：
 * 1. 什么是反射?
 * 2. 反射在你平时写代码或者框架中的应用场景有哪些?
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaReflectionConceptAndScenarios {

    public static void main(String[] args) {
        initConsoleEncoding();

        System.out.println("======================================================================");
        System.out.println("           Java 反射底层机制、运行时动态操作与框架应用全景解析         ");
        System.out.println("======================================================================");

        explainWhatIsReflection();
        explainReflectionScenariosInFrameworks();

        System.out.println("\n======================================================================");
        System.out.println("            反射机制与框架应用解析完毕，请细读类中源码与注释          ");
        System.out.println("======================================================================");
    }

    /**
     * 第一部分：
     * 什么是反射?
     *
     * 面试核心考点：反射的定义、类加载镜像 Class 对象、核心四大 API、优缺点。
     */
    public static void explainWhatIsReflection() {
        System.out.println("1. 什么是反射?");

        /*
         * 1. 反射的核心定义（Reflection）：
         *    - 在 Java【运行状态（Runtime）】中，对于任意一个类，都能够知道这个类的所有属性和方法；
         *      对于任意一个对象，都能够调用它的任意一个方法和属性。
         *    - 这种“在运行时动态获取类信息以及动态调用对象方法”的机制称为 Java 语言的反射机制。
         *
         * 2. 反射的底层支柱：Class 对象
         *    - 当 JVM 加载一个 .class 文件到内存中时，JVM 会在堆区/方法区为该类自动创建一个独特的 java.lang.Class 对象。
         *    - 这个 Class 对象就像一面“镜子”，完整映照着这个类的所有构造函数、字段、方法、注解等元数据。
         *
         * 3. 核心 API 体系：
         *    - java.lang.Class：类的抽象表示，反射的总入口；
         *    - java.lang.reflect.Constructor：类的构造方法；
         *    - java.lang.reflect.Method：类的方法；
         *    - java.lang.reflect.Field：类的成员变量/字段；
         *    - java.lang.reflect.Modifier：访问权限修饰符解析器。
         */
        System.out.println("\n--- [反射现场实测] 运行期动态透视 OrderServiceImpl 类的全部元信息 ---");
        Class<?> targetClass = OrderServiceImpl.class;

        System.out.println("   [类名剖析] 全限定名: " + targetClass.getName() + " | 简单类名: " + targetClass.getSimpleName());

        // 1. 获取并透视所有构造方法
        System.out.println("   [构造器剖析] 检索到的构造函数列表:");
        for (Constructor<?> c : targetClass.getDeclaredConstructors()) {
            System.out.println("      " + Modifier.toString(c.getModifiers()) + " " + c.getName());
        }

        // 2. 获取并透视所有声明字段（包括 private 私有字段）
        System.out.println("   [字段剖析] 检索到的成员字段列表:");
        for (Field f : targetClass.getDeclaredFields()) {
            System.out.println("      " + Modifier.toString(f.getModifiers()) + " " + f.getType().getSimpleName() + " " + f.getName());
        }

        // 3. 获取并透视所有方法（包括 private 私有方法）
        System.out.println("   [方法剖析] 检索到的成员方法列表:");
        for (Method m : targetClass.getDeclaredMethods()) {
            System.out.println("      " + Modifier.toString(m.getModifiers()) + " " + m.getReturnType().getSimpleName() + " " + m.getName() + "()");
        }

        // 4. 反射动态调用实例方法
        System.out.println("\n--- [反射动态执行实测] 调用 OrderServiceImpl 的方法 ---");
        try {
            Object serviceInstance = targetClass.getDeclaredConstructor().newInstance();
            Method createOrderMethod = targetClass.getMethod("createOrder", String.class, double.class);
            Object invokeResult = createOrderMethod.invoke(serviceInstance, "ORD_20260913_888", 699.0);
            System.out.println("   [反射执行结果返回] " + invokeResult);
        } catch (Exception e) {
            System.out.println("   反射调用失败: " + e.getMessage());
        }

        /*
         * 5. 反射的优缺点对比：
         *    - 优点：极大的灵活性和通用性，是现代所有主流框架（Spring/MyBatis/Hibernate/Dubbo）的立身之本。
         *    - 缺点：
         *      1. 性能相对直接调用有损耗（涉及安全检查、JIT 无法直接内联、参数封箱解箱开销）；
         *      2. 破坏面向对象封装性（setAccessible(true) 可以随意篡改私有数据）；
         *      3. 失去编译期类型安全（拼错方法名只能在运行期爆发 NoSuchMethodException）。
         */
    }

    /**
     * 第二部分：
     * 反射在你平时写代码或者框架中的应用场景有哪些?
     *
     * 面试拔高考点：Spring IoC/DI、动态代理 AOP、MyBatis ORM 映射、通用数据导入导出、RPC 通信。
     */
    public static void explainReflectionScenariosInFrameworks() {
        System.out.println("\n\n2. 反射在你平时写代码或者框架中的应用场景有哪些?");

        /*
         * 场景一：Spring 框架核心 —— 控制反转（IoC）与依赖注入（DI）
         * - Spring 启动时，扫描 @Component / @Service / @Autowired 注解；
         * - 通过 Class.forName(className) 拿到 Class，通过 Constructor.newInstance() 实例化 Bean；
         * - 通过 Field.setAccessible(true) + Field.set(bean, dependency) 把依赖的组件私有注入进去。
         */
        System.out.println("--- 场景一：Spring IoC / DI 容器（反射创建 Bean 与私有属性注入）实测 ---");
        MiniSpringIocContainer iocContainer = new MiniSpringIocContainer();
        OrderService orderService = (OrderService) iocContainer.registerAndInjectBean(
                "orderService",
                "reflection.OrderServiceImpl",
                "databaseUrl",
                "jdbc:mysql://10.0.0.88:3306/production_cloud_order_db" // 动态注入自定义数据源
        );
        // 调用测试注入是否生效
        orderService.createOrder("MOCK_SPRING_ORDER_001", 999.0);

        /*
         * 场景二：动态代理与面向切面编程（AOP / RPC 远程调用）
         * - JDK 动态代理 Proxy.newProxyInstance 底层就是依赖 InvocationHandler 中的 Method.invoke(target, args) 反射执行目标对象；
         * - Spring AOP 事务控制（@Transactional）、日志记录、权限鉴权全是在代理类中通过反射拦截执行；
         * - RPC 框架（如 Dubbo、gRPC、Feign）通过网络接收到方法名与参数后，服务端反射调用本地 Service。
         */
        System.out.println("\n--- 场景二：JDK 动态代理与 AOP 切面拦截（底层基于反射 Method.invoke）实测 ---");
        OrderService proxyInstance = (OrderService) Proxy.newProxyInstance(
                OrderService.class.getClassLoader(),
                new Class<?>[]{OrderService.class},
                (proxy, method, methodArgs) -> {
                    System.out.println("   [AOP 前置切面通知] 正在执行全局事务开启与性能监控埋点，方法: " + method.getName());
                    long start = System.currentTimeMillis();

                    // 通过反射触发被代理真实对象的业务方法
                    Object result = method.invoke(orderService, methodArgs);

                    System.out.println("   [AOP 后置切面通知] 全局事务提交完成，耗时: " + (System.currentTimeMillis() - start) + " ms");
                    return result;
                }
        );
        proxyInstance.createOrder("AOP_PAYMENT_TX_999", 588.0);

        /*
         * 场景三：ORM 持久层框架结果集自动映射（MyBatis / Hibernate / JPA）
         * - 执行 SQL 查询后，数据库返回 JDBC 原生 ResultSet 结果集（行与列）。
         * - MyBatis 通过反射：遍历实体类的属性名，或者读取 @Column 注解，
         *   通过反射获取实体对象的 setter 方法或直接对私有字段 field.set(entity, rsValue) 封装返回！
         */
        System.out.println("\n--- 场景三：ORM 数据映射（MyBatis 数据库列自动注入实体类） ---");
        System.out.println("   实现：MyBatis 执行 JDBC 查询得到列值后，通过反射遍历实体属性，调用 field.set() 自动将数据库字段绑定到 Java 对象上。");

        /*
         * 场景四：企业级通用 Excel 数据导入导出工具（EasyExcel / Apache POI）
         * - 传统方式：给每个实体类写极其繁重的赋值逻辑。
         * - 反射方式：编写一个通用的 ExcelExportUtil<T>，传入任意实体类 List<T>，
         *   通过反射遍历对象的全部属性与 @ExcelProperty 注解，自动生成 Excel 单元格！
         */
        System.out.println("\n--- 场景四：通用组件与工具类开发（EasyExcel 导出、通用数据校验） ---");
        System.out.println("   实现：通过反射通用遍历任意 JavaBean 的字段与注解，实现通用数据校验器、JSON 序列化器（Jackson/Fastjson）。");

        /*
         * 场景五：自动化测试与 Mock 框架（JUnit 5 / Mockito）
         * - JUnit 的 @Test、@BeforeEach 注解，是通过测试运行器反射扫描并执行的；
         * - Mockito 通过反射强行将 Mock 假对象注入到目标待测类的 private 私有属性中。
         */
        System.out.println("\n--- 场景五：单元测试与 Mock 框架（JUnit、Mockito 私有属性注入） ---");
        System.out.println("   实现：JUnit 测试运行器反射检索带 @Test 的方法并触发；Mockito 通过反射替换待测对象内部的私有依赖。");
    }

    /**
     * 初始化控制台字符编码，解决 Windows 环境终端输出中文乱码的问题。
     */
    private static void initConsoleEncoding() {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}
