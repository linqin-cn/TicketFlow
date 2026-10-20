package org.linqin.business.domain;

import lombok.Data;

import java.util.Date;

/**
 * 车次实体类
 * 对应数据库表 train，存储列车车次的基础信息（车次号、车型、始发终到站及发车到达时间）
 *
 * @author linqin
 * @date 2026-10-20
 */
@Data
public class Train {
    private Long id;

    private String code;

    private String type;

    private String start;

    private String startPinyin;

    private Date startTime;

    private String end;

    private String endPinyin;

    private Date endTime;

    private Date createTime;

    private Date updateTime;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", code=").append(code);
        sb.append(", type=").append(type);
        sb.append(", start=").append(start);
        sb.append(", startPinyin=").append(startPinyin);
        sb.append(", startTime=").append(startTime);
        sb.append(", end=").append(end);
        sb.append(", endPinyin=").append(endPinyin);
        sb.append(", endTime=").append(endTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}
