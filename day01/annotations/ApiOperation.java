package annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义业务接口文档注解：用于演示方法级别注解定义与运行时反射解析。
 *
 * @author InterviewGuide
 * @version 1.0
 */
@Documented
@Target(ElementType.METHOD) // 作用目标：仅能修饰方法
@Retention(RetentionPolicy.RUNTIME) // 作用域生命周期：保留至运行期，可通过反射解析
public @interface ApiOperation {

    /**
     * 接口功能描述
     *
     * @return 描述文本
     */
    String value() default "未命名的业务操作";

    /**
     * 接口负责人
     *
     * @return 负责人姓名
     */
    String author() default "架构组";
}
