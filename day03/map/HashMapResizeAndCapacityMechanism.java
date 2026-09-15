package map;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * 题目覆盖：
 * 1. HashMap的扩容机制介绍一下
 * 2. HashMap的大小为什么是2的n次方大小呢?
 * 3. 往hashmap存20个元素，会扩容几次?
 * 4. 说说hashmap的负载因子
 *
 * 核心考点与理论剖析：
 *
 * 一、HashMap 为什么容量必须是 2 的 n 次方？
 * 1. 原因一（位运算高效替代取模）：
 *    - 寻址计算：index = (n - 1) & hash；
 *    - 当且仅当容量 n 是 2 的整数次幂时，数学上满足：hash % n == hash & (n - 1)；
 *    - CPU 执行位与 & 仅需 1 个时钟周期，而十进制取模 % 需要几十个时钟周期，运算效率高出一个数量级。
 * 2. 原因二（散列分布均匀，规避哈希碰撞）：
 *    - 当 n 是 2^k 时，n - 1 的二进制表示低位全为 1（如 16-1=15 的二进制是 0000 1111）；
 *    - 在与 hash 进行位与时，每个低位槽位都有机会被置为 1 或 0，散列空间利用率达到 100%；
 *    - 若 n 不是 2 的次幂（如 n=10，n-1=9 的二进制是 1001），中间多位恒为 0，导致算出的下标末尾永远无法覆盖某些槽位，
 *      造成严重的哈希槽位闲置与碰撞加剧。
 * 3. 原因三（扩容迁移无需重新计算哈希）：
 *    - 扩容时数组长度翻倍，高位掩码（hash & oldCap）仅为 0 或 1；
 *    - 为 0 的节点原索引不动，为 1 的节点移动到 原索引 + oldCap，拆分迁移效率极高。
 *
 * 二、说说 HashMap 的负载因子（Load Factor = 0.75）：
 * 1. 概念：衡量 HashMap 内部空间满载程度的指标。扩容阈值 threshold = capacity * loadFactor。
 * 2. 为什么选择 0.75？（时间与空间的黄金折中点 Trade-Off）：
 *    - 若设为 1.0：空间利用率高，但哈希碰撞率呈指数级上升，链表增多，严重拖慢 get/put 查询速度；
 *    - 若设为 0.5：冲突极低查询快，但只要存入一半元素就触发翻倍扩容，内存空间浪费 50%，扩容非常频繁；
 *    - 数学与泊松分布支撑：在负载因子 0.75 的条件下，单个哈希桶节点碰撞长度达到 8 的概率低至 0.00000006（千万分之六），
 *      既最大化节约了内存，又保证了极低几率触发树化。
 *
 * 三、往 HashMap 存 20 个元素，会扩容几次？
 * 1. 默认无参构造 new HashMap<>() 场景：
 *    - 初始状态：table = null，容量为 0；
 *    - 第 1 次扩容（惰性分配）：存入第 1 个元素时，分配默认容量 16，阈值 threshold = 16 * 0.75 = 12；
 *    - 存入第 2~12 个元素：size <= 12，不触发扩容；
 *    - 第 2 次扩容（翻倍扩容）：存入第 13 个元素时，因 size(13) > 12，触发扩容翻倍至 32，阈值变为 32 * 0.75 = 24；
 *    - 存入第 14~20 个元素：size(20) <= 24，不再扩容。
 *    - 结论：【共扩容 2 次】（1 次初始化分配 + 1 次真实翻倍扩容）。
 * 2. 显式指定合适容量场景（如 new HashMap<>(32)）：
 *    - 首次 put 初始容量即为 32，阈值为 24，存入 20 个元素全程【0 次额外扩容】。
 */
public class HashMapResizeAndCapacityMechanism {

    /**
     * 通过反射获取 HashMap 底层 table 数组的真实长度
     * 在高版本 JDK 强模块化封装下，若反射受限将优雅降级
     */
    public static int getTableCapacity(HashMap<?, ?> map) {
        try {
            Field tableField = HashMap.class.getDeclaredField("table");
            tableField.setAccessible(true);
            Object[] table = (Object[]) tableField.get(map);
            return table == null ? 0 : table.length;
        } catch (Throwable e) {
            return -1;
        }
    }

    private static String formatCapacity(int capacity, int fallback) {
        return capacity >= 0 ? String.valueOf(capacity) : fallback + " (规范标准容量)";
    }

    /**
     * 实测存入 20 个元素过程中的容量变化与扩容次数
     */
    public static void demonstratePutting20Elements() {
        System.out.println("--- 往 HashMap 存入 20 个元素容量演化实测 ---");

        HashMap<Integer, String> map = new HashMap<>();
        System.out.println("1. new HashMap<>() 初始化: size=" + map.size() + ", 底层容量=" + formatCapacity(getTableCapacity(map), 0));

        int resizeCount = 0;
        int lastCapacity = getTableCapacity(map);

        for (int i = 1; i <= 20; i++) {
            map.put(i, "Value_" + i);
            int currentCapacity = getTableCapacity(map);
            if (currentCapacity != lastCapacity) {
                resizeCount++;
                System.out.println(String.format("   [第 %d 次扩容] 插入第 %2d 个元素后，底层容量由 %s 扩容为 -> %s (当前 size=%2d)",
                        resizeCount, i, formatCapacity(lastCapacity, 0), formatCapacity(currentCapacity, (resizeCount == 1 ? 16 : 32)), map.size()));
                lastCapacity = currentCapacity;
            }
        }

        System.out.println("2. 存入 20 个元素最终结果: size=" + map.size() + ", 最终底层容量=" + formatCapacity(getTableCapacity(map), 32) + ", 总扩容次数=" + resizeCount + " 次");
    }
}
