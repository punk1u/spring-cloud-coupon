package tech.punklu.coupon.controller;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.executor.ExecuteManager;
import tech.punklu.coupon.vo.SettlementInfo;

/**
 * 结算服务的controller
 */
@Slf4j
@RestController
public class SettlementController {

    /**
     * 结算规则执行管理器
     */
    @Autowired
    private ExecuteManager executeManager;

    /**
     * 优惠券结算
     * @param settlementInfo
     * @return
     * @throws CouponException
     */
    @PostMapping("/settlement/compute")
    public SettlementInfo computeRule(@RequestBody SettlementInfo settlementInfo) throws CouponException{
        log.info("Settlement : {}", JSON.toJSONString(settlementInfo));
        return executeManager.computeRule(settlementInfo);
    }
}
