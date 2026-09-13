package keywords.statickeyword;

/**
 * 容器宿主类：专门用于演示 static 修饰内部类（静态内部类 / 静态嵌套类）的机制。
 *
 * 核心考点：
 * 1. 语法定位：
 *    - static 只能修饰成员内部类，不能修饰顶级外部类（外部类只有 public 或 package-private 默认包访问权限）。
 * 2. 核心区别：
 *    - 非静态内部类：强行持有外部宿主对象的隐式引用（this$0），必须依赖外部对象存在（outer.new Inner()）。
 *    - 静态内部类：使用 static 修饰，【完全不持有外部宿主对象的隐式引用】！
 *      可以直接通过 new OuterContainer.StaticComponent() 独立创建，脱离外部对象生命周期。
 * 3. 生产防坑与内存泄漏：
 *    - 在 Android Handler 或高并发连接会话中，非静态内部类极易导致外部宿主无法被 GC 回收从而引发内存泄露。
 *    - 阿里巴巴 Java 规约强制要求：若内部类不需要访问外部类的非静态属性，必须定义为 static 静态内部类！
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class OuterContainer {

    private String containerName;
    private static String globalEnvironment = "Production-Cluster-A";

    public OuterContainer(String containerName) {
        this.containerName = containerName;
    }

    /**
     * 静态内部类（Static Nested Class）：独立自治组件
     */
    public static class StaticComponent {
        private String componentName;

        public StaticComponent(String componentName) {
            this.componentName = componentName;
        }

        public void printInfo() {
            // 可以直接读取外部类的静态成员
            System.out.println("   [静态内部类] 组件名称: " + componentName + "，读取外部静态环境: " + globalEnvironment);
            // 无法直接访问外部类的非静态成员 containerName（因为没有 this$0 外部宿主对象）
        }
    }

    public String getContainerName() {
        return containerName;
    }
}
