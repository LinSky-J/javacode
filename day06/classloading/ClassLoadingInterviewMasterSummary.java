package classloading;

/**
 * ==============================================================================================
 * 面试专题：JVM 类的初始化与加载、对象创建与 ClassLoader 大厂通关速记 (ClassLoadingInterviewMasterSummary)
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
 * Q4: 什么是双亲委派机制？为什么需要它？
 * -> 答：工作机制：自底向上委托检查缓存，自顶向下尝试加载。收到请求先委托给父加载器，父加载器找不到才由子加载器尝试加载。
 *        三大目的：
 *        ① 沙箱安全防篡改：防止自定义同名恶意类（如恶意 java.lang.String）篡改核心 API；
 *        ② 避免类重复加载：父加载器加载过则无需重复加载，节约元空间；
 *        ③ 保证类型唯一性：同一个类必须由同一个 ClassLoader 加载才在 JVM 中被视为相同类型。
 *
 * Q5: 有哪些破坏双亲委派的场景？
 * -> 答：三大经典场景：
 *        ① 历史包袱：JDK 1.2 之前已有 ClassLoader，通过重写 loadClass() 自定义加载；JDK 1.2 起规范推荐重写 findClass()；
 *        ② 核心类调用用户代码（SPI 机制）：如 JDBC DriverManager，rt.jar 中的接口由 Bootstrap 加载，但 MySQL 实现类在 ClassPath，
 *           通过【线程上下文类加载器（Thread Context ClassLoader）】反向委派子类加载器加载；
 *        ③ Web 容器隔离与热部署：Tomcat 的 WebappClassLoader 优先加载自身 WEB-INF 目录下的类，实现不同应用间同名不同版本 jar 包彻底物理隔离。
 *
 * Q6: 类的完整生命周期与初始化时机是怎样的？
 * -> 答：七大阶段：加载 -> 验证 -> 准备（类变量赋初始零值，常量赋最终值）-> 解析（符号引用转直接引用）-> 初始化（执行 <clinit>）-> 使用 -> 卸载。
 *        执行顺序：父类静态代码块/属性 -> 子类静态代码块/属性 -> 父类构造代码块/构造函数 -> 子类构造代码块/构造函数。
 *        被动引用不触发初始化：通过子类引用父类静态字段、定义对象数组、引用 static final 编译期常量。
 */
public class ClassLoadingInterviewMasterSummary {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("      JVM 类的初始化与加载、对象创建与类加载器 6 问通关全景复盘       ");
        System.out.println("======================================================================");

        System.out.println("【day06/classloading 模块类结构索引】");
        System.out.println("1. ObjectCreationAndLifecycleMechanism.java");
        System.out.println("   -> 涵盖：创建对象六大步骤（类加载检查、指针碰撞/空闲列表、TLAB/CAS并发分配、零值初始化、对象头Mark Word/Klass Word、<init>执行）；");
        System.out.println("            对象七大生命周期阶段、可达性分析与GC Roots、finalize() 濒死救赎实测与弃用原因。");
        System.out.println("2. ClassLoaderHierarchyAndDelegationMechanism.java");
        System.out.println("   -> 涵盖：Bootstrap、Platform/Ext、App、Custom 四层类加载器体系、运行时委托树探查；");
        System.out.println("            双亲委派机制底层两阶段工作原理、三大设计目的；");
        System.out.println("            破坏双亲委派的三大经典场景（JDK 历史重写 loadClass、JDBC SPI 线程上下文类加载器反向委派、Tomcat WebappClassLoader 隔离）。");
        System.out.println("3. ClassLoadingAndInitializationProcess.java");
        System.out.println("   -> 涵盖：类生命周期七大阶段（加载、验证、准备、解析、初始化、使用、卸载）；");
        System.out.println("            准备阶段赋零值 vs 编译期常量赋真值；");
        System.out.println("            主动引用 6 大触发初始化场景 vs 被动引用 3 大不触发陷阱；");
        System.out.println("            <clinit> 类构造器与 <init> 实例构造器父子类嵌套执行顺序现场实测。");

        System.out.println("\n======================================================================");
        System.out.println("          类的初始化和加载全部题目已整理于 day06/classloading 文件夹   ");
        System.out.println("======================================================================");
    }
}
