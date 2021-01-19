package tech.punklu.coupon.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.feign.hystrix.SettlementClientHystrix;
import tech.punklu.coupon.vo.CommonResponse;
import tech.punklu.coupon.vo.SettlementInfo;

/**
 * 优惠券结算微服务 Feign接口
 */
// value与结算微服务中的spring application name的值一致,fallback指定服务熔断兜底策略
@FeignClient(value = "eureka-client-coupon-settlement",fallback = SettlementClientHystrix.class)
public interface SettlementClient {

    /**
     * 优惠券规则计算
     * @param settlement
     * @return
     * @throws CouponException
     */
    @RequestMapping(value = "/coupon-settlement/settlement/compute",method = RequestMethod.POST)
    CommonResponse<SettlementInfo> computeRule(@RequestBody  SettlementInfo settlement) throws CouponException;
}
