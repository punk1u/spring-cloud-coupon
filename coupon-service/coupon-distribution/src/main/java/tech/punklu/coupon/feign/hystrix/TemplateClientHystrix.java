package tech.punklu.coupon.feign.hystrix;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.punklu.coupon.feign.TemplateClient;
import tech.punklu.coupon.vo.CommonResponse;
import tech.punklu.coupon.vo.CouponTemplateSDK;

import java.util.*;

/**
 * 优惠券模板Feign接口的熔断降级策略
 */
@Slf4j
@Component
public class TemplateClientHystrix implements TemplateClient {

    /**
     * 查找所有可用的优惠券模板
     * @return
     */
    @Override
    public CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate() {
        log.error("[eureka-client-coupon-template] findAllUsableTemplate request error");
        return new CommonResponse<>(-1,"[eureka-client-coupon-template] request error", Collections.emptyList());
    }

    /**
     * 获取模板ids到CouponTemplateSDK的映射
     * @return
     */
    @Override
    public CommonResponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(Collection<Integer> ids) {
        log.error("[eureka-client-coupon-template] findIds2TemplateSDK request error");
        return new CommonResponse<>(-1,"[eureka-client-coupon-template] request error",new HashMap<>());
    }
}
