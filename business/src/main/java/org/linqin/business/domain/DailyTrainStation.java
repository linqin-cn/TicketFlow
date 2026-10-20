package org.linqin.business.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 车次经停站实体类
 * 对应数据库表 train_station，存储某车次沿途经停的车站信息（站序、站名、到发时间、停靠时长、里程）
 *
 * @author linqin
 * @date 2026-10-20
 */

@Data
public class DailyTrainStation {
    private Long id;

    private Date date;

    private String trainCode;

    private Integer index;

    private String name;

    private String namePinyin;

    private Date inTime;

    private Date outTime;

    private Date stopTime;

    private BigDecimal km;

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
        sb.append(", name=").append(name);
        sb.append(", namePinyin=").append(namePinyin);
        sb.append(", inTime=").append(inTime);
        sb.append(", outTime=").append(outTime);
        sb.append(", stopTime=").append(stopTime);
        sb.append(", km=").append(km);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}
