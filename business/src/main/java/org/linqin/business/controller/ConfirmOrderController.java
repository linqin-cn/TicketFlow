package org.linqin.business.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.linqin.business.req.ConfirmOrderDoReq;
import org.linqin.business.service.BeforeConfirmOrderService;
import org.linqin.business.service.ConfirmOrderService;
import org.linqin.common.exception.BusinessExceptionEnum;
import org.linqin.common.resp.CommonResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {
    private static final Logger LOG= LoggerFactory.getLogger(ConfirmOrderController.class);

    @Resource
    private BeforeConfirmOrderService beforeConfirmOrderService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("dev")
    private String env;

    @Resource
    private ConfirmOrderService confirmOrderService;

    // **Sentinel **注解，告诉Sentinel这个资源需要被限流/降级 保护，被限流时走指定的兜底方法
    // `confirmOrderDo` 只是**资源名**，不是方法名
    /**
     * 确认订单
     * @param req 确认订单请求参数
     * @return 订单ID
     */
    @SentinelResource(value = "confirmOrderDo", blockHandler = "doConfirmBlock")
    @PostMapping("do")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        if (!env.equals("dev")) { // 如果不是开发环境，需要图形验证码
            // 图像验证码校验
            String imageCodeToken = req.getImageCodeToken();
            String imageCode =  req.getImageCode();
            String imageCodeRedis = stringRedisTemplate.opsForValue().get(imageCodeToken);
            LOG.info("从Redis中获取到验证码：{}",imageCodeRedis);
            if (ObjectUtils.isEmpty(imageCodeRedis)) {
                return  new CommonResp<>(false, "验证码已过期，请重新获取",null);
            }
            if (!imageCode.equalsIgnoreCase(imageCodeRedis)) {
                return  new CommonResp<>(false, "验证码不正确",null);
            } else {
                // 验证通过，Redis中移除验证码
                stringRedisTemplate.delete(imageCodeToken);
            }
        }

        // 在 Controller 里调用 `BeforeConfirmOrderService.beforeDoConfirm()`，**执行购票前的全部准备逻辑，返回订单 ID 给前端**。
        Long id = beforeConfirmOrderService.beforeDoConfirm(req);
        return new CommonResp<>(String.valueOf(id));// id作为content返回
    }

    /**
     * 查询订单排队人数
     * @param id 订单ID
     * @return 排队人数
     */
    @GetMapping("/query-line-count/{id}")
    public CommonResp<Integer> queryLineCount(@PathVariable("id") Long id){
        Integer count = confirmOrderService.queryLineCount(id);
        return new CommonResp<>(count);
    }

    /**
     * 取消订单
     * @param id 订单ID
     * @return 取消结果
     */
    @GetMapping("/cancel/{id}")
    public CommonResp<Integer> cancel(@PathVariable Long id) {
        Integer count = confirmOrderService.cancel(id);
        return new CommonResp<>(count);
    }


    /** 在确认订单的操作的降级方法，需包含限流方法的所有参数和BlockException参数，且返回值要保持一致
     * 直接返回错误
     * @param req
     * @param e
     */
    public CommonResp<Object> doConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.info("ConfirmOrderController购票请求被限流：{}", req);
        // throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
        CommonResp<Object> commonResp = new CommonResp<>();
        commonResp.setSuccess(false);
        commonResp.setMessage(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION.getDesc());
        return commonResp;
    }
}

