package classloading;

/**
 * ==============================================================================================
 * 面试专题：JVM 类的初始化与加载、对象创建与双亲委派原则大厂通关速记 (ClassLoadingInterviewMasterSummary)
 * ==============================================================================================
 * 本类将本次 JVM 类的初始化和加载专题中的全部核心题目进行结构化提炼，
 * 形成直击底层、大厂面试背诵金句，并作为整个 classloading 文件夹的核心导航入口。
 *
 * ==============================================================================================
 * 核心问题通关口诀与黄金回答速记
 * ==============================================================================================
 *
 * Q1: 创建对象的过程是怎样的？（new Object() 底层六大步）
 * -> 答：六步黄金流转链：
 *        ① 类加载检查：检查常量池符号引用，若未加载先触发类加载；
 *        ② 分配内存：计算对象大小，在堆中划分空间。若内存规整采用【指针碰撞】（带整理功能的收集器），
 *           若内存不规整采用【空闲列表】（CMS 标记-清除）；
 *        ③ 解决并发分配冲突：优先在各线程私有的【TLAB】（线程本地分配缓冲区）无锁快速分配；TLAB 耗尽在公共堆采用【CAS 配失败重试】；
 *        ④ 初始化零值：除对象头外，内存空间全部赋默认零值（保证属性未赋值也能直接安全使用默认值）；
 *        ⑤ 设置对象头：设置 Mark Word（哈希码、GC分代年龄、锁状态标志、偏向线程ID）+ Klass Pointer（类型指针）+ 数组长度；
 *        ⑥ 执行 <init> 构造方法：执行实例变量显式初始化、构造代码块与构造函数，完成对象装配。
 *
 * Q2: 对象的生命周期包括哪些阶段？
 * -> 答：七大生命周期阶段：
 *        ① 创建阶段（Created）：分配内存、对象头设置、执行构造方法；
 *        ② 应用阶段（In Use）：被至少一个强引用持有，正在被业务使用；
 *        ③ 不可见阶段（Invisible）：脱离作用域（如局部变量方法结束），但栈帧槽位尚未被复用；
 *        ④ 不可达阶段（Unreachable）：GC Roots 可达性分析无法到达，准备回收（避开引用计数循环引用缺陷）；
 *        ⑤ 收集阶段（Collected）：判断并执行 finalize()（若重写且未执行过，低优先级 Finalizer 线程执行一次救赎）；
 *        ⑥ 终结阶段（Finalized）：finalize 执行完毕且未复活，等待内存释放；
 *        ⑦ 释放阶段（Deallocated）：内存被回收并归还堆池。
 *        注：JDK 9 已废弃 finalize()，推荐使用 try-with-resources 或 Cleaner。
 *
 * Q3: 类的加载器有哪些？
 * -> 答：四层分层体系：
 *        ① 启动类加载器（Bootstrap ClassLoader）：C++ 实现，加载 <JAVA_HOME>/lib 或核心类库（rt.jar、java.base），在 Java 中获取为 null；
 *        ② 扩展/平台类加载器（Platform / ExtClassLoader）：Java 编写，加载 <JAVA_HOME>/lib/ext 或系统扩展目录；
 *        ③ 应用程序类加载器（AppClassLoader）：Java 编写，加载用户 ClassPath 路径上的类，是程序默认的类加载器；
 *        ④ 自定义类加载器（Custom ClassLoader）：继承 ClassLoader，重写 findClass()，用于源码加密解密、网络加载、热部署与隔离。
 *
 * Q4: Java中双亲委派 是什么？有啥用? / 双亲委派模型的作用
 * -> 答：【是什么】：
 *        类加载器收到加载请求时，首先不自己尝试加载，而是逐层委派给父加载器去加载（自底向上查缓存），
 *        只有父加载器反馈无法加载（抛 ClassNotFoundException）时，子加载器才尝试自己去加载（自顶向下试加载）。
 *        【有啥用 / 作用】：
 *        ① 沙箱安全防篡改：防止自定义恶意类（如篡改 java.lang.String）破坏系统核心 API；
 *        ② 避免类重复加载：父类加载过的类子类直接复用，保障元空间性能；
 *        ③ 保证核心类类型唯一性：全限定类名 + ClassLoader 共同决定类的唯一性，确保基础类型在全局一致；
 *        ④ 建立规范清晰的类库分工与模块化隔离边界。
 *
 * Q5: 讲一下类加载过程?
 * -> 答：类加载分为五大阶段（加载、验证、准备、解析、初始化）：
 *        ① 加载（Loading）：通过类全限定名获取二进制字节流，转化为方法区运行时数据结构，在堆中生成 java.lang.Class 镜像；
 *        ② 验证（Verification）：文件格式验证（0xCAFEBABE）、元数据验证、字节码验证、符号引用验证，确保字节流安全合规；
 *        ③ 准备（Preparation）：正式在方法区为 static 类变量分配内存并设置初始零值（注意：常量 static final 在此阶段直接赋真值）；
 *        ④ 解析（Resolution）：将常量池内的符号引用替换为直接引用（静态解析非虚方法，运行期动态链接虚方法）；
 *        ⑤ 初始化（Initialization）：执行类构造器 <clinit>() 方法（自动收集静态变量赋值与静态代码块合并执行），父类优先且线程安全同步。
 *
 * Q6: 讲一下类的加载和双亲委派原则
 * -> 答：紧密联动关系：
 *        ① 分工联动：类加载机制是类生命周期的执行引擎，而双亲委派原则是 ClassLoader 在执行【加载 Loading 阶段获取字节流】时所遵照的最高路由查找策略！
 *        ② 命名空间隔离：ClassLoader 不仅搬运字节流，还充当运行时命名空间边界。同一个 Class 文件由两个不同 ClassLoader 实例加载，
 *           在 JVM 看来是两个完全独立且类型互不兼容的类（instanceof 返回 false，强转报 ClassCastException）。
 *           双亲委派原则正是通过统一父类委派，消除了命名空间割裂与类型混乱。
 */
