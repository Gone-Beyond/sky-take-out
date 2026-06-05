package com.sky.constant;

/**
 * Redis key 常量类
 *
 * 统一管理项目中的 Redis key，避免在业务代码中到处手写字符串。
 */
public class RedisKeyConstant {

    /**
     * 店铺营业状态 key
     *
     * 对应 Redis 中的：
     * shop:status
     */
    public static final String SHOP_STATUS = "shop:status";

    /**
     * 按分类缓存菜品列表 key 前缀
     *
     * 最终 Redis key 示例：
     * dish:list:category:1
     * dish:list:category:2
     */
    public static final String DISH_LIST_CATEGORY_PREFIX = "dish:list:category:";

    /**
     * 根据分类 id 构造菜品列表缓存 key
     *
     * @param categoryId 分类 id
     * @return 菜品列表缓存 key
     */
    public static String getDishListCategoryKey(Long categoryId) {
        return DISH_LIST_CATEGORY_PREFIX + categoryId;
    }

    /**
     * 私有构造方法
     *
     * 工具类不需要创建对象，防止 new RedisKeyConstant()
     */
    private RedisKeyConstant() {
    }
}