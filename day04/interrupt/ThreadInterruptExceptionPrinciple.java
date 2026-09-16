package interrupt;

/**
 * 调用 interrupt() 是如何让线程抛出异常的？底层原理深度剖析
 *
 * 面试原题：
 * - 调用 interrupt 是如何让线程抛出异常的？
 *
 * 核心考点：
 * 1. 认知纠偏：interrupt() 本身并不直接抛出异常，它只是设置了一个中断标志位 (interrupt status = true)
 * 2. 阻塞方法 (sleep/wait/join) 的底层源码机制（HotSpot os::interrupt、ParkEvent::unpark）
 * 3. 阻塞恢复检查：native 方法唤醒后检测中断位 -> 清除标志位 (重置为 false) -> 构造抛出 InterruptedException
 * 4. 关键陷阱与大厂避坑：InterruptedException 被捕获后中断位被擦除，为何必须重新设置中断 (恢复中断)？
 */
public class ThreadInterruptExceptionPrinciple {

    private static final Object SYNC_OBJECT = new Object();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("【面试核心答辩】调用 interrupt() 是如何让线程抛出异常的？");
        System.out.println("================================================================================");
        explainHotSpotInterruptPrinciple();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战验证 1】线程处于 sleep 阻塞时响应 interrupt() 抛出异常与标志位清除过程");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateSleepInterruptionAndFlagClear();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【实战验证 2】如果在非阻塞纯计算状态下调用 interrupt() 会抛出异常吗？");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateComputationThreadInterruption();

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("【大厂必考最佳实践】捕获 InterruptedException 后的中断状态恢复范式");
        System.out.println("--------------------------------------------------------------------------------");
        explainInterruptPreservationPractice();
    }

    /**
     * JVM HotSpot 源码级机制深度讲解
     */
    public static void explainHotSpotInterruptPrinciple() {
        System.out.println("面试官常问：'调用 thread.interrupt()，为什么会让休眠的线程抛出 InterruptedException？'");
        System.out.println("JVM 内部底层执行全链路剖析：");
        System.out.println("1. 纠正误解：");
        System.out.println("   - interrupt() 绝不是直接把一个异常强行注入到目标线程的栈帧中！");
        System.out.println("   - 其本身只是一个协作信号，负责将目标线程内部的 boolean 变量（中断标志位）标记为 true。");
        System.out.println("2. 阻塞状态下的唤醒与抛异常三部曲（以 sleep 为例）：");
        System.out.println("   (1) 第一步：底层阻塞挂起");
        System.out.println("       * 线程调用 Thread.sleep() / Object.wait() 时，JVM 底层（jvm.cpp、os_linux.cpp 等）");
        System.out.println("         会调用操作系统的底层函数（如 pthread_cond_timedwait 或 ParkEvent::park()），");
        System.out.println("         操作系统将该线程移出 CPU 调度队列，放入休眠等待队列挂起。");
        System.out.println("   (2) 第二步：唤醒并修改中断位");
        System.out.println("       * 另一个线程调用 targetThread.interrupt() 时，HotSpot 执行 Thread::interrupt() -> os::interrupt()；");
        System.out.println("       * JVM 将该线程的 _interrupted 标志位设为 1（true）；");
        System.out.println("       * 【最核心的一步】：JVM 调用该线程关联的 ParkEvent::unpark() 或发送信号，");
        System.out.println("         强行将处于操作系统挂起等待队列中的线程【提前唤醒】！重新恢复为可被 CPU 调度的就绪状态！");
        System.out.println("   (3) 第三步：Native 检查、清除标志位并抛出异常");
        System.out.println("       * 被唤醒的 sleep() 本地方法继续向下执行，首先检测自身的中断标志位（check_interrupted(true)）；");
        System.out.println("       * 发现标志位为 true，JVM 会立即执行【清除中断标志位】操作（将其复位为 0/false）；");
        System.out.println("       * 随后在 JVM 内部创建 java/lang/InterruptedException 异常对象，并抛出给当前 Java 调用栈！");
    }

    /**
     * 实战演示：sleep 阻塞中响应中断、抛出异常及标志位清除
     */
    public static void demonstrateSleepInterruptionAndFlagClear() throws InterruptedException {
        Thread worker = new Thread(() -> {
            System.out.println("   [Worker] 准备执行 Thread.sleep(5000) 进入深度睡眠...");
            try {
                Thread.sleep(5000);
                System.out.println("   [Worker] 睡满 5 秒正常苏醒（不应打印）");
            } catch (InterruptedException e) {
                System.out.println("   [Worker 捕获异常] 成功捕获到: " + e.getClass().getName());
                // 重点验证：抛出异常后，当前线程的中断标志位是否被自动清除了？
                boolean interruptedAfterException = Thread.currentThread().isInterrupted();
                System.out.printf("   [Worker 关键探测] 抛出异常后，当前线程 isInterrupted() 状态 = %b (预期: false)\n",
                        interruptedAfterException);
                System.out.println("   -> 证明：JVM 在抛出 InterruptedException 前，严格遵守契约将中断标志位复位清除了！");
            }
        }, "Interrupt-Demo-Worker");

        worker.start();
        Thread.sleep(200); // 确保子线程已经进入 sleep 阻塞

        System.out.println("   [MainThread] 调用 worker.interrupt()...");
        worker.interrupt();
        worker.join();
    }

    /**
     * 实战演示：非阻塞纯计算线程调用 interrupt 不会抛出异常
     */
    public static void demonstrateComputationThreadInterruption() throws InterruptedException {
        Thread calcThread = new Thread(() -> {
            long sum = 0;
            // 纯 CPU 密集型循环计算
            while (sum < 100_000_000L) {
                sum++;
                // 如果没有手动检查 isInterrupted()，线程将继续执行，完全不会凭空抛出任何异常！
                if (sum % 50_000_000 == 0) {
                    System.out.printf("   [CalcThread] 进度: %d, 当前中断标志位: %b\n",
                            sum, Thread.currentThread().isInterrupted());
                }
            }
            System.out.println("   [CalcThread] 循环顺利跑完，并未抛出任何异常！");
        }, "Calc-Worker");

        calcThread.start();
        Thread.sleep(10);
        System.out.println("   [MainThread] 对纯计算线程调用 calcThread.interrupt()...");
        calcThread.interrupt();
        calcThread.join();
        System.out.println("   -> 结论证实：只有调用了能够感知中断的阻塞方法（如 sleep/wait/join）才会抛出异常；");
        System.out.println("      普通计算代码必须手动调用 isInterrupted() 或 Thread.interrupted() 轮询中断标志！");
    }

    /**
     * 捕获异常后的恢复中断最佳实践
     */
    public static void explainInterruptPreservationPractice() {
        System.out.println("【大厂阿里巴巴规范必考陷阱】：为什么不能在 catch (InterruptedException) 里什么都不做？");
        System.out.println("原因：由于 JVM 抛出异常时清除了中断位（置为 false），如果上层调用者或线程池依赖该线程的中断状态，");
        System.out.println("      吞掉异常将导致整个系统丢失中断信号，线程无法正常停止！");
        System.out.println("标准处理范式：");
        System.out.println("   try {");
        System.out.println("       Thread.sleep(1000);");
        System.out.println("   } catch (InterruptedException e) {");
        System.out.println("       // 方案 1：向外继续抛出异常让调用方处理");
        System.out.println("       // 方案 2：重新设置中断位恢复状态！");
        System.out.println("       Thread.currentThread().interrupt(); // 关键！将标志位重新置为 true");
        System.out.println("       // 然后执行资源收尾逻辑并退出...");
        System.out.println("   }");
    }
}
