package tech.punklu.coupon.feign.hystrix;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.feign.SettlementClient;
import tech.punklu.coupon.vo.CommonResponse;
import tech.punklu.coupon.vo.SettlementInfo;

/**
 * 结算微服务熔断策略实现
 */
@Slf4j
@Component
public class SettlementClientHystrix implements SettlementClient {

    /**
     * 优惠券规则计算
     * @param settlement
     * @return
     * @throws CouponException
     */
    @Override
    public CommonResponse<SettlementInfo> computeRule(SettlementInfo settlement) throws CouponException {
        log.error("[eureka-client-coupon-settlement] computeRule request error" );
        settlement.setEmploy(false);
        settlement.setCost(-1.0);
        return new CommonResponse<>(-1,"[eureka-client-coupon-settlement] request error",settlement);
    }
}
