package com.sky.controller.user;


import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user/user")
@Api(tags = "用户相关接口")
public class UserController {

    private final UserService userService;
    private final JwtProperties jwtProperties;

    public UserController(UserService userService, JwtProperties jwtProperties) {
        this.userService = userService;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 微信登录
     *
     * @param userLoginDTO 微信登录临时凭证
     * @return 用户信息和 JWT 令牌
     */
        @PostMapping("/login")
        @ApiOperation("微信登录")
        public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
            log.info("微信用户登录");
            log.info("user login request, codePresent={}", userLoginDTO != null && userLoginDTO.getCode() != null && !userLoginDTO.getCode().isEmpty());

            User user = userService.wxLogin(userLoginDTO);
            log.info("user login success from service, userId={}, openid={}", user.getId(), user.getOpenid());

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);
        log.info("user login jwt created, userId={}, ttl={}", user.getId(), jwtProperties.getUserTtl());

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();


        return Result.success(userLoginVO);
    }
}
