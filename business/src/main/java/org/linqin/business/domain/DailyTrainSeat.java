package org.linqin.business.domain;

import lombok.Data;

import java.util.Date;

/**
 * 车次座位实体类
 * 对应数据库表 train_seat，存储某车次各座位的静态信息（所属车厢、排座号、座位类型、车厢内座位序号）
 *
 * @author linqin
 * @date 2026-10-20
 */
@Data
public class DailyTrainSeat {
    private Long id;

    private Date date;

    private String trainCode;

    private Integer carriageIndex;

    private String row;

    private String col;

    private String seatType;

    private Integer carriageSeatIndex;

    private String sell;

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
        sb.append(", carriageIndex=").append(carriageIndex);
        sb.append(", row=").append(row);
        sb.append(", col=").append(col);
        sb.append(", seatType=").append(seatType);
        sb.append(", carriageSeatIndex=").append(carriageSeatIndex);
        sb.append(", sell=").append(sell);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}
