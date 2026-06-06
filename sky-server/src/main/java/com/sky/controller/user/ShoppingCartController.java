package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端购物车相关接口。
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "用户端购物车相关接口")
@Slf4j
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }


    /**
     * 添加商品到购物车。
     *
     * 前端传入 dishId 表示添加菜品，传入 setmealId 表示添加套餐。
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("用户端添加购物车，参数：{}", shoppingCartDTO);

        shoppingCartService.addShoppingCart(shoppingCartDTO);

        log.info("用户端添加购物车成功");
        return Result.success();
    }


    /**
     * 查看当前登录用户的购物车列表。
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list() {
        log.info("用户端查看购物车");

        List<ShoppingCart> list = shoppingCartService.showShoppingCart();

        log.info("用户端查看购物车成功，数量：{}", list.size());
        return Result.success(list);
    }

    /**
     * 清空当前登录用户的购物车。
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean() {
        log.info("用户端清空购物车");

        shoppingCartService.cleanShoppingCart();

        log.info("用户端清空购物车成功");
        return Result.success();
    }
}
