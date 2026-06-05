package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String WX_LOGIN =
            "https://api.weixin.qq.com/sns/jscode2session";

    private final UserMapper userMapper;
    private final WeChatProperties weChatProperties;

    public UserServiceImpl(UserMapper userMapper, WeChatProperties weChatProperties) {

        this.userMapper = userMapper;
        this.weChatProperties = weChatProperties;
    }

    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        log.info("wxLogin start, codePresent={}", userLoginDTO != null && userLoginDTO.getCode() != null && !userLoginDTO.getCode().isEmpty());

        String openid = getOpenid(userLoginDTO.getCode());
        log.info("wxLogin get openid result, openid={}", openid);

        if (openid == null) {
            log.warn("wxLogin failed, openid is null");
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        log.info("wxLogin query user by openid, openid={}", openid);
        User user = userMapper.getByOpenid(openid);

        if (user == null) {
            log.info("wxLogin user not found, create new user, openid={}", openid);
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
            log.info("wxLogin new user created, userId={}, openid={}", user.getId(), openid);
        } else {
            log.info("wxLogin user found, userId={}, openid={}", user.getId(), openid);
        }

        return user;
    }


    private String getOpenid(String code) {
        log.info("wxLogin request jscode2session, appid={}, codePresent={}", weChatProperties.getAppid(), code != null && !code.isEmpty());

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", code);
        paramMap.put("grant_type", "authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN, paramMap);
        log.info("wxLogin jscode2session response={}", json);

        if (json == null || json.isEmpty()) {
            log.warn("微信登录接口未返回数据");
            return null;
        }

        JSONObject jsonObject = JSON.parseObject(json);
        if (jsonObject == null || !jsonObject.containsKey("openid")) {
            log.warn("微信登录失败，errcode：{}，errmsg：{}",
                    jsonObject == null ? null : jsonObject.getString("errcode"),
                    jsonObject == null ? null : jsonObject.getString("errmsg"));
            return null;
        }

        return jsonObject.getString("openid");
    }
}
