package org.linqin.business.domain;

import lombok.Data;

import java.util.Date;

/**
 * 令牌实体类
 * 对应数据库表 sk_token，存储指定日期某车次的令牌余量（count），用于购票前快速校验车次是否还有余票
 *
 * @author linqin
 * @date 2026-10-20
 */
@Data
public class SkToken {
    /**
     * 主键 id
     */
    private Long id;

    /**
     * 日期
     */
    private Date date;

    /**
     * 车次
     */
    private String trainCode;

    /**
     * 令牌数量
     */
    private Integer count;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", date=").append(date);
        sb.append(", trainCode=").append(trainCode);
        sb.append(", count=").append(count);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}
