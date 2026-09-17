package classloading;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * 对象创建完整流转机制与对象生命周期全景深度解析
 *
 * 涵盖面试核心题目：
 * 1. 创建对象的过程
 * 2. 对象的生命周期
 */
public class ObjectCreationAndLifecycleMechanism {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          Java 对象创建完整六步流转与对象全生命周期深度实测           ");
        System.out.println("======================================================================");

        explainObjectCreationProcess();
        explainObjectLifecycleStages();
        demonstrateFinalizeResurrectionMechanism();

        System.out.println("======================================================================");
        System.out.println("                 对象创建与生命周期解析完成                          ");
        System.out.println("======================================================================");
    }

    /**
     * 问题 1：创建对象的过程（new Object() 底层深度解剖）
     *
     * 核心步骤：
     * 步骤一：类加载检查（Class Loading Check）
     * 步骤二：分配堆内存（Memory Allocation - 指针碰撞 vs 空闲列表）
     * 步骤三：解决并发分配冲突（Concurrency Safety - TLAB vs CAS）
     * 步骤四：初始化零值（Zero-initialization）
     * 步骤五：设置对象头（Object Header - Mark Word + Klass Word）
     * 步骤六：执行 <init> 构造方法（Execution of Constructor）
     */
    public static void explainObjectCreationProcess() {
        System.out.println("\n--- 1. 创建对象的六大底层流转步骤（new 关键字字节码级透视） ---");

        System.out.println("步骤一：类加载检查（Class Loading Check）");
        System.out.println("   - 当虚拟机遇到一条 new 字节码指令时，首先去运行时常量池中检查能否定位到该类的符号引用；");
        System.out.println("   - 检查该符号引用所代表的类是否已被加载、解析和初始化（Class.forName / ClassLoader）；");
        System.out.println("   - 如果没有，必须先执行相应的【类加载过程】（加载 -> 验证 -> 准备 -> 解析 -> 初始化）。");

        System.out.println("\n步骤二：为新生对象分配堆内存（Memory Allocation）");
        System.out.println("   - 对象所需内存大小在类加载完成后便可完全确定（对象头 + 实例字段数据 + 对齐填充字节）。");
        System.out.println("   - 在 Java 堆中划分内存的两种主要方式（取决于堆内存是否规整，而堆是否规整由所采用的垃圾收集器决定）：");
        System.out.println("     1) 指针碰撞（Bump the Pointer）：");
        System.out.println("        - 适用场景：堆内存绝对规整（用过的在左边，空闲的在右边，中间有指针作为分界点）。");
        System.out.println("        - 分配方式：指针向空闲空间方向挪动一段与对象大小相等的距离。");
        System.out.println("        - 配合算法：带内存压缩/整理功能的收集器（如 Serial、ParNew、G1 局部 Region）。");
        System.out.println("     2) 空闲列表（Free List）：");
        System.out.println("        - 适用场景：堆内存不规整（已使用与空闲内存交错分布）。");
        System.out.println("        - 分配方式：JVM 维护一个空闲内存块列表，分配时从列表中找出一块足够大的空间划分给对象，并更新列表记录。");
        System.out.println("        - 配合算法：基于标记-清除（Mark-Sweep）算法的收集器（如 CMS）。");

        System.out.println("\n步骤三：解决并发分配竞争安全（Concurrency Safety）");
        System.out.println("   - 对象创建在多线程高并发环境下极其频繁，单纯修改指针在并发下是不安全的。解决方案：");
        System.out.println("     1) TLAB（Thread Local Allocation Buffer，线程本地分配缓冲区 - 首选方案）：");
        System.out.println("        - JVM 在 Eden 区为每个线程预先分配一小块独立的私有内存缓冲区（通常占 Eden 1%）；");
        System.out.println("        - 每个线程分配对象时优先在自己的 TLAB 上分配，使用指针碰撞，无锁化、零线程竞争；");
        System.out.println("        - 只有在 TLAB 空间耗尽或分配大对象时，才需要同步锁定并申请新的 TLAB。");
        System.out.println("     2) CAS 配失败重试（比较并交换机制）：");
        System.out.println("        - 当在公共堆空间分配时，采用 CAS 乐观并发机制加上失败重试，保证更新操作的原子性。");

        System.out.println("\n步骤四：初始化零值（Zero-initialization）");
        System.out.println("   - 内存分配完成后，JVM 将分配到的内存空间（不包括对象头）都初始化为零值（0、false、null 等）；");
        System.out.println("   - 核心保障：这步操作保证了对象的实例字段在 Java 代码中即使没有显式赋予初始值，也可以直接安全使用默认默认值。");

        System.out.println("\n步骤五：设置对象头（Object Header 设置）");
        System.out.println("   - JVM 对对象进行必要的设置，将关键元数据填入对象头（Object Header）：");
        System.out.println("     1) Mark Word（标记字，64 位系统占 8 字节）：存储对象的 HashCode、GC 分代年龄（4 位）、锁状态标识（无锁/偏向锁/轻量级锁/重量级锁）、偏向线程 ID；");
        System.out.println("     2) Klass Word（类型指针）：指向该对象类元数据的指针，JVM 通过该指针确定该对象是哪个类的实例；");
        System.out.println("     3) 数组长度（Array Length，仅数组对象有）：若为数组对象，额外记录数组长度（4 字节）。");

        System.out.println("\n步骤六：执行 <init> 构造方法（程序员视角的对象装配）");
        System.out.println("   - 上述五步完成后，从 JVM 虚拟机视角来看，一个新的对象已经产生了；");
        System.out.println("   - 但从 Java 程序视角来看，对象创建才刚开始：执行字节码 invokespecial 指令，");
        System.out.println("     按照程序员的意愿执行【实例变量显式初始化】、【构造代码块】和【构造函数】，完成真正的初始化装配！");
    }

    /**
     * 问题 2：对象的生命周期（从降生到灰飞烟灭）
     *
     * 阶段：创建阶段 -> 应用阶段 -> 不可见阶段 -> 不可达阶段 -> 收集阶段(finalize) -> 终结阶段 -> 释放阶段。
     * 回收判定：引用计数法 vs 可达性分析法（GC Roots）。
     */
    public static void explainObjectLifecycleStages() {
        System.out.println("\n--- 2. Java 对象的七大生命周期阶段 ---");
        System.out.println("阶段一：创建阶段（Created）");
        System.out.println("   - 为对象分配堆存储空间、初始化成员属性、执行构造方法。");
        System.out.println("阶段二：应用阶段（In Use）");
        System.out.println("   - 对象被至少一个强引用（Strong Reference）持有，处于存活并正在被业务调用的有效状态。");
        System.out.println("阶段三：不可见阶段（Invisible）");
        System.out.println("   - 程序的执行已经超出了该对象的有效作用域（如局部变量方法执行完毕），");
        System.out.println("     但在 JVM 栈帧的局部变量表中可能仍有槽位尚未被后续变量复用，物理上仍保留指针，但逻辑上对程序已不可见。");
        System.out.println("阶段四：不可达阶段（Unreachable）");
        System.out.println("   - GC Roots 经【可达性分析算法（Reachability Analysis）】遍历后，没有任何强引用链能够到达该对象。");
        System.out.println("   - [核心面试考点] 为什么不采用引用计数法？因为引用计数法无法解决对象之间【相互循环引用】（A 持有 B，B 持有 A）的致命缺陷！");
        System.out.println("   - [GC Roots 包含哪些？] 虚拟机栈局部变量、方法区类静态属性、方法区常量池引用、本地方法栈 JNI 引用、JVM 内部系统核心引用。");
        System.out.println("阶段五：收集阶段（Collected - 判定 finalize）");
        System.out.println("   - 垃圾收集器发现对象不可达后，判断对象是否有必要执行 finalize() 方法：");
        System.out.println("     a. 若对象没有重写 finalize()，或者 finalize() 已经被 JVM 调用过：直接判定为没有必要执行；");
        System.out.println("     b. 若对象重写了 finalize() 且尚未被调用：对象被放入 F-Queue 队列中，由一条低优先级的 Finalizer 线程去触发执行。");
        System.out.println("阶段六：终结阶段（Finalized）");
        System.out.println("   - 对象的 finalize() 方法执行完毕后，对象仍未被重新拯救（没有搭上 GC Roots），该对象进入终结状态，静候回收。");
        System.out.println("阶段七：释放阶段（Deallocated）");
        System.out.println("   - 垃圾收集器正式回收该对象所占用的内存空间，对象从堆内存中被抹除，空间归还堆内存池。");
    }

    // 静态变量保存自我救赎对象的强引用
    public static ResurrectableObject saviorHook = null;

    /**
     * 演示 finalize() 自我救赎机制（以及只能救赎一次的特性）
     */
    public static void demonstrateFinalizeResurrectionMechanism() {
        System.out.println("\n--- 3. [现场实测] 对象的濒死自我救赎（finalize 机制与唯一性） ---");

        saviorHook = new ResurrectableObject("Hero-Object-001");

        System.out.println("1. 首次将强引用断开（saviorHook = null）:");
        saviorHook = null;
        System.gc(); // 触发 GC，此时 JVM 会调用 finalize()

        try {
            // Finalizer 线程优先级很低，暂停 500ms 等待其执行
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        if (saviorHook != null) {
            System.out.println("   [救赎成功] 对象在 finalize() 中重新挂载到了静态 GC Roots: " + saviorHook);
        } else {
            System.out.println("   [救赎失败] 对象已被回收。");
        }

        System.out.println("\n2. 第二次断开强引用并触发 GC:");
        saviorHook = null;
        System.gc();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        if (saviorHook != null) {
            System.out.println("   [救赎成功] 对象依然存活: " + saviorHook);
        } else {
            System.out.println("   [救赎失败] saviorHook 为 null，证实对象的 finalize() 只能被 JVM 自动执行一次！");
        }

        System.out.println("\n[企业级最佳实践警告] 现代 Java（JDK 9+）已明确废弃 finalize() 方法！");
        System.out.println("   - 缺点：执行时间不可控、效率极其低下、会严重阻碍垃圾收集器的并发回收性能；");
        System.out.println("   - 现代替代方案：推荐使用 try-with-resources（AutoCloseable）或 java.lang.ref.Cleaner 进行资源清理。");
    }

    /**
     * 辅助类：演示自我救赎的实体
     */
    static class ResurrectableObject {
        private final String name;

        public ResurrectableObject(String name) {
            this.name = name;
        }

        @Override
        @SuppressWarnings("deprecation")
        protected void finalize() throws Throwable {
            super.finalize();
            System.out.println("   -> ResurrectableObject.finalize() 被 Finalizer 线程调用，正在执行濒死自我救赎...");
            // 重新挂载强引用到 GC Roots 上
            ObjectCreationAndLifecycleMechanism.saviorHook = this;
        }

        @Override
        public String toString() {
            return "ResurrectableObject{" + name + "}";
        }
    }
}
