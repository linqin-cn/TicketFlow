package org.linqin.business.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ConfirmOrderDoReq {

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 购票日期
     */
    @JsonFormat(pattern="yyyy-MM-dd",timezone = "GMT+8")
    @NotNull(message = "【购票日期】不能为空")
    private Date date;

    /**
     * 车次编号
     */
    @NotBlank(message = "【车次编号】不能为空")
    private String trainCode;

    /**
     * 出发站
     */
    @NotBlank(message = "【出发站】不能为空")
    private String start;

    /**
     * 结束站
     */
    @NotBlank(message = "【到达站】不能为空")
    private String end;

    /**
     * 余票ID，对应表**`daily_train_ticket`（每日余票表）的主键 ID**，值是一个雪花算法生成的 Long 型数字，比如 `1893746502394816768`
     * 对应哪一个车次的哪一个路段   传给后端方便找到该表进行库存扣减
     */
    @NotNull(message = "【余票ID】不能为空")
    private Long dailyTrainTicketId;

    /**
     * 车票，List，里面每个元素是**一个乘客买的一张具体车票**：ConfirmOrderTicketReq，比如用户一次买两张票就是
     * "tickets": [
     *   {"passengerName": "张三", "seatTypeCode": "2", "seat": "A1"},
     *   {"passengerName": "李四", "seatTypeCode": "2", "seat": "B2"}
     * ]
     */
    @NotEmpty(message = "【车票】不能为空")
    private List<ConfirmOrderTicketReq> tickets;

    /**
     * 验证码 -- 用户在页面上输入的验证码
     */
    @NotBlank(message = "【图片验证码】不能为空")
    private String imageCode;

    /**
     * 图像验证码token--Redis 里存验证码的 key，前端传入输入的验证码和token，后端用token去Redis里取出验证码，验证用户输入的验证码是否正确
     */
    @NotBlank(message = "【图片验证码】参数非法")
    private String imageCodeToken;

    /**
     * 日志跟踪号
     */
    private String logId;

    /**
     * 加入排队人数，用于体验排队功能
     */
    private int lineNumber;

    @Override
    public String toString() {
        return "ConfirmOrderDoReq{" +
                "memberId=" + memberId +
                ", date=" + date +
                ", trainCode='" + trainCode + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", dailyTrainTicketId=" + dailyTrainTicketId +
                ", tickets=" + tickets +
                ", imageCode='" + imageCode + '\'' +
                ", imageCodeToken='" + imageCodeToken + '\'' +
                ", logId='" + logId + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
