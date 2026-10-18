package org.linqin.batch.feign;

import org.linqin.common.resp.CommonResp;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class BusinessFeignFallback implements BusinessFeign {

    @Override
    public String hello() { // 作为降级回复
        return "Business模块暂不可用--Fallback";
    }

    @Override
    public CommonResp<Object> genDaily(Date data) {
        return null;
    }
}
