package tech.punklu.coupon.executor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.punklu.coupon.constant.RuleFlag;
import tech.punklu.coupon.executor.AbstractExecutor;
import tech.punklu.coupon.executor.RuleExecutor;
import tech.punklu.coupon.vo.CouponTemplateSDK;
import tech.punklu.coupon.vo.SettlementInfo;

/**
 * 折扣优惠券结算规则执行器
 */
@Slf4j
@Component
public class ZheKouExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * 指定是折扣结算规则
     * @return
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.ZHEKOU;
    }

    /**
     * 优惠券规则的计算
     * @param settlementInfo 包含了选择的优惠券
     * @return
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlementInfo) {
        // 获取商品总价
        double goodsSum = retain2Decimals(goodsCostSum(settlementInfo.getGoodsInfos()));
        // 判断商品类型与优惠券中定义的可以使用的商品类型是否匹配
        SettlementInfo propability = processGoodsTypeNotSatisfy(settlementInfo,goodsSum);
        if (null != propability){
            log.debug("ZheKou Template Is Not Match GoodsType!");
            return propability;
        }

        // 折扣优惠券可以直接使用，没有门槛
        CouponTemplateSDK templateSDK = settlementInfo.getCouponAndTemplateInfos().get(0).getTemplate();
        // 可用额度
        double quota = (double)templateSDK.getRule().getDiscount().getQuota();
        // 计算使用优惠券之后的价格
        // 当使用优惠券后的价格大于最小支付价格的时候，返回使用优惠券后的结算价格，当小于最小支付价格的时候，返回最小支付价格
        settlementInfo.setCost(retain2Decimals(goodsSum * (quota * 1.0 /100)) > minCost() ?
                retain2Decimals(goodsSum * (quota * 1.0 /100)) : minCost());
        log.debug("Use ZheKou Coupon Make Goods Cost From {} To {}",goodsSum,settlementInfo.getCost());
        return settlementInfo;
    }
}
