package other;

import java.util.Comparator;

/**
 * 学生排序比较器：
 * 排序规则：优先按分数降序排序（高分在前），若分数相同则按学号升序排序（小编号在前）。
 *
 * 技术重点：
 * 1. 避免使用减法 (s2.getScore() - s1.getScore())：
 *    - 浮点数相减转整型时可能因绝对值小于 1 导致精度截断为 0；
 *    - 整型相减可能发生数值溢出（Overflow / Underflow）导致符号反转。
 *    - 规范做法：必须使用 Double.compare() 与 Long.compare()。
 * 2. 区分 Comparator（定制排序策略）与 Comparable（自然内置排序）。
 */
public class StudentScoreComparator implements Comparator<Student> {

    @Override
    public int compare(Student s1, Student s2) {
        // 空值安全防御
        if (s1 == null && s2 == null) {
            return 0;
        }
        if (s1 == null) {
            return 1;
        }
        if (s2 == null) {
            return -1;
        }

        // 1. 第一优先级：分数降序（s2.score 与 s1.score 对比）
        int scoreComparison = Double.compare(s2.getScore(), s1.getScore());
        if (scoreComparison != 0) {
            return scoreComparison;
        }

        // 2. 第二优先级：若分数相同，按学号升序（s1.id 与 s2.id 对比）
        return Long.compare(s1.getId(), s2.getId());
    }
}
