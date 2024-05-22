package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.service.ShopService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ShopServiceImpl implements ShopService {
    private static final String SHOP_STATUS_KEY = "SHOP_STATUS";
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Integer getStatus() {
        String status = stringRedisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        return status == null ? null : Integer.valueOf(status);
    }

    @Override
    public void setStatus(Integer status) {

        stringRedisTemplate.opsForValue().set(SHOP_STATUS_KEY, status.toString());
    }
}

