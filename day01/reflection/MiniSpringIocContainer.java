package reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 极简仿写 Spring IoC / DI 容器：演示反射在主流企业级框架中的核心应用场景。
 *
 * 核心考点：
 * 1. 控制反转（IoC）：通过类的全限定名字符串，反射加载 Class 并调用无参构造函数创建 Bean 实例。
 * 2. 依赖注入（DI）：通过反射遍历对象的字段，破除 private 权限（setAccessible(true)），
 *    在无需编写 public setter 方法的情况下强行将配置或依赖注入到字段中（类似 Spring @Autowired / @Value）。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class MiniSpringIocContainer {

    /**
     * 单例池：存储反射创建并初始化完毕的 Bean 实例
     */
    private final Map<String, Object> singletonObjects = new HashMap<>();

    /**
     * 模拟 Spring 核心逻辑：反射根据类名实例化 Bean 并反射注入私有属性
     *
     * @param beanName Bean 名称
     * @param className 类全限定名
     * @param propertyName 注入的私有属性名
     * @param propertyValue 注入的值
     * @return 构建完毕的 Bean
     */
    public Object registerAndInjectBean(String beanName, String className, String propertyName, Object propertyValue) {
        try {
            System.out.println("   [Mini-IoC 模拟] 1. Class.forName(\"" + className + "\") 动态加载字节码");
            Class<?> clazz = Class.forName(className);

            System.out.println("   [Mini-IoC 模拟] 2. 获取无参构造器并反射创建 Bean 实例");
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object beanInstance = constructor.newInstance();

            System.out.println("   [Mini-IoC 模拟] 3. 反射获取私有字段 '" + propertyName + "' 并强制注入配置数据");
            Field targetField = clazz.getDeclaredField(propertyName);
            targetField.setAccessible(true); // 压制私有权限，模拟 Spring @Value 注入
            targetField.set(beanInstance, propertyValue);

            singletonObjects.put(beanName, beanInstance);
            System.out.println("   [Mini-IoC 模拟] 4. Bean 实例化与属性装配完成，注册进单例池！");
            return beanInstance;
        } catch (Exception e) {
            throw new RuntimeException("Mini-IoC 容器构建 Bean 失败: " + e.getMessage(), e);
        }
    }

    public Object getBean(String beanName) {
        return singletonObjects.get(beanName);
    }
}
