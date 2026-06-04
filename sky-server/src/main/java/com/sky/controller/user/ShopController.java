package com.sky.controller.user;

import com.sky.constant.RedisKeyConstant;
import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端店铺相关接口
 */
@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "用户端店铺相关接口")
@Slf4j
public class ShopController {

    private final RedisTemplate<String, Object> redisTemplate;

    public ShopController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 查询店铺营业状态
     *
     * @return 店铺营业状态：1 为营业，0 为打烊
     */
    @GetMapping("/status")
    @ApiOperation("用户端查询店铺营业状态")
    public Result<Integer> getStatus() {
        log.info("用户端查询店铺营业状态");

        Integer value = (Integer) redisTemplate.opsForValue().get(RedisKeyConstant.SHOP_STATUS);
        Integer status = value == null ? StatusConstant.DISABLE : value;

        return Result.success(status);
    }
}
