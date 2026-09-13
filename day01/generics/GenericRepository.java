package generics;

/**
 * 泛型接口演示：通用数据持久层仓储契约规范。
 *
 * 核心考点：
 * 1. 泛型接口允许在接口声明中定义一个或多个类型参数，如 <T, ID>。
 * 2. 规范定义：由通用契约抽象增删改查标准行为，不同的业务实体直接继承该接口并指定具体类型。
 *
 * @param <T> 实体类型
 * @param <ID> 实体主键唯一标识类型
 * @author InterviewGuide
 * @version 1.0
 */
public interface GenericRepository<T, ID> {

    /**
     * 保存实体对象
     *
     * @param entity 待保存实体
     */
    void save(T entity);

    /**
     * 根据主键 ID 检索实体
     *
     * @param id 主键 ID
     * @return 检索到的实体对象
     */
    T findById(ID id);

    /**
     * 根据主键 ID 删除实体
     *
     * @param id 主键 ID
     */
    void deleteById(ID id);
}
