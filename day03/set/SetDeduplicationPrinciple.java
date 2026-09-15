package set;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * 题目：Set集合有什么特点？如何实现key无重复的?
 *
 * 核心考点与源码级深度剖析：
 *
 * 一、Set 集合的核心特点：
 * 1. 元素唯一性：集合内不允许存储逻辑相等的重复元素，重复添加直接返回 false。
 * 2. 无整数索引：没有下标概念，不能像 List 一样通过 get(i) 检索，不支持基于位置的随机访问。
 * 3. Null 值限制：HashSet、LinkedHashSet 最多仅允许存入一个 null 元素；TreeSet 在自然排序下严禁存入 null。
 * 4. 底层依附性：Set 自身并不维护独立的数据结构，而是直接依托于对应的 Map 体系实现（HashSet 包装 HashMap，TreeSet 包装 TreeMap）。
 *
 * 二、HashSet 如何实现 Key 无重复的？（深入 HashMap.putVal 底层流程）：
 * 1. 结构封装：
 *    - private transient HashMap<E,Object> map;
 *    - private static final Object PRESENT = new Object(); // 全局唯一的静态哑对象占位
 *    - public boolean add(E e) { return map.put(e, PRESENT) == null; }
 * 2. 去重四步校验时序：
 *    - Step 1（哈希计算）：调用元素的 key.hashCode()，并通过扰动函数优化：hash = (h = key.hashCode()) ^ (h >>> 16)；
 *    - Step 2（定位桶位）：通过路由算法 (n - 1) & hash 找到数组槽位；
 *    - Step 3（三层相等比对）：
 *      if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
 *      * 首先比对两者的 hash 整数值是否相等。若不同，绝不可能是同一个对象，直接放行；
 *      * 若 hash 相同，比对两者内存引用是否相等（k == key）。若相同，判定重复；
 *      * 若引用不同，调用 key.equals(k) 进行逻辑业务相等比对。若返回 true，判定重复。
 *    - Step 4（重复拦截）：判定为重复后，put() 返回旧值 PRESENT，add() 判断返回值非 null，返回 false 宣告添加失败，
 *      从而彻底杜绝了重复 Key 的存入。
 *
 * 三、为什么向 HashSet 存入自定义对象时，必须同时重写 hashCode() 和 equals()？
 * 1. 若只重写 equals() 而不重写 hashCode()：
 *    - 两个业务字段完全相同的对象，由于默认使用 Object.hashCode()（基于内存地址计算），生成了不同的哈希值；
 *    - 它们会被分配到数组的不同桶位（Bucket）中，根本不会触发 equals 比对，导致重复对象被成功插入，去重彻底失效！
 * 2. 若只重写 hashCode() 而不重写 equals()：
 *    - 两个对象哈希码相同，落入同一个桶位，但由于默认的 Object.equals() 比较的是堆内存引用地址（==），
 *      比对结果为 false，它们会被作为不同节点挂在链表或红黑树上，去重同样失效！
 *
 * 四、TreeSet 是如何实现去重的？（面试深度考点）
 * - 重点：TreeSet 的去重机制【完全不依赖】hashCode() 和 equals()！
 * - 底层纯粹依据 Comparable.compareTo() 或 Comparator.compare() 的返回值是否等于 0！
 *   只要 compare 返回 0，TreeSet 就判定两对象重复，拒绝插入（即使此时调用 equals 返回 false）。
 */
public class SetDeduplicationPrinciple {

    public static void demonstrateDeduplicationMechanisms() {
        System.out.println("--- Set 去重机理与 hashCode/equals 契约实测 ---");

        // 1. 规范重写 hashCode 和 equals 的实体类去重
        Set<UserAccountItem> correctSet = new HashSet<>();
        UserAccountItem u1 = new UserAccountItem(1001, "Alice", 90);
        UserAccountItem u2 = new UserAccountItem(1001, "Alice", 90); // 逻辑重复对象
        correctSet.add(u1);
        boolean addedU2 = correctSet.add(u2);
        System.out.println("1. 规范重写 hashCode+equals 的对象去重: 成功加入u1, 加入重复u2返回=" + addedU2 + ", 最终集合大小=" + correctSet.size());

        // 2. 只重写 equals 而漏写 hashCode 导致的去重失效事故
        Set<BadUserOnlyEquals> badSet = new HashSet<>();
        BadUserOnlyEquals bad1 = new BadUserOnlyEquals(999, "Tester");
        BadUserOnlyEquals bad2 = new BadUserOnlyEquals(999, "Tester");
        badSet.add(bad1);
        badSet.add(bad2); // 预期应该去重，但由于 hashCode 不同，导致两个都被存入！
        System.out.println("2. [反面典型] 漏写 hashCode 导致去重失效: 预期大小=1, 实际大小=" + badSet.size() + " (两个相同 ID 的对象均被存入!)");

        // 3. TreeSet 纯粹依赖 compareTo == 0 进行去重验证
        Set<UserAccountItem> treeSet = new TreeSet<>((a, b) -> Integer.compare(a.getScore(), b.getScore()));
        UserAccountItem t1 = new UserAccountItem(1001, "UserA", 95);
        UserAccountItem t2 = new UserAccountItem(1002, "UserB", 95); // ID 与姓名均不同，但分数相同！
        treeSet.add(t1);
        boolean treeSetAdd = treeSet.add(t2);
        System.out.println("3. TreeSet 依据 compare == 0 去重: t1(UserA, 95) 与 t2(UserB, 95) 分数相同，加入结果=" + treeSetAdd + ", TreeSet 大小=" + treeSet.size());
    }
}
