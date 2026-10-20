package org.linqin.common.util;

import cn.hutool.core.util.IdUtil;

/**
 * 封装hutool雪花算法
 */
public class SnowUtil {
    // 区分不同机房、不同机器
    private static long dataCenterId = 1;  //数据中心
    private static long workerId = 1;     //机器标识

    /**
     * 获取下一个雪花算法ID(long)
     * IdUtil.getSnowflake(workerId, dataCenterId) 返回`Snowflake` 对象，
     * Snowflake.nextId() 方法会生成一个唯一的 64 位 long 型 ID。
     * @return
     */
    public static long getSnowflakeNextId(){
        return IdUtil.getSnowflake(workerId, dataCenterId).nextId();// 生成 `long` 型 ID
    }

    /**
     * 获取下一个雪花算法ID（字符串形式）
     * @return
     */
    public static String getSnowflakeNextIdStr() {
        return IdUtil.getSnowflake(workerId, dataCenterId).nextIdStr();// 生成 `String` 型 ID
    }
}
