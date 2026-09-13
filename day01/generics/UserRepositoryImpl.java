package generics;

import java.util.HashMap;
import java.util.Map;

/**
 * 泛型接口实现类：用户数据仓储实现。
 *
 * 核心考点：
 * 1. 实现类在 implements GenericRepository<UserEntity, Long> 时，
 *    明确将泛型形参 T 绑定为 UserEntity，ID 绑定为 Long。
 * 2. 此时接口中的 save(T) 自动演化为 save(UserEntity)，findById(ID) 自动演化为 findById(Long)，
 *    彻底消除强转，提供全编译期类型约束。
 *
 * @author InterviewGuide
 * @version 1.0
 */
public class UserRepositoryImpl implements GenericRepository<UserEntity, Long> {

    private final Map<Long, UserEntity> databaseMap = new HashMap<>();

    @Override
    public void save(UserEntity entity) {
        if (entity != null && entity.getId() != null) {
            databaseMap.put(entity.getId(), entity);
            System.out.println("   [仓储层-数据保存] 成功保存用户信息: " + entity);
        }
    }

    @Override
    public UserEntity findById(Long id) {
        UserEntity user = databaseMap.get(id);
        System.out.println("   [仓储层-数据检索] 检索 ID=" + id + " 结果: " + user);
        return user;
    }

    @Override
    public void deleteById(Long id) {
        databaseMap.remove(id);
        System.out.println("   [仓储层-数据删除] 成功删除 ID=" + id + " 的用户");
    }
}
