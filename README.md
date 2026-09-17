# Java Interview Master (Java 高频核心面试题全景深度解析与实战项目)

[English Version Below](#english-version) | [中文版本](#中文版本)

---

<a name="中文版本"></a>
# 中文版本

## 一、项目概述

本项目是一个专注于 **Java 核心技术、高并发编程、集合框架底层原理与 JVM 深度剖析** 的工程化知识库与面试突击实战代码集。

项目秉承 **“问题导向、源码剖析、实战验证、无死角覆盖”** 的原则，将常见的一线大厂高频面试题转化为结构严谨、可直接运行验证的 Java 源码模块。全工程包含 150+ 个核心类文件，涵盖 Java 语言基础、Java 8/21 新特性、集合容器源码实现、多线程与 JMM 内存模型、AQS/CAS 并发安全锁机制、线程池工程化配置以及 JVM 运行时数据区、类加载双亲委派与垃圾回收（GC）等六大核心阶段。

---

## 二、知识体系与模块全览

项目按专题分为六大递进模块（`day01` - `day06`）：

### Day 01: Java 语言核心基础与面向对象
深入拆解 Java 语言底层的运行机制、数据类型、语法糖与设计哲学：
- **`basics/`**: Java 核心特性、优缺点剖析、Java 与 Python 全维度对比。
- **`execution/`**: 编译型与解释型语言异同、Java“一次编写，到处运行”的跨平台底层实现原理。
- **`jvm/`**: JVM、JRE、JDK 三者关系与系统架构图谱。
- **`datatypes/`**: 基本数据类型、自动拆装箱底层实现与 IntegerCache 缓存陷阱。
- **`parameters/`**: Java 核心参数传递机制（值传递本质与内存对象图谱）。
- **`oop/`**: 面向对象三大特性（封装、继承、多态）、重写与重载本质、抽象类与接口深度对比、静态嵌套类与非静态内部类。
- **`keywords/`**: `static` 与 `final` 关键字底层内存布局与使用规范。
- **`strings/`**: String、StringBuilder 与 StringBuffer 源码对比、不可变性与字符数组复用。
- **`objects/`**: Object 类核心方法解析、`equals` 与 `hashCode` 契约规范与 Hash 碰撞危害。
- **`copy/`**: 浅拷贝与深拷贝的实现途径（Cloneable、序列化与深拷贝工具）。
- **`generics/`**: 泛型核心概念、类型擦除机制（Type Erasure）与通用 Repository 实践。
- **`reflection/`**: 反射机制原理、优缺点分析、私有属性访问突破与性能损耗应对。
- **`annotations/`**: 注解原理、元注解体系与运行时反射解析框架实战。
- **`exceptions/`**: 异常体系继承树、受检异常与非受检异常、try-catch-finally 字节码执行顺序。

### Day 02: 现代 Java 新特性与函数式编程
聚焦企业级开发主流的 Java 8 LTS 及最新 Java 21 LTS 核心进阶特性：
- **`java8/`**: Lambda 表达式原理、函数式接口体系、默认接口方法设计意图。
- **`stream/`**: Stream API 核心操作符（filter/map/flatMap/reduce）、惰性求值与性能基准。
- **`async/`**: CompletableFuture 异步编排、任务合并、异常降级与多源并行处理实战。
- **`java21/`**: Java 21 核心演进、虚拟线程（Virtual Thread）底层调度模型与百万并发对比。
- **`serialization/`**: Java 原生序列化、`serialVersionUID` 作用与生产级安全防范。
- **`other/`**: Java Native Interface (JNI) 本地方法与操作系统底层交互机制。

### Day 03: Java 集合框架底层源码剖析
全方位攻克大厂必问的集合容器源码、数据结构、扩容缩容与并发安全性：
- **`concept/`**: Collection 集合框架顶层架构设计、List/Set/Queue/Map 分类体系与遍历策略。
- **`list/`**:
  - ArrayList、LinkedList、Vector 核心区别；
  - ArrayList 动态扩容机制（1.5 倍）与线程安全转换方案；
  - 多线程并发下 ArrayList 的 `add()` 数据丢失与数组越界底层复现；
  - CopyOnWriteArrayList 读写分离写时复制机制及适用场景；
  - List 与数组互转的避坑指南（`Arrays.asList` 陷阱与泛型擦除）。
- **`set/`**:
  - List 与 Set 核心区别、HashSet 无重复元素的 `putVal` 底层机制；
  - 维持插入顺序的 LinkedHashSet 与基于红黑树有序的 TreeSet；
  - 重写 equals 不重写 hashCode 引发的哈希集合数据泄漏演示。
- **`map/`**:
  - HashMap 数组 + 单链表 + 红黑树结构演进；
  - 为什么用红黑树而不是 AVL 树？哈希冲突解决算法；
  - put 与 get 完整执行流程及高低位异或扰动函数；
  - 为什么容量必须是 2 的 n 次方？扩容机制与多线程死循环/数据覆盖问题；
  - HashTable 与 ConcurrentHashMap 的实现演进（分段锁 Segment -> Node 数组 + CAS + synchronized）。

### Day 04: Java 多线程基础与内存模型 (JMM)
透彻讲解 Java 并发多线程生命周期、协作通讯与底层硬件级内存模型：
- **`model/`**: Java 线程与操作系统的内核线程（1:1 模型）映射关系、协程与线程对比。
- **`creation/`**: 线程创建的四种方式（Thread、Runnable、Callable、线程池）及为何不建议继承 Thread。
- **`lifecycle/`**: 线程六大状态转换状态机、BLOCKED 与 WAITING 状态细致差异。
- **`interrupt/`**: 线程中断机制（`interrupt`、`isInterrupted`、`interrupted`）与优雅停机方案。
- **`waitnotify/`**: `wait()` / `notify()` 底层 ObjectMonitor 机制与虚假唤醒规避。
- **`communication/`**: 线程间通信的三种方式（共享内存、管道流动、条件队列）。
- **`jmm/`**: Java 内存模型（主内存与工作内存）、可见性、有序性、原子性三大特性及 Happens-Before 规则。
- **`overview/`**: 并发编程四大阶段与常见核心面试汇总。

### Day 05: 并发安全体系、JUC 核心工具与线程池工程化
攻坚高并发系统架构中不可或缺的锁升级、无锁并发、AQS 抽象队列同步器与线程池调优：
- **`concurrentsafety/`**:
  - `volatile` 内存语义、内存屏障（LoadLoad/LoadStore/StoreStore/StoreLoad）与双重检验锁单例；
  - `synchronized` 锁升级演进（无锁 -> 偏向锁 -> 轻量级锁 -> 重量级锁）与 Mark Word 布局；
  - 乐观锁与悲观锁、公平锁与非公平锁、可重入锁底层计数逻辑；
  - CAS 无锁原子操作、Unsafe 原理、ABA 问题与 AtomicStampedReference 解决方案；
  - AQS（AbstractQueuedSynchronizer）核心架构、State 状态与双向 CLH 队列变种，基于 AQS 手写实现自定义可重入公平锁。
- **`multithread/`**:
  - 死锁形成的四个必要条件与破坏策略（Lock 接口 `tryLock` 定时尝试避免死锁）；
  - ThreadLocal 核心实现原理、ThreadLocalMap 弱引用机制与内存泄漏排查（`remove()` 必须调用）；
  - JUC 核心并发工具类（CountDownLatch、CyclicBarrier、Semaphore）。
- **`threadpool/`**:
  - 线程池核心七大参数解析与工作任务调度队列运转流程；
  - 四种系统默认拒绝策略与生产级自定义告警/持久化拒绝策略实战；
  - 动态线程池调优实践与 CPU 密集型/IO 密集型线程数精确计算。

### Day 06: JVM 虚拟机深度探秘（内存模型、类加载与垃圾回收）
聚焦 JVM 内部黑盒运作机理、从字节码执行到垃圾回收的全生命周期管理：
- **`memory/` (JVM 内存结构与排查)**:
  - JVM 运行时数据区五大组成部分及各区域职责；
  - 堆和栈的区别（指针还是对象）；
  - 堆分代结构（Eden/Survivor/Old/Humongous）与大对象分配；
  - 方法区演进（永久代 -> 元空间）与字节码执行引擎；
  - 字符串常量池机制、`String s = new String("abc")` 内存分配流转；
  - 强、软、弱、虚四大引用类型与 WeakHashMap 典型应用；
  - 内存泄漏与内存溢出（OOM）实战复现、排查工具（MAT/JProfiler/VisualVM）与生产调优。
- **`classloading/` (类初始化与双亲委派机制)**:
  - 对象创建的六大详细流程与完整生命周期（加载 -> 连接 -> 初始化 -> 使用 -> 卸载）；
  - 类加载器层级结构（Bootstrap -> Extension/Platform -> Application -> Custom）；
  - 双亲委派模型的定义、三大核心作用（安全性、一致性、避免重复加载）；
  - 破坏双亲委派机制的场景（SPI 机制、OSGi、Tomcat 类加载隔离）及手写打破双亲委派的自定义 ClassLoader。
- **`gc/` (垃圾回收机制与算法)**:
  - 垃圾回收定义与自动/手动触发条件；
  - 垃圾判别方法（引用计数法缺陷 vs 可达性分析法及对象自救）；
  - 垃圾回收算法演进（标记-清除、标记-复制、标记-整理、分代收集、分区算法）；
  - 标记清除算法的致命缺陷（碎片化与效率不稳定）；
  - GC 哪些阶段会 Stop The World (STW)？
  - Minor GC、Major GC、Full GC 核心区别与 Full GC 触发的六大场景；
  - 垃圾收集器全景图谱（Serial、Parallel、CMS、G1、ZGC、Shenandoah）；
  - CMS 与 G1 的六大维度深度对比及选型指南；
  - G1 回收器的七大核心特色（Region、垃圾优先、停顿预测模型、SATB等）；
  - GC 的回收范围（不仅是堆，元空间废弃常量与类卸载）。

---

## 三、工程特色与规范

1. **题目覆盖全面且原汁原味**：严格保留一线开发与面试常见原题，配套系统解答，兼顾面试突击与长期架构底层积累。
2. **100% 独立可运行**：每个重点模块均包含完整的 `main` 方法，支持随时单点运行调试并实时查看控制台推导日志。
3. **零 Emoji 严谨代码风格**：全工程遵循专业工程规范，代码、日志与注释完全杜绝任何 emoji 字符，保证跨平台终端输出整洁不乱码。
4. **编译零报错与高质量**：全工程 150+ 源码文件经过严格的全量编译验证，保证无语法错误、无缺失依赖。

---

## 四、快速上手指南

### 1. 环境依赖
- **操作系统**: Windows / macOS / Linux
- **开发工具**: IntelliJ IDEA 2023+ / VS Code
- **JDK 版本**: JDK 17 或 JDK 21（推荐使用支持虚拟线程的 JDK 21，向前兼容 JDK 8 核心语法）
- **编译配置**: 源码统一采用 UTF-8 编码

### 2. 导入与运行
1. 克隆代码仓库：
   ```bash
   git clone https://github.com/LinSky-J/javacode.git
   ```
2. 打开 IntelliJ IDEA，选择 `File -> Open`，定位并打开项目根目录。
3. 确保 Project SDK 配置为 JDK 17 或 JDK 21。
4. 导航至任意专题目录（如 `day06/gc/GarbageCollectionInterviewMasterSummary.java`），点击类名旁的运行按钮即可在控制台查看深度推导与分析输出。

---

<a name="english-version"></a>
# English Version

## 1. Project Overview

**Java Interview Master** is an engineering-grade knowledge base and runnable test-case repository focusing on **Core Java Mechanics, Concurrent Programming, Collection Framework Internals, and JVM Architecture**.

Adhering to the core philosophy of **"Problem-Driven, Source-Level Analysis, Empirical Verification, and Exhaustive Coverage"**, this repository converts high-frequency technical interview questions from top-tier tech companies into clean, well-structured, and directly executable Java source files. With over 150 class files, it systematically covers fundamental language concepts, modern Java 8/21 features, collection internals, multithreading and JMM, lock escalation, AQS/CAS lock-free mechanisms, thread pool tuning, and the JVM runtime, class loading delegation, and Garbage Collection (GC).

---

## 2. Comprehensive Module Directory

The repository is structured into six progressive modules (`day01` to `day06`):

### Day 01: Core Java Foundations & Object-Oriented Principles
Deconstructing runtime execution, typing system, syntax sugar, and OOP design:
- **`basics/`**: Core Java features, trade-offs, and an exhaustive feature comparison between Java and Python.
- **`execution/`**: Compiled vs. interpreted languages, and the mechanics of Java's "Write Once, Run Anywhere" cross-platform portability.
- **`jvm/`**: Architecture diagrams and boundary distinctions between JVM, JRE, and JDK.
- **`datatypes/`**: Primitive types, auto-boxing/unboxing bytecodes, and IntegerCache pool traps.
- **`parameters/`**: Value-transfer mechanism (pass-by-value nature with memory object graph representations).
- **`oop/`**: Encapsulation, inheritance, polymorphism, overriding vs overloading, abstract classes vs interfaces, and static nested vs inner classes.
- **`keywords/`**: `static` and `final` keywords in memory allocation and immutability design.
- **`strings/`**: Source comparison of String, StringBuilder, and StringBuffer; immutability and char/byte array optimizations.
- **`objects/`**: Object methods deep dive, contract requirements for `equals` and `hashCode`, and hash collision impact.
- **`copy/`**: Shallow copy vs deep copy approaches (Cloneable, serialization, and deep copy utilities).
- **`generics/`**: Generics theory, Type Erasure mechanics, bridge methods, and generic repository design.
- **`reflection/`**: Reflection capabilities, performance overhead, bypassing private access, and optimization tips.
- **`annotations/`**: Annotation mechanics, meta-annotations, and building a runtime reflection-based annotation parser.
- **`exceptions/`**: Exception hierarchy, checked vs unchecked exceptions, and try-catch-finally return evaluation flow.

### Day 02: Modern Java & Advanced Features
Targeting corporate-level LTS versions (Java 8 LTS and Java 21 LTS):
- **`java8/`**: Lambda expressions, functional interfaces (`Function`, `Predicate`, `Consumer`, `Supplier`), and default methods in interfaces.
- **`stream/`**: Stream API pipeline processing (`filter`, `map`, `flatMap`, `reduce`), lazy evaluation, and performance benchmarks.
- **`async/`**: CompletableFuture asynchronous task pipelines, orchestration, exception handling, and parallel task joining.
- **`java21/`**: Virtual Threads architecture, carrier thread scheduling, and handling millions of concurrent tasks.
- **`serialization/`**: Native Java serialization, `serialVersionUID` importance, and security risks.
- **`other/`**: Java Native Interface (JNI) and system-level OS interactions.

### Day 03: Java Collections Framework Internals
Deep dive into data structures, resizing mechanics, and concurrent safety of core collections:
- **`concept/`**: Collection framework architecture, taxonomy of List, Set, Queue, and Map, and traversal benchmarking.
- **`list/`**:
  - Comparison of ArrayList, LinkedList, and Vector;
  - Dynamic growth algorithm (1.5x) and synchronization wrappers;
  - Multithreaded race conditions in ArrayList (lost updates and `ArrayIndexOutOfBoundsException`);
  - CopyOnWriteArrayList design, copy-on-write snapshot mechanics, and use cases;
  - Safe conversion between arrays and lists (`Arrays.asList` fixed-size traps).
- **`set/`**:
  - Differences between List and Set, HashSet's internal delegation to HashMap `putVal`;
  - LinkedHashSet insertion order maintenance and TreeSet Red-Black tree sorting;
  - Memory leak and data loss demonstrations when overriding `equals` without `hashCode`.
- **`map/`**:
  - HashMap layout evolution (Array + LinkedList + Red-Black Tree);
  - Why Red-Black Trees instead of AVL Trees; Hash collision resolution strategies;
  - Step-by-step trace of `put` and `get`, bitwise hash spread functions;
  - Why table capacity must be a power of two; Resizing behavior and multi-threading race conditions;
  - Architectural evolution from HashTable to ConcurrentHashMap (Segment locking -> Node array + CAS + synchronized).

### Day 04: Multithreading Fundamentals & Java Memory Model (JMM)
Exploring thread lifecycles, inter-thread synchronization, and memory consistency:
- **`model/`**: Java thread to OS kernel thread mapping (1:1 model), and threads vs coroutines.
- **`creation/`**: Four ways to create threads (Thread, Runnable, Callable, ThreadPool) and best practices.
- **`lifecycle/`**: The six-state thread state machine, and precise differences between BLOCKED and WAITING.
- **`interrupt/`**: Thread interruption protocols (`interrupt`, `isInterrupted`, `interrupted`) and graceful shutdown patterns.
- **`waitnotify/`**: `wait()` and `notify()` with ObjectMonitor, and avoiding spurious wakeups.
- **`communication/`**: Inter-thread communication mechanisms (shared memory, pipes, conditional wait queues).
- **`jmm/`**: Main memory vs working memory, Visibility, Atomicity, Ordering, and Happens-Before rules.
- **`overview/`**: Multi-phase concurrent programming roadmap and interview summary.

### Day 05: Concurrency Safety, JUC Utilities & ThreadPool Engineering
Mastering lock upgrades, non-blocking synchronization, AQS, and industrial thread pool management:
- **`concurrentsafety/`**:
  - `volatile` semantics, memory barriers (LoadLoad, LoadStore, StoreStore, StoreLoad), and DCL Singleton;
  - `synchronized` lock escalation (No lock -> Biased lock -> Lightweight lock -> Heavyweight lock) and Mark Word layouts;
  - Optimistic vs pessimistic locks, fair vs non-fair locks, and reentrant locking logic;
  - CAS operations, Unsafe mechanics, ABA problems, and AtomicStampedReference resolution;
  - AQS (AbstractQueuedSynchronizer) architecture, synchronization state, CLH queue variants, and writing a custom fair reentrant lock.
- **`multithread/`**:
  - Four necessary conditions of Deadlock and avoidance strategies (`tryLock` timeout pattern);
  - ThreadLocal mechanics, ThreadLocalMap weak reference design, and memory leak prevention (`remove()` call requirement);
  - JUC coordination utilities (CountDownLatch, CyclicBarrier, Semaphore).
- **`threadpool/`**:
  - Seven core parameters of ThreadPoolExecutor and task rejection lifecycle;
  - Default rejection policies and implementing custom alerting/persistence rejection policies;
  - Dynamic thread pool parameter tuning and sizing formulas for CPU-bound vs IO-bound workloads.

### Day 06: JVM Deep Dive (Memory Structure, Class Loading & Garbage Collection)
Deconstructing the virtual machine black box from bytecode execution to GC:
- **`memory/` (JVM Runtime Data Areas & OOM Troubleshooting)**:
  - Five runtime data areas (Heap, Stack, Method Area, PC, Native Method Stack);
  - Stack vs Heap (values, references, and objects);
  - Heap generational structure (Eden, Survivor, Old, Humongous) and large object direct allocation;
  - Method area evolution (PermGen to Metaspace) and execution engine;
  - String Constant Pool mechanics, memory allocations in `String s = new String("abc")`;
  - Reference types (Strong, Soft, Weak, Phantom) and WeakHashMap caching;
  - Memory leak and OOM scenarios, diagnostic tools (MAT, JProfiler, VisualVM), and tuning guidelines.
- **`classloading/` (Class Initialization & Parents Delegation)**:
  - Six-step object creation lifecycle (Loading -> Linking -> Initialization -> Usage -> Unloading);
  - ClassLoader hierarchy (Bootstrap, Extension/Platform, Application, Custom);
  - Parents Delegation model definition and three core benefits (security, consistency, single-load guarantee);
  - Scenarios breaking Parents Delegation (SPI, OSGi, Tomcat isolation) and writing a custom delegating ClassLoader.
- **`gc/` (Garbage Collection Algorithms & Collectors)**:
  - Garbage collection definitions and automatic vs explicit triggers;
  - Liveness detection: Reference Counting flaws vs Reachability Analysis and object revival via `finalize()`;
  - Core GC algorithms: Mark-Sweep, Mark-Copy, Mark-Compact, Generational, and Region-based partitioning;
  - Fatal drawbacks of Mark-Sweep (memory fragmentation and non-uniform allocation latency);
  - Stop The World (STW) phases across collectors;
  - Distinctions between Minor GC, Major GC, and Full GC; Six trigger scenarios for Full GC;
  - Collector taxonomy (Serial, Parallel, CMS, G1, ZGC, Shenandoah);
  - Comprehensive comparison between CMS and G1 across six architectural dimensions;
  - Seven defining features of G1 (Region layout, Garbage-First heuristic, Predictable pause-time model, SATB, etc.);
  - GC scope (Heap regions as well as Metaspace constant pool clearing and class unloading).

---

## 3. Engineering Quality & Standards

1. **Faithful Interview Mapping**: Retains real-world corporate interview questions accompanied by in-depth solutions for rapid revision and lasting architectural mastery.
2. **100% Standalone Executable**: Every principal topic includes a self-contained `main` method for immediate debugging and live console tracing.
3. **Zero-Emoji Professional Style**: Strictly enforces standard industrial formatting without any emoji characters, ensuring clean, uncorrupted console output across all operating systems.
4. **Zero Compilation Warnings & Errors**: All 150+ source files pass strict full-repository compilation checks (`javac`).

---

## 4. Quick Start

### 1. Prerequisites
- **Operating System**: Windows, macOS, or Linux
- **IDE**: IntelliJ IDEA 2023+ or Visual Studio Code
- **JDK**: JDK 17 or JDK 21 (JDK 21 recommended for Virtual Thread execution; backwards-compatible with Java 8 core syntax)
- **Encoding**: UTF-8

### 2. Clone and Run
1. Clone the repository:
   ```bash
   git clone https://github.com/LinSky-J/javacode.git
   ```
2. Open IntelliJ IDEA and choose `File -> Open`, then select the repository root directory.
3. Verify that the Project SDK is set to JDK 17 or JDK 21.
4. Open any class file (for example, `day06/gc/GarbageCollectionInterviewMasterSummary.java`) and run the `main` method to inspect the live execution trace and analysis in the console.

---

## 5. Repository Structure Tree / 目录结构树

```text
Java interview/
├── README.md                      # Bilingual Project Documentation / 中英文项目说明文档
├── day01/                         # Java Foundations / Java 核心基础
│   ├── annotations/
│   ├── basics/
│   ├── copy/
│   ├── datatypes/
│   ├── exceptions/
│   ├── execution/
│   ├── generics/
│   ├── jvm/
│   ├── keywords/
│   ├── objects/
│   ├── oop/
│   ├── parameters/
│   ├── reflection/
│   └── strings/
├── day02/                         # Modern Java & Advanced / 现代 Java 特性与进阶
│   ├── async/
│   ├── java21/
│   ├── java8/
│   ├── other/
│   ├── serialization/
│   └── stream/
├── day03/                         # Collections Framework / 集合框架源码
│   ├── concept/
│   ├── list/
│   ├── map/
│   └── set/
├── day04/                         # Multithreading & JMM / 多线程与内存模型
│   ├── communication/
│   ├── creation/
│   ├── interrupt/
│   ├── jmm/
│   ├── lifecycle/
│   ├── model/
│   ├── overview/
│   └── waitnotify/
├── day05/                         # Concurrency Safety & JUC / 并发安全与 JUC 实战
│   ├── concurrentsafety/
│   ├── multithread/
│   └── threadpool/
└── day06/                         # JVM Deep Dive / JVM 虚拟机深度探索
    ├── classloading/
    ├── gc/
    └── memory/
```
