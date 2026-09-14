package annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 面试专题：Java 注解底层原理、JVM 动态代理实现真相与注解作用域深度解析。
 *
 * 本类对应面试核心题目：
 * 1. 能讲一讲Java注解的原理吗?
 * 2. 对注解解析的底层实现了解吗?
 * 3. Java注解的作用域呢?
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class JavaAnnotationPrincipleAndParsing {

    public static void main(String[] args) {

        System.out.println("======================================================================");
        System.out.println("          Java 注解本质、动态代理底层解析与作用域全景解析             ");
        System.out.println("======================================================================");

        explainAnnotationPrinciple();
        explainAnnotationParsingUnderlying();
        explainAnnotationScopeAndRetention();

        System.out.println("\n======================================================================");
        System.out.println("            注解底层原理与作用域解析完毕，请细读类中源码与注释        ");
        System.out.println("======================================================================");
    }

    /**
     * 第一部分：
     * 能讲一讲Java注解的原理吗?
     *
     * 面试核心考点：注解的本质接口定义、元数据标签定位、注解处理器。
     */
    public static void explainAnnotationPrinciple() {
        System.out.println("1. 能讲一讲Java注解的原理吗?");

        /*
         * 1. 注解的语法本质（颠覆认知的真相）：
         *    - 注解本质上是一个【继承了 java.lang.annotation.Annotation 接口的特殊 interface】！
         *    - 当我们写 public @interface ApiOperation 时，经过 javac 编译后，生成的字节码本质上就是：
         *      public interface ApiOperation extends java.lang.annotation.Annotation { ... }
         *
         * 2. 注解方法（属性）的本质：
         *    - 注解内部定义的 String value() default ""; 本质上就是该接口中声明的无参抽象方法！
         *
         * 3. 元数据标签定位：
         *    - 注解自身是【没有任何可执行业务逻辑的标签（Metadata）】！
         *    - 它就像贴在类、方法或字段上的一张“便签纸”。便签纸本身不会动，必须由外部程序（框架/注解处理器/反射代码）
         *      去读取这张便签纸，并根据上面的信息执行相应的逻辑。
         */
        System.out.println("\n--- [现场实测证据一] 反射验证 ApiOperation 注解的底层接口本质 ---");
        Class<?> annotationClass = ApiOperation.class;
        System.out.println("   ApiOperation 是否为接口: " + annotationClass.isInterface());
        System.out.println("   ApiOperation 父接口列表: ");
        for (Class<?> superInterface : annotationClass.getInterfaces()) {
            System.out.println("      " + superInterface.getName());
        }
        System.out.println("   (铁证如山：@interface 经过编译后，底层 100% 就是一个继承自 java.lang.annotation.Annotation 的 interface！)");
    }

    /**
     * 第二部分：
     * 对注解解析的底层实现了解吗?
     *
     * 面试拔高考点：JVM 动态代理生成代理类、AnnotationInvocationHandler、字节码属性表。
     */
    public static void explainAnnotationParsingUnderlying() {
        System.out.println("\n\n2. 对注解解析的底层实现了解吗?");

        /*
         * 1. 运行期注解解析的底层全链路揭秘（反射如何拿到注解对象）：
         *    - 第一步（字节码存储）：编译期，javac 把标注在方法/类上的注解信息保存到 .class 文件的
         *      RuntimeVisibleAnnotations 属性表中。
         *    - 第二步（类加载入内存）：JVM 类加载阶段，将这些字节码属性解析到方法区/元空间的 Class 镜像中。
         *    - 第三步（核心神操作 - 动态代理！）：
         *      当我们在 Java 代码中调用 method.getAnnotation(ApiOperation.class) 时，
         *      JVM 底层使用【JDK 动态代理机制（Proxy.newProxyInstance）】在内存中悄悄生成了一个实现了 ApiOperation 接口的代理类！
         *    - 第四步（数据检索）：
         *      这个动态代理对象的内部持有一个 AnnotationInvocationHandler（调用处理器），
         *      处理器内部维护着一个 Map<String, Object>（存着注解各个属性名及其对应的值）。
         *      当调用 apiOperation.value() 时，触发代理对象的 invoke() 方法，从 Map 中通过 "value" 键把配置的数据读取出来返回！
         */
        System.out.println("--- [现场实测证据二] 反射抓包证实 getAnnotation() 返回的真实对象其实是一个 JDK 动态代理！ ---");
        try {
            Method method = ProductService.class.getMethod("offlineProduct", Long.class);
            ApiOperation annotation = method.getAnnotation(ApiOperation.class);

            System.out.println("   [抓包结果] annotation 对象真实运行时类名: " + annotation.getClass().getName());
            System.out.println("   [抓包结果] Proxy.isProxyClass(annotation.getClass()): " + Proxy.isProxyClass(annotation.getClass()));
            System.out.println("   (铁证如山：运行时拿到的注解对象根本不是普通实体类，而是一个纯粹的 JDK 动态代理 $Proxy 对象！)");

            // 业务调用
            System.out.println("\n   [代理方法调用] annotation.value() -> " + annotation.value());
            System.out.println("   [代理方法调用] annotation.author() -> " + annotation.author());
        } catch (Exception e) {
            System.out.println("   解析注解失败: " + e.getMessage());
        }
    }

    /**
     * 第三部分：
     * Java注解的作用域呢?
     *
     * 面试核心考点：生命周期保留策略（RetentionPolicy）与目标适用范围（ElementType）。
     */
    public static void explainAnnotationScopeAndRetention() {
        System.out.println("\n\n3. Java注解的作用域呢?");

        /*
         * 注解的“作用域”在 Java 中由两大核心元注解（Meta-Annotation）严格界定：
         * 维度一：生命周期保留策略（@Retention）—— 决定注解活到哪一个阶段
         * -----------------------------------------------------------------------------------------
         * 保留策略枚举                生命周期存活阶段                           典型应用场景
         * -----------------------------------------------------------------------------------------
         * RetentionPolicy.SOURCE      只活在 .java 源码中，编译为 .class 后丢弃      @Override, @SuppressWarnings, Lombok
         * RetentionPolicy.CLASS       保留在 .class 字节码中，但类加载时不载入内存   ASM 字节码插桩、静态代码检查工具
         * RetentionPolicy.RUNTIME     常驻 JVM 运行期内存中，可通过反射完全读取     Spring, MyBatis, 自定义业务框架
         * -----------------------------------------------------------------------------------------
         */
        System.out.println("--- 维度一：生命周期保留策略（@Retention，决定注解活到哪个生命周期阶段） ---");
        System.out.println("   1. SOURCE (源码级)：编译为 .class 字节码时立即被丢弃。例如 @Override、Lombok @Data（编译期直接由 javac 插件改写字节码）。");
        System.out.println("   2. CLASS (字节码级，默认)：写入 .class 字节码，但 JVM 类加载时不会装入运行时内存，反射无法获取。常用于字节码静态增强。");
        System.out.println("   3. RUNTIME (运行期级)：类加载后永久常驻内存，支持运行期通过反射 getAnnotation() 动态读取。Spring/MyBatis 等所有业务注解必须使用该策略！");

        /*
         * 维度二：目标适用范围（@Target）—— 决定注解可以打在哪些语法结构上
         * -----------------------------------------------------------------------------------------
         * 适用范围枚举                允许标注的语法目标
         * -----------------------------------------------------------------------------------------
         * ElementType.TYPE            类、接口（包括注解类型）、枚举
         * ElementType.METHOD          成员方法
         * ElementType.FIELD           成员变量 / 属性字段
         * ElementType.PARAMETER       方法形参
         * ElementType.CONSTRUCTOR     构造函数
         * ElementType.LOCAL_VARIABLE  局部变量
         * ElementType.ANNOTATION_TYPE 注解类型（专门修饰其他元注解）
         * -----------------------------------------------------------------------------------------
         */
        System.out.println("\n--- 维度二：目标适用范围（@Target，决定注解可以打在什么程序元素上） ---");
        System.out.println("   TYPE(类/接口), METHOD(方法), FIELD(字段), PARAMETER(入参), CONSTRUCTOR(构造方法), LOCAL_VARIABLE(局部变量)。");

        // 字段级注解实测
        System.out.println("\n--- [字段级注解实测] 解析 ProductService 上的 @TableField 注解 ---");
        try {
            Field field = ProductService.class.getDeclaredField("productId");
            TableField tableField = field.getAnnotation(TableField.class);
            System.out.println("   字段 " + field.getName() + " 映射物理列: " + tableField.columnName() + " | 是否为主键: " + tableField.isPrimaryKey());
        } catch (Exception e) {
            System.out.println("   解析字段注解失败: " + e.getMessage());
        }
    }
}
