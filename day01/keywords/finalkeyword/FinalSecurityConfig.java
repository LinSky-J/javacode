package keywords.finalkeyword;

/**
 * 最终类演示：系统安全核心配置类。
 *
 * 专门用于演示 Java 中 final 修饰类的核心语义与安全价值。
 *
 * 核心考点：
 * 1. 语法约束：
 *    - 当一个类被 final 修饰时，该类成为【最终类】，严禁被任何其他类继承（extends）。
 *    - 如果尝试编写 public class SubConfig extends FinalSecurityConfig，编译器会立即报错：
 *      "Cannot inherit from final 'keywords.finalkeyword.FinalSecurityConfig'"。
 *
 * 2. 核心设计目的：
 *    - 安全防篡改：防止恶意子类继承并重写核心方法、伪造或篡改系统核心逻辑。
 *    - 维护不可变契约：JDK 核心基础类（如 java.lang.String、java.lang.Integer、java.lang.Double 等）
 *      全都被声明为 public final class，确保在多线程、字符串常量池以及安全字典中的不可变性。
 *    - 编译器内联优化：JIT 编译器在执行方法调用时，明确知道该类不可能有子类派生重写，
 *      可以直接将虚方法调用退化为静态调用甚至直接内联方法体，消除压栈开销。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public final class FinalSecurityConfig {

    private final String systemSecretKey;
    private final int maxTokenExpirySeconds;

    public FinalSecurityConfig(String systemSecretKey, int maxTokenExpirySeconds) {
        this.systemSecretKey = systemSecretKey;
        this.maxTokenExpirySeconds = maxTokenExpirySeconds;
    }

    public void printConfigSummary() {
        System.out.println("   [Final类安全配置] 密钥前缀: " + systemSecretKey.substring(0, 4) + "****, 令牌有效时长: " + maxTokenExpirySeconds + " 秒");
    }

    public String getSystemSecretKey() {
        return systemSecretKey;
    }

    public int getMaxTokenExpirySeconds() {
        return maxTokenExpirySeconds;
    }
}
