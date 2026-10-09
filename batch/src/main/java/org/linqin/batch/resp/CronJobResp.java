package org.linqin.batch.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * `@JsonInclude(JsonInclude.Include.NON_EMPTY)` 告诉 Jackson：**把对象序列化成 JSON 时，
 * 值为 "空" 的字段不要输出到 JSON 里**。
 * "空" 包括：`null`、空字符串 `""`、空集合 `[]`、空 Map 等。
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class CronJobResp {
    private String group;

    private String name;

    private String description;

    // 任务的状态
    private String state;

    private String cronExpression;

    // 任务的下次执行时间
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date nextFireTime;

    // 任务的上次执行时间
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date preFireTime;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CronJobDto{");
        sb.append("cronExpression='").append(cronExpression).append('\'');
        sb.append(", group='").append(group).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append(", nextFireTime=").append(nextFireTime);
        sb.append(", preFireTime=").append(preFireTime);
        sb.append('}');
        return sb.toString();
    }
}
