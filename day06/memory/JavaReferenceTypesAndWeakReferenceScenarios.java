package memory;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Java 四大引用类型深度剖析、回收时机对比与弱引用企业级应用实战
 *
 * 涵盖面试核心题目：
 * 1. 引用类型有哪些？有什么区别?
 * 2. 弱引用了解吗?举例说明在哪里可以用?
 */
public class JavaReferenceTypesAndWeakReferenceScenarios {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          Java 四种引用类型、生命周期与弱引用实战全景解析             ");
        System.out.println("======================================================================");

        explainFourReferenceTypes();
        demonstrateWeakReferenceLifecycle();
        explainWeakReferenceRealWorldScenarios();

        System.out.println("======================================================================");
        System.out.println("                 引用类型与弱引用实战解析完成                        ");
        System.out.println("======================================================================");
    }

    /**
     * 问题 1：引用类型有哪些？有什么区别?
     *
     * 强引用（Strong）、软引用（Soft）、弱引用（Weak）、虚引用（Phantom）。
     */
    public static void explainFourReferenceTypes() {
        System.out.println("\n--- 1. Java 四大引用类型核心区别对比 ---");
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println("引用类型        | 对应类名             | 垃圾回收（GC）时机       | 是否报 OOM | 典型应用场景");
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println("强引用 (Strong) | 默认直接赋值 Object  | 只要强引用存在，绝不回收 | 宁死不屈   | 99% 的日常业务对象创建");
        System.out.println("软引用 (Soft)   | SoftReference        | 内存不足即将 OOM 前回收  | 极大减少   | 内存敏感型高速缓存（图片/网页缓存）");
        System.out.println("弱引用 (Weak)   | WeakReference        | 只要触发 GC 发现即回收   | 不会触发   | ThreadLocalMap、WeakHashMap、缓存映射");
        System.out.println("虚引用 (Phantom)| PhantomReference     | 随时可能回收，get() 为空 | 无直接关系 | 配合 ReferenceQueue 跟踪堆外内存释放");
        System.out.println("---------------------------------------------------------------------------------------------");

        System.out.println("\n四种引用的底层生命周期机制详解：");
        System.out.println("1. 强引用（Strong Reference）：");
        System.out.println("   - 如：Object obj = new Object();");
        System.out.println("   - 只要 GC Roots 能通过强引用链可达该对象，哪怕 JVM 内存告急即将崩溃，GC 也绝对不会回收它。");
        System.out.println("2. 软引用（Soft Reference）：");
        System.out.println("   - 如：SoftReference<byte[]> softRef = new SoftReference<>(new byte[1024]);");
        System.out.println("   - 当内存充足时，不会被 GC 回收；只有当系统内存严重告急，GC 发现回收完年轻代仍不够时，才会将软引用对象回收。");
        System.out.println("3. 弱引用（Weak Reference）：");
        System.out.println("   - 如：WeakReference<byte[]> weakRef = new WeakReference<>(new byte[1024]);");
        System.out.println("   - 只要发生垃圾回收（无论 Minor GC 还是 Full GC），只要该对象仅被弱引用关联，就会立即被回收。");
        System.out.println("4. 虚引用（Phantom Reference / 幽灵引用）：");
        System.out.println("   - 如：PhantomReference<Object> phantomRef = new PhantomReference<>(obj, queue);");
        System.out.println("   - 调用 phantomRef.get() 永远返回 null！它的存在完全不影响对象的生命周期。");
        System.out.println("   - 唯一作用：当该对象被 GC 回收时，JVM 会将虚引用加入到关联的 ReferenceQueue 引用队列中，以便通知程序执行后置清理（如 Netty DirectByteBuffer 的堆外内存销毁）。");
    }

    /**
     * 实测弱引用的生命周期演进：GC 前后状态比对
     */
    public static void demonstrateWeakReferenceLifecycle() {
        System.out.println("\n--- 2. [代码实测] 弱引用生命周期与 GC 现场联动验证 ---");

        // 创建一个普通强引用对象
        String payload = new String("High-Value-Data-Payload");
        WeakReference<String> weakRef = new WeakReference<>(payload);

        System.out.println("1. 外部保持强引用时:");
        System.out.println("   weakRef.get() = " + weakRef.get() + " (此时强引用依然存在，对象不会被回收)");

        // 断开外部强引用，此时只剩弱引用指向该对象
        payload = null;

        System.out.println("2. 断开外部强引用，未触发 GC 时:");
        System.out.println("   weakRef.get() = " + weakRef.get() + " (对象仍在堆中暂未被回收)");

        // 手动请求 JVM 进行垃圾回收
        System.gc();

        // 稍作等待确保 GC 线程完成扫描与清理
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}

        System.out.println("3. 执行 System.gc() 垃圾回收之后:");
        System.out.println("   weakRef.get() = " + weakRef.get() + " (已变成 null，证实只被弱引用关联的对象在 GC 时必定被回收！)");
    }

    /**
     * 问题 2：弱引用了解吗?举例说明在哪里可以用?
     *
     * 经典场景一：ThreadLocal 中的 ThreadLocalMap.Entry 继承 WeakReference。
     * 经典场景二：WeakHashMap 自动清理过期 Key 缓存。
     * 经典场景三：LeakCanary 内存泄漏监控框架。
     */
    public static void explainWeakReferenceRealWorldScenarios() {
        System.out.println("\n--- 3. 弱引用的三大企业级实战应用场景 ---");

        System.out.println("场景一：ThreadLocal 底层 ThreadLocalMap.Entry 的防泄漏设计");
        System.out.println("   1. 结构剖析：ThreadLocal.ThreadLocalMap 的底层 Entry 继承自 WeakReference<ThreadLocal<?>>：");
        System.out.println("      static class Entry extends WeakReference<ThreadLocal<?>> { Object value; }");
        System.out.println("   2. 为什么要使用弱引用作为 Key？");
        System.out.println("      - 假设 Entry 的 Key 为强引用：当业务代码将 ThreadLocal 变量设为 null（外部强引用断开）时，");
        System.out.println("        当前线程的 threadLocals 属性依然持有 ThreadLocalMap，Entry 依然强引用着 ThreadLocal 对象，导致 ThreadLocal 永远无法被回收！");
        System.out.println("      - 采用弱引用作为 Key：当外部没有强引用时，下一次 GC 会自动将 Entry 中的 Key 回收为 null！");
        System.out.println("   3. 极重要面试陷阱：弱引用解决了 Key 的泄漏，但无法解决 Value 的泄漏！");
        System.out.println("      - 当 Key 变成 null 后，Entry.value 依然被当前 Thread 强引用（Thread -> ThreadLocalMap -> Entry -> Value）；");
        System.out.println("      - 如果线程长期存活在线程池中，Value 对象将永远得不到回收，从而引发内存泄漏！");
        System.out.println("      - 黄金解决方案：每次使用完 ThreadLocal 之后，必须显式调用 threadLocal.remove() 手动清理！");

        System.out.println("\n场景二：WeakHashMap 自动释放无用缓存映射（实测展示）");
        Map<String, String> weakMap = new WeakHashMap<>();
        String key1 = new String("TEMP_SESSION_TOKEN_1001");
        String key2 = new String("TEMP_SESSION_TOKEN_1002");

        weakMap.put(key1, "UserSessionMetadata_Alice");
        weakMap.put(key2, "UserSessionMetadata_Bob");

        System.out.println("   [WeakHashMap 存入前] size = " + weakMap.size());

        // 模拟外部请求结束，key1 强引用销毁，key2 依然保持强引用
        key1 = null;

        // 触发 GC
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}

        // 再次访问 WeakHashMap 会触发 expungeStaleEntries 清理无用 Entry
        System.out.println("   [触发 GC 后的 WeakHashMap] size = " + weakMap.size());
        System.out.println("   内容: " + weakMap + " (失去强引用的 key1 被自动清理，保留了仍有强引用的 key2)");

        System.out.println("\n场景三：Android / 服务端 LeakCanary 内存泄漏监控工具");
        System.out.println("   - 原理：当一个对象（如 Activity、大型上下文）执行 onDestroy 声明周期结束时，");
        System.out.println("     将该对象包装在带有 ReferenceQueue 的 WeakReference 中；");
        System.out.println("   - 触发一次后台 GC 后，检查 ReferenceQueue 中是否存在该弱引用：");
        System.out.println("     a. 若存在：说明对象已被垃圾回收器正常释放；");
        System.out.println("     b. 若不存在：说明该对象依然被某些静态集合或长生命周期对象强引用着，立即触发 Heap Dump 生成泄漏分析报告！");
    }
}
