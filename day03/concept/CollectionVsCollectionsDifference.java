package concept;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 题目：Collections和Collection的区别
 *
 * 核心考点与理论剖析：
 *
 * 1. 概念本质区别：
 *    - Collection 是一个【接口】（java.util.Collection）。
 *      它是单列集合框架的顶层根接口，为 List、Set、Queue 等子接口提供通用的行为规范契约，不能直接实例化。
 *    - Collections 是一个【工具类】（java.util.Collections）。
 *      构造器被私有化（private Collections() {}），专门服务于各类集合的静态实用方法集合（Static Utility Class）。
 *
 * 2. 职责与核心 API 对比：
 *    - Collection 接口：
 *      定义集合的抽象行为：add()、remove()、contains()、size()、isEmpty()、iterator()、stream()、toArray()。
 *    - Collections 工具类：
 *      提供五大类集合操作算法与高级封装：
 *      A. 算法操作：
 *         - Collections.sort(list)：基于 TimSort 算法对列表排序。
 *         - Collections.binarySearch(list, key)：二分查找（要求预先有序）。
 *         - Collections.reverse(list)：逆序翻转。
 *         - Collections.shuffle(list)：洗牌随机打乱。
 *      B. 极值与频次统计：
 *         - Collections.max(coll)、Collections.min(coll)。
 *         - Collections.frequency(coll, element)：统计元素出现次数。
 *      C. 线程安全包装（装饰器模式）：
 *         - Collections.synchronizedList(list)、Collections.synchronizedMap(map)。
 *      D. 不可变只读视图（防御性编程）：
 *         - Collections.unmodifiableList(list)：返回不可修改的只读包装，试图调用 add/remove 会立即抛出 UnsupportedOperationException。
 *      E. 内存节约型单例与空集合：
 *         - Collections.emptyList()、Collections.emptyMap()：返回共享的单例空对象，避免方法返回空集合时频繁 new 造成的 GC 压力。
 *         - Collections.singletonList(element)：不可变单元素高效集合。
 */
public class CollectionVsCollectionsDifference {

    public static void demonstrateDifference() {
        System.out.println("--- Collection 接口与 Collections 工具类差异对比 ---");

        // 1. Collection 接口作为多态引用
        Collection<String> collectionRef = new ArrayList<>();
        collectionRef.add("Node_C");
        collectionRef.add("Node_A");
        collectionRef.add("Node_B");
        System.out.println("1. Collection 接口多态引用实例: size=" + collectionRef.size() + ", 内容=" + collectionRef);

        // 2. Collections 工具类操作：排序
        List<String> list = (List<String>) collectionRef;
        Collections.sort(list);
        System.out.println("2. Collections.sort() 排序后: " + list);

        // 翻转
        Collections.reverse(list);
        System.out.println("3. Collections.reverse() 逆序后: " + list);

        // 3. 不可变视图
        List<String> unmodifiableList = Collections.unmodifiableList(list);
        try {
            unmodifiableList.add("Node_D");
        } catch (UnsupportedOperationException ex) {
            System.out.println("4. Collections.unmodifiableList() 返回只读视图，写入时抛出: " + ex.getClass().getSimpleName());
        }

        // 4. 空集合与内存优化
        List<String> emptyList = Collections.emptyList();
        System.out.println("5. Collections.emptyList() 内存复用单例空集合: size=" + emptyList.size());
    }
}
