package map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 题目覆盖：
 * 如何对map进行快速遍历?
 *
 * 核心考点与理论剖析：
 *
 * 一、Map 遍历的五种主流方式：
 * 1. 方式一（最佳通用实践）：使用 entrySet() 配合增强 for 循环或 Iterator
 *    - 核心优势：单次循环直接从 Node 节点（Entry）中获取 Key 和 Value，时间复杂度为严格的 O(n)。
 *    - 性能表现：最高。避免了任何额外的二次查找与哈希重计算开销。
 * 2. 方式二（反面低效模式）：使用 keySet() 遍历，循环体内调用 map.get(key)
 *    - 性能缺陷：极慢！先遍历所有的 Key，然后在循环体内每一次都重新调用 map.get(key)。
 *      每一次 get() 都需要重新计算 key 的 hashcode、重新定位数组下标并在链表或红黑树上查找比对，
 *      导致无谓的二次散列计算与树遍历，效率比 entrySet() 低 30% ~ 100%。
 * 3. 方式三：仅需 Value 时使用 map.values()
 *    - 直接获取所有 Value 的集合视图，不需要遍历无用的 Key，更加轻量。
 * 4. 方式四：Java 8+ 内部迭代器 map.forEach(BiConsumer)
 *    - 语法极其精炼，底层也是通过 entrySet 循环分发，性能与 entrySet 相当，代码表现力最佳。
 * 5. 方式五：Stream API 流式遍历 map.entrySet().stream()
 *    - 支持流式过滤、映射转换，且支持 parallelStream() 并行流加速高并发统计。
 *
 * 二、实测基准对比结论：
 * 在遍历获取 Key 和 Value 时，必须优先选择 entrySet() 或 map.forEach()，严禁在 keySet() 循环中反查 map.get(key)！
 */
public class MapTraversalPerformanceBenchmark {

    public static void demonstrateTraversalMethods() {
        System.out.println("--- Map 五大遍历方式功能与语法实战 ---");
        Map<String, String> map = new HashMap<>();
        map.put("K1", "V1");
        map.put("K2", "V2");
        map.put("K3", "V3");

        // 1. entrySet() 增强 for 遍历
        System.out.print("1. entrySet() 增强 for 遍历: ");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.print(entry.getKey() + "=" + entry.getValue() + " ");
        }
        System.out.println();

        // 2. entrySet() Iterator 迭代器遍历（支持遍历中安全删除）
        System.out.print("2. entrySet() Iterator 遍历: ");
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            System.out.print(entry.getKey() + "=" + entry.getValue() + " ");
        }
        System.out.println();

        // 3. keySet() 仅遍历 Key
        System.out.print("3. keySet() 仅遍历 Key: ");
        for (String key : map.keySet()) {
            System.out.print(key + " ");
        }
        System.out.println();

        // 4. values() 仅遍历 Value
        System.out.print("4. values() 仅遍历 Value: ");
        for (String val : map.values()) {
            System.out.print(val + " ");
        }
        System.out.println();

        // 5. Java 8 forEach(BiConsumer)
        System.out.print("5. Java 8 forEach(BiConsumer): ");
        map.forEach((k, v) -> System.out.print(k + "=" + v + " "));
        System.out.println();
    }

    /**
     * entrySet 与 keySet+get 在海量数据下的性能基准实测
     */
    public static void runBenchmark(int elementCount) {
        System.out.println("\n--- entrySet 与 keySet+get 性能差异实测 (数据量: " + elementCount + ") ---");
        Map<Integer, String> testMap = new HashMap<>(elementCount);
        for (int i = 0; i < elementCount; i++) {
            testMap.put(i, "Data_" + i);
        }

        // 基准测试 A: entrySet 遍历
        long startEntry = System.nanoTime();
        long dummyCountA = 0;
        for (Map.Entry<Integer, String> entry : testMap.entrySet()) {
            Integer k = entry.getKey();
            String v = entry.getValue();
            dummyCountA += k + v.length();
        }
        long timeEntryMs = (System.nanoTime() - startEntry) / 1_000_000;
        System.out.println("1. [推荐] entrySet() 耗时: " + timeEntryMs + " ms, 校验和: " + dummyCountA);

        // 基准测试 B: keySet + get(key) 反面遍历
        long startKeySet = System.nanoTime();
        long dummyCountB = 0;
        for (Integer k : testMap.keySet()) {
            String v = testMap.get(k); // 二次哈希定位与检索
            dummyCountB += k + v.length();
        }
        long timeKeySetMs = (System.nanoTime() - startKeySet) / 1_000_000;
        System.out.println("2. [低效] keySet() + get(key) 耗时: " + timeKeySetMs + " ms, 校验和: " + dummyCountB);
        System.out.println("结论：keySet() + get(key) 存在重复计算哈希与树/链表二次寻址开销，大数据量下性能显著劣化！");
    }
}
