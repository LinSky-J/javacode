package other;

/**
 * 学生实体类，包含学号、姓名、分数
 */
public class Student {

    private long id;
    private String name;
    private double score;

    public Student() {
    }

    public Student(long id, String name, double score) {
        this.id = id;
        this.name = name;
        this.score = score;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return String.format("Student{id=%d, name='%s', score=%.1f}", id, name, score);
    }
}
