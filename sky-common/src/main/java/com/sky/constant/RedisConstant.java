package com.sky.constant;


import java.util.concurrent.TimeUnit;

public class RedisConstant {
    public static final String DISH_KEY = "dish:";
    public static final Long DISH_TTL = 30L;
    public static final TimeUnit DISH_TTL_Unit = TimeUnit.MINUTES;

    public final static String SETMEAL_CACHE_KEY = "setmealCache";
    public final static String ORDER_STATISTICS = "statistics:orders";

    public static final String USER_STATISTICS = "statistics:user";
    public static final String TURNOVER_STATISTICS = "statistics:turnover";
    public static final String TOP10_STATISTICS = "statistics:top10";
}
