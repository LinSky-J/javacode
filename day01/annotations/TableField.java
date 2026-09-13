package annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义数据库字段映射注解：用于演示属性字段级别注解定义与运行时反射解析。
 *
 * @author InterviewGuide
 * @version 1.0
 */
@Documented
@Target(ElementType.FIELD) // 作用目标：仅能修饰成员变量/字段
@Retention(RetentionPolicy.RUNTIME) // 作用域生命周期：保留至运行期
public @interface TableField {

    /**
     * 映射对应的物理数据表列名
     *
     * @return 列名
     */
    String columnName() default "";

    /**
     * 是否是主键
     *
     * @return true 表示为主键
     */
    boolean isPrimaryKey() default false;
}
