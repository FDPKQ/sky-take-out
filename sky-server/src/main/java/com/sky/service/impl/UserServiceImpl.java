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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final static String WECHAT_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Resource
    private WeChatProperties weChatProperties;

    @Resource
    private UserMapper userMapper;

    @Override
    public User wechatLogin(UserLoginDTO userLoginDTO) {
        // get open id
        String openid = getOpenid(userLoginDTO);

        // null ?
        if (openid == null || openid.isEmpty()) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }


        // new user?
        User user = userMapper.getByOpenid(openid);

        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        return user;
    }

    private String getOpenid(UserLoginDTO userLoginDTO) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");

        String jsonResult = HttpClientUtil.doGet(WECHAT_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(jsonResult);
        return jsonObject.getString("openid");

    }
}
