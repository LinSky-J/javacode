package day02.stream;

/**
 * 电商商品订单实体类。
 * 用于 Stream 流式计算 API 的过滤、映射、分组、汇总实战演示。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class ProductOrder {

    private final String orderId;
    private final String category;
    private final double price;
    private final int quantity;
    private final String status; // "PAID", "UNPAID", "CANCELLED"

    public ProductOrder(String orderId, String category, double price, int quantity, String status) {
        this.orderId = orderId;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    public double getTotalAmount() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return "ProductOrder{" +
                "id='" + orderId + '\'' +
                ", category='" + category + '\'' +
                ", amount=" + getTotalAmount() +
                ", status='" + status + '\'' +
                '}';
    }
}
