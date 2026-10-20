package org.linqin.business.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.linqin.business.domain.ConfirmOrder;
import org.linqin.business.enums.ConfirmOrderStatusEnum;
import org.linqin.business.mapper.ConfirmOrderMapper;
import org.linqin.business.req.ConfirmOrderDoReq;
import org.linqin.business.req.ConfirmOrderTicketReq;
import org.linqin.common.context.LoginMemberContext;
import org.linqin.common.exception.BusinessException;
import org.linqin.common.exception.BusinessExceptionEnum;
import org.linqin.common.util.SnowUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

// 把这个类**标记为业务层组件**,后续可通过`@Resource` / `@Autowired` 注入它
@Service
public class BeforeConfirmOrderService {
    private static final Logger LOG = LoggerFactory.getLogger(BeforeConfirmOrderService.class);

    // 注入mapper对象，方便操作 `confirm_order`
    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    // 令牌服务，校验**令牌余量**，**下单前校验令牌是否够用**：
    @Autowired
    private SkTokenService skTokenService;

    // RocketMQ消息模板
    @Resource
    public RocketMQTemplate rocketMQTemplate;

    // 出票服务，真正执行出票逻辑
    @Resource
    private ConfirmOrderService confirmOrderService;

    /**
     * 在确认订单之前执行的操作
     * @param req 确认订单请求参数
     * @return 订单ID
     */
    @SentinelResource(value = "beforeDoConfirm", blockHandler = "beforeDoConfirmBlock")
    public Long beforeDoConfirm(ConfirmOrderDoReq req) {
        Long id = null;
        // 根据前端传值，加入排队人数，模拟购票时的排队,根据req.getLineNumber()获取排队人数进行循环
        for (int i = 0; i < req.getLineNumber() + 1; i++) {
            req.setMemberId(LoginMemberContext.getId());
            // 校验令牌余量--日期，车码，上下文获取id
            boolean validSkToken = skTokenService.validSkToken(req.getDate(), req.getTrainCode(), LoginMemberContext.getId());
            if(validSkToken) {
                LOG.info("令牌校验通过");
            } else {
                LOG.info("令牌校验不通过");
                // 构造自定义BusinessException
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
            }

            //  获取请求中的日期和车次，始末站等信息
            Date date = req.getDate();
            String trainCode = req.getTrainCode();
            String start = req.getStart();
            String end = req.getEnd();
            List<ConfirmOrderTicketReq> tickets = req.getTickets();

            // 保存订单状态
            DateTime now = DateTime.now();
            ConfirmOrder confirmOrder = new ConfirmOrder();
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrder.setMemberId(req.getMemberId());
            confirmOrder.setDate(date);
            confirmOrder.setTrainCode(trainCode);
            confirmOrder.setStart(start);
            confirmOrder.setEnd(end);
            confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
            confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
            confirmOrder.setTickets(JSON.toJSONString(tickets));
            confirmOrderMapper.insert(confirmOrder);

            // 发送MQ排队购票
            ConfirmOrderMQDto confirmOrderMQDto = new ConfirmOrderMQDto();
            confirmOrderMQDto.setDate(req.getDate());
            confirmOrderMQDto.setTrainCode(req.getTrainCode());
            confirmOrderMQDto.setLogId(MDC.get("LOG_ID"));
            String reqJson = JSON.toJSONString(confirmOrderMQDto);
             LOG.info("排队购票，发送mq开始，消息：{}", reqJson);
             rocketMQTemplate.convertAndSend(RocketMQTopicEnum.CONFIRM_ORDER.getCode(), reqJson);
             LOG.info("排队购票，发送mq结束");
             // 执行完确认订单之前执行的操作，才开始执行确认订单的操作
             confirmOrderService.doConfirm(confirmOrderMQDto);
             id = confirmOrder.getId();
        }
        return id;
    }

    /**
     * 在确认订单之前执行的操作
     * 降级方法，需包含限流方法的所有参数和BlockException参数
     * @param req {date: "2025-01-15", trainCode: "G123", ...}
     * @param e Sentinel 的限流异常对象
     */
    public Long beforeDoConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.error("限流触发，请求被拒绝", e);
        return null;
    }
}
