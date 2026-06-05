package com.sky.controller.admin;


import com.sky.constant.RedisKeyConstant;
import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
@Api(tags = "店铺管理")
public class ShopController {

    // 注入 RedisTemplate
    private final RedisTemplate<String, Object> redisTemplate;
    // 构造方法
    public ShopController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置营业状态
     *
     * @param status
     * @return
     */
    @ApiOperation("设置店铺营业状态")
    @PutMapping("/{status}")
    public Result<String> setStatus(@PathVariable Integer status) {
        // 记录状态日志
        log.info("设置店铺营业状态 status: {}", status == 1 ? "营业" : "打烊");

        // 参数合法性校验：店铺状态只能是 0 或 1
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("店铺状态参数非法");
        }

        // 将店铺状态写入 Redis
        redisTemplate.opsForValue().set(RedisKeyConstant.SHOP_STATUS, status);
        log.info("shop status redis write success, key={}, status={}", RedisKeyConstant.SHOP_STATUS, status);

        return Result.success();
    }

    /**
     * 管理端查询营业状态
     *
     * @return
     */
    @ApiOperation("查询店铺营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        log.info("管理端查询营业状态");

        Integer value = (Integer) redisTemplate.opsForValue().get(RedisKeyConstant.SHOP_STATUS);
        Integer status = value == null ? StatusConstant.DISABLE : value;
        log.info("admin shop status redis read, key={}, rawValue={}, resultStatus={}", RedisKeyConstant.SHOP_STATUS, value, status);

        return Result.success(status);

    }

}