public class ClassLoadingInterviewMasterSummary {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("      JVM 类的初始化与加载、对象创建与双亲委派 6 问通关全景复盘       ");
        System.out.println("======================================================================");

        System.out.println("【day06/classloading 模块类结构索引】");
        System.out.println("1. ParentsDelegationModelAndClassLoaderPrinciples.java");
        System.out.println("   -> 涵盖：Java 中双亲委派是什么？有啥用？双亲委派模型的核心作用；");
        System.out.println("            ClassLoader.loadClass() 源码拆解、沙箱安全防篡改现场拦截实测。");
        System.out.println("2. ClassLoadingDeepDiveAndDelegationIntegration.java");
        System.out.println("   -> 涵盖：讲一下类加载过程？讲一下类的加载和双亲委派原则；");
        System.out.println("            类加载五步底层细节、命名空间隔离与不同 ClassLoader 类型不匹配实测。");
        System.out.println("3. ObjectCreationAndLifecycleMechanism.java");
        System.out.println("   -> 涵盖：创建对象六大步骤（类加载检查、指针碰撞/空闲列表、TLAB/CAS并发分配、零值初始化、对象头设置、<init>构造执行）；");
        System.out.println("            对象七大生命周期阶段、可达性分析与 GC Roots、finalize() 濒死救赎实测。");
        System.out.println("4. ClassLoaderHierarchyAndDelegationMechanism.java");
        System.out.println("   -> 涵盖：四层类加载器体系与运行时打印、双亲委派破坏三大场景（历史兼容、JDBC SPI 线程上下文类加载器、Tomcat 隔离）。");
        System.out.println("5. ClassLoadingAndInitializationProcess.java");
        System.out.println("   -> 涵盖：类生命周期七大阶段、主动引用 6 大触发初始化场景 vs 被动引用 3 大陷阱、<clinit> 与 <init> 父子类执行顺序实测。");

        System.out.println("\n======================================================================");
        System.out.println("          类的初始化和加载全部题目已整理于 day06/classloading 文件夹   ");
        System.out.println("======================================================================");
    }
}
