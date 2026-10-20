package org.linqin.business.domain;

import lombok.Data;

import java.util.Date;

/**
 * 车次车厢实体类
 * 对应数据库表 train_carriage，存储某车次各车厢的基础信息（车厢序号、座位类型、座位数、行列布局）
 *
 * @author linqin
 * @date 2026-10-20
 */

@Data
public class DailyTrainCarriage {
    private Long id;

    private Date date;

    private String trainCode;

    private Integer index;

    private String seatType;

    private Integer seatCount;

    private Integer rowCount;

    private Integer colCount;

    private Date createTime;

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
        sb.append(", index=").append(index);
        sb.append(", seatType=").append(seatType);
        sb.append(", seatCount=").append(seatCount);
        sb.append(", rowCount=").append(rowCount);
        sb.append(", colCount=").append(colCount);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}
