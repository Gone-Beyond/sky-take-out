package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Profile("dev")
@Api(tags = "开发环境用户测试接口")
@Slf4j
public class DevUserController {

    private final JwtProperties jwtProperties;

    public DevUserController(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @RequestMapping(value = "/dev-login", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation("开发环境用户假登录")
    public Result<UserLoginVO> devLogin(@RequestParam(defaultValue = "1") Long userId,
                                        @RequestParam(required = false) String openid) {
        log.info("开发环境用户假登录，userId: {}", userId);

        String devOpenid = openid == null || openid.isEmpty()
                ? "dev-openid-" + userId
                : openid;

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);

        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(userId)
                .openid(devOpenid)
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }
}
