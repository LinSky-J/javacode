package map;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 题目覆盖：
 * 1. HashMap一般用什么做Key？为啥String适合做Key呢?
 * 2. 重写HashMap的equal和hashcode方法需要注意什么?
 * 3. 重写HashMap的equal方法不当会出现什么问题?
 *
 * 核心考点与理论剖析：
 *
 * 一、HashMap 一般用什么做 Key？为啥 String 最适合做 Key？
 * 1. 推荐选用类型：
 *    - 最推荐使用 String、Integer、Long 等【不可变类（Immutable Class）】。
 * 2. 为什么 String 最适合做 Key？
 *    - 特性一（绝对不可变性 Immutability）：
 *      String 类被 final 修饰，内部存储字符串的 byte[]/char[] 数组同样为 private final。
 *      一旦创建完成，其内容不可被篡改，因此其 hashCode 具有绝对的确定性与不可变性。
 *    - 特性二（哈希码缓存 Cached HashCode）：
 *      String 内部维护了成员变量 private int hash;（默认值为 0）。
 *      首次调用 hashCode() 时计算哈希值并缓存在该变量中；后续再次调用时直接返回该缓存值，
 *      无需重复遍历字符数组，在 HashMap 高频 put/get 时性能极其出众。
 *    - 特性三（标准规范的 equals/hashCode 实现）：
 *      String 天然严格遵循 Java 规范，保证内容相等的字符串哈希值绝对一致。
 *
 * 二、若使用可变对象（Mutable Object）做 Key 会发生什么严重后果？
 * - 致命事故：【数据永久丢失与堆内存泄漏】！
 * - 事故过程：
 *   1. 构造一个包含属性 id 和 name 的可变对象作为 key 存入 HashMap；
 *   2. 外部代码修改了该 key 的 name 属性；
 *   3. 属性修改导致 key.hashCode() 计算结果发生改变；
 *   4. 后续调用 map.get(key) 时，根据新的哈希值计算出错误的数组下标，导致【永远查不到之前存入的 Value（返回 null）】！
 *   5. 此外，调用 map.remove(key) 也无法精准定位该节点，导致该废弃键值对永久残留在 Map 中，引发严重的内存泄漏。
 *
 * 三、重写 equals 和 hashCode 方法需要注意什么？不当会出现什么问题？
 * 1. 核心铁律（Contract）：
 *    - 若两对象通过 equals() 判定为相等，两者的 hashCode() 返回值必须绝对相同；
 *    - 若两对象的 hashCode() 相同，两者的 equals() 不一定相同（即允许哈希冲突）；
 *    - 参与 equals 比较的所有核心业务字段，必须全部参与 hashCode() 的联合哈希计算。
 * 2. 不当重写的具体问题：
 *    - 问题 A（只重写 equals，漏写 hashCode）：
 *      两个业务上完全相等的对象，由于使用默认的 Object.hashCode()（基于内存地址计算），生成的哈希值不同，
 *      落入不同的数组桶槽，导致使用相同的业务 Key 进行 map.get() 永远返回 null！
 *    - 问题 B（只重写 hashCode，漏写 equals）：
 *      两个对象即使落入同一个桶，由于默认 equals 比对的是内存引用（==），判定不相等，导致 Key 重复插入覆盖失效。
 *    - 问题 C（散列极度不均匀）：
 *      如 hashCode() 粗暴返回固定常量 1，导致所有元素集中在 table[1]，彻底退化为链表/红黑树，查询性能大幅劣化。
 */
public class HashMapKeyDesignAndHashContract {

    /**
     * 可变 Key 实体类（反面教材演示）
     */
    static class MutableKey {
        private String identifier;

        public MutableKey(String identifier) {
            this.identifier = identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MutableKey that = (MutableKey) o;
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

        @Override
        public String toString() {
            return "MutableKey{" + identifier + "}";
        }
    }

    public static void demonstrateMutableKeyLeak() {
        System.out.println("--- 可变对象作为 HashMap Key 引发无法检索与内存泄漏实测 ---");

        Map<MutableKey, String> leakMap = new HashMap<>();
        MutableKey userKey = new MutableKey("Session_1001");

        // 1. 存入键值对
        leakMap.put(userKey, "UserData_Active");
        System.out.println("1. 存入可变 Key: key=" + userKey + ", map.get(key)=" + leakMap.get(userKey));

        // 2. 外部代码修改了可变 Key 的成员属性
        userKey.setIdentifier("Session_1001_Modified");
        System.out.println("2. 外部篡改 Key 的属性为: " + userKey);

        // 3. 再次查询
        String retrieved = leakMap.get(userKey);
        System.out.println("3. 属性改变后再次 map.get(key) 结果: " + retrieved + " (数据丢失！无法查出)");

        // 4. 尝试根据原内容查找
        MutableKey oldLookupKey = new MutableKey("Session_1001");
        System.out.println("   用原始 ID 构造 Key 查询: " + leakMap.get(oldLookupKey) + " (同样无法查出！)");
        System.out.println("   当前 Map 大小: " + leakMap.size() + " (对象永久残留在堆内存中形成幽灵数据，造成内存泄漏！)");
    }
}
