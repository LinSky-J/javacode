package other;

/**
 * Native 方法核心机制与深度解析
 *
 * 核心考点：
 * 1. 什么是 Native 方法？
 *    - native 是 Java 关键字，用于修饰方法，表示该方法的具体实现在外部由非 Java 语言（如 C、C++、汇编）编写。
 *    - 只有方法声明，没有方法体，以分号结尾。
 * 2. 交互桥梁：JNI（Java Native Interface，Java 本地接口）
 *    - JNI 是 JVM 规范定义的一套标准协议，允许 Java 代码与本地编写的代码/库相互调用。
 * 3. 为什么需要 Native 方法？
 *    - 硬件控制与特权指令：Java 无法直接操作底层寄存器、中断向量、驱动设备。
 *    - 极限性能：图像处理、音视频编解码、高频量化矩阵运算等，C/C++ 原生优化更加极致。
 *    - 遗留 C/C++ 资产复用：如操作系统驱动、OpenCV、加密芯片等库。
 *    - JVM 自身运行基石：线程调度、内存分配、对象哈希、系统时间均依赖 Native 方法。
 * 4. 两种注册机制：
 *    - 静态注册：根据标准命名规则解析，格式为 Java_全限定包名_类名_方法名。
 *    - 动态注册：通过 C/C++ 的 JNI_OnLoad 函数，在加载时调用 (*env)->RegisterNatives 动态绑定函数指针。
 * 5. 风险与代价：
 *    - 破坏跨平台性（不同操作系统需单独编译 .dll / .so）。
 *    - 失去 JVM 安全沙箱与垃圾回收保护，C 内存未释放将导致系统级内存泄漏，段错误（Segfault）将直接崩溃整个 JVM 进程。
 */
public class NativeMethodDemo {

    /**
     * 模拟声明一个自定义 Native 方法
     * 注意：该方法若未通过 System.loadLibrary() 加载对应的 dll/so 并在底层实现，
     * 调用时将抛出 java.lang.UnsatisfiedLinkError。
     */
    public native void triggerHardwareInterrupt();

    /**
     * 展示 JDK 内部常见的核心 Native 方法
     */
    public static void displayJdkNativeExamples() {
        System.out.println("--- JDK 内部经典 Native 方法剖析 ---");

        // 1. System.currentTimeMillis() 底层调用操作系统 gettimeofday / GetSystemTimeAsFileTime
        long now = System.currentTimeMillis();
        System.out.println("1. System.currentTimeMillis() [Native] 获取系统时钟: " + now);

        // 2. Thread.currentThread() 底层调用当前 OS 线程句柄
        Thread currentThread = Thread.currentThread();
        System.out.println("2. Thread.currentThread() [Native] 获取当前线程: " + currentThread.getName());

        // 3. Object.hashCode() 默认的 IdentityHashCode 由 JVM C++ 代码计算（偏向锁/MarkWord/随机种子）
        String sample = new String("native-sample");
        int identityHash = System.identityHashCode(sample);
        System.out.println("3. System.identityHashCode() [Native] 获取默认哈希码: " + identityHash);

        // 4. System.arraycopy() 内存连续块快速复制，底层调用 C 的 memmove / memcpy
        int[] src = {1, 2, 3, 4, 5};
        int[] dest = new int[5];
        System.arraycopy(src, 0, dest, 0, src.length);
        System.out.println("4. System.arraycopy() [Native] 内存块高速复制完成，目标首元素: " + dest[0]);
    }
}
