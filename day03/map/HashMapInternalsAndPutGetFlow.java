package map;

import java.util.HashMap;
import java.util.Map;

/**
 * 题目覆盖：
 * 1. HashMap实现原理介绍一下?
 * 2. 了解的哈希冲突解决方法有哪些?
 * 3. 在 Java 的 hashmap 中 get一个元素的过程是怎样的?
 * 4. hashmap的put过程介绍一下
 * 5. HashMap的put(key,val)和get(key)过程
 * 6. hashmap key可以为null吗?
 *
 * 核心考点与源码级深度剖析：
 *
 * 一、HashMap 底层实现架构（JDK 1.8+）：
 * 1. 数据结构：Node<K,V>[] 动态数组 + 单向链表 + 红黑树（TreeNode<K,V>）。
 * 2. 初始容量与阈值：默认初始容量为 16，默认负载因子（loadFactor）为 0.75，扩容阈值为 16 * 0.75 = 12。
 * 3. 树化与退化双重阈值：
 *    - 树化条件：链表长度 >= 8 且底层数组总容量 >= 64 时，链表才会转换为红黑树；若链表达到 8 但数组容量小于 64，
 *      优先调用 resize() 进行数组扩容，规避过早树化；
 *    - 退化条件：当扩容拆分或删除节点导致红黑树节点数 <= 6 时，红黑树退化还原为普通单向链表（留出 7 作为缓冲，避免频繁抖动）。
 *
 * 二、哈希冲突（Hash Collision）的四大主流解决方法：
 * 1. 链地址法 / 拉链法（Separate Chaining）：
 *    - HashMap、Hashtable 所采用的方式。将所有散列到同一桶位的元素用单向链表或红黑树串联。
 * 2. 开放定址法（Open Addressing）：
 *    - 发生冲突时，按照某种探测序列寻找下一个空闲的数组槽位：
 *      * 线性探测（Linear Probing）：顺延往后找下一个空位（Java 中 ThreadLocalMap 采用此方式）；
 *      * 二次探测（Quadratic Probing）：探测步长按平方跳跃（+1^2, -1^2, +2^2 ...）；
 *      * 双重散列（Double Hashing）：使用第二个哈希函数计算探测步长。
 * 3. 再哈希法 / 双哈希（Re-hashing）：
 *    - 准备多个不同的哈希算法，当第一个哈希冲突时，换用第二个哈希函数计算，直到不冲突。
 * 4. 公共溢出区法（Coalesced Hashing / Public Overflow Area）：
 *    - 将散列表分为基本表和溢出表，所有冲突的元素一律存入公共溢出区。
 *
 * 三、HashMap 的 put(key, val) 完整底层时序：
 * Step 1（初始化检测）：检查底层数组 table 是否为空或长度为 0，若是则调用 resize() 进行延迟惰性初始化（分配默认容量 16）；
 * Step 2（高低位扰动计算）：计算 key 的哈希码：hash = (h = key.hashCode()) ^ (h >>> 16)；
 * Step 3（路由寻址）：通过 (n - 1) & hash 定位数组下标 i；
 * Step 4（无冲突写入）：若 table[i] 为 null，直接构造新 Node 放入该槽位；
 * Step 5（冲突处理）：若 table[i] 槽位已有节点：
 *       - 子分支 A（首节点即匹配）：若首节点与待存 key 的 hash 相同且 (k == key || (key != null && key.equals(k)))，
 *         说明已存在该 Key，记录该节点准备覆盖；
 *       - 子分支 B（红黑树节点）：若首节点是 TreeNode 类型，调用 putTreeVal 进行红黑树遍历查找或插入，耗时 O(log n)；
 *       - 子分支 C（普通单向链表）：采用【尾插法】顺着 next 遍历链表：
 *         * 若遍历到相同 key，跳出循环准备覆盖；
 *         * 若遍历到尾节点仍未找到，在末尾创建新 Node 挂载；若挂载后链表长度达到 8，触发 treeifyBin 进行树化检测。
 * Step 6（Value 覆盖）：若匹配到已有 Key，用新 value 替换旧 value，并返回旧 value；
 * Step 7（容量检测）：++modCount，若 ++size > threshold，触发 resize() 扩容翻倍。
 *
 * 四、HashMap 的 get(key) 完整底层时序：
 * Step 1（合法性前置）：检查 table 非空、长度大于 0，且通过 (n - 1) & hash 算出的首节点 first 不为 null；
 * Step 2（首节点命中）：直接比对首节点：if (first.hash == hash && ((k = first.key) == key || key.equals(k)))，
 *       若匹配成功，直接返回 first.value（大部分场景单次 O(1) 命中）；
 * Step 3（冲突分支检索）：若首节点未命中且 first.next != null：
 *       - 若首节点是 TreeNode，调用 ((TreeNode)first).getTreeNode(hash, key) 在红黑树中基于二叉查找树检索，O(log n)；
 *       - 否则，沿着单向链表线性循环比对每一个 Node，O(k)；
 * Step 4（未找到）：遍历结束未匹配到目标 Key，返回 null。
 *
 * 五、HashMap 的 Key 可以为 null 吗？
 * - 【可以】！HashMap 允许且仅允许一个 Key 为 null，允许多个 Value 为 null。
 * - 底层实现：当 key == null 时，hash(key) 直接固定返回 0，因此 null key 永远固定存储在 table[0] 的槽位中。
 */
public class HashMapInternalsAndPutGetFlow {

    public static void demonstratePutGetFlowAndNullKey() {
        System.out.println("--- HashMap put/get 核心执行流与 null key 机制验证 ---");

        Map<String, String> map = new HashMap<>();

        // 1. 验证 null key 的支持
        map.put(null, "NullValue_Original");
        System.out.println("1. 存入 null key 成功: map.get(null) = " + map.get(null));

        // 覆盖 null key
        String oldValue = map.put(null, "NullValue_Updated");
        System.out.println("2. 覆盖 null key，返回旧值: " + oldValue + ", 新值: " + map.get(null));

        // 2. 正常键值对写入与读取
        map.put("Spring", "Framework");
        map.put("Netty", "NIO");
        System.out.println("3. 正常读取键值: map.get(\"Spring\") = " + map.get("Spring"));

        // 3. 读取不存在的 key
        System.out.println("4. 读取不存在的 key 返回: " + map.get("NonExistentKey"));
    }
}
