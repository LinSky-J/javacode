package annotations;

/**
 * 商品服务业务实体类：使用自定义注解装饰成员变量与业务方法，用于注解底层原理实测。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class ProductService {

    @TableField(columnName = "prod_id", isPrimaryKey = true)
    private Long productId;

    @TableField(columnName = "prod_title")
    private String productTitle;

    public ProductService(Long productId, String productTitle) {
        this.productId = productId;
        this.productTitle = productTitle;
    }

    @ApiOperation(value = "根据商品ID下架指定商品", author = "交易中台团队")
    public boolean offlineProduct(Long id) {
        System.out.println("   [业务执行] 商品 " + id + " 成功下架！");
        return true;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductTitle() {
        return productTitle;
    }
}
