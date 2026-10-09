package org.linqin.batch.req;

import lombok.Data;

@Data
public class CronJobReq {
    // 任务的组
    private String group;
    // 任务的名字
    private String name;
    // 任务的描述
    private String description;
    // 任务的表达式
    private String cronExpression;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CronJobDto{");
        sb.append("cronExpression='").append(cronExpression).append('\'');
        sb.append(", group='").append(group).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
