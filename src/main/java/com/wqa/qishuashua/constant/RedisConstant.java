package com.wqa.qishuashua.constant;

/**
 * Redis常量
 */
public interface RedisConstant {
    /**
     * 用户签到记录的 Redis key 前缀
     */
    String USER_SIGN_IN_REDIS_KEY_PREFIX = "user:signIns";

    /**
     * 获取用户签到记录的 Redis key
     *
     * @param year  年份
     * @param userId 用户ID
     * @return Redis key
     */
    static String getUserSignInRedisKey(int year, long userId) {
        return String.format("%s:%d:%d", USER_SIGN_IN_REDIS_KEY_PREFIX, year, userId);
    }
}
