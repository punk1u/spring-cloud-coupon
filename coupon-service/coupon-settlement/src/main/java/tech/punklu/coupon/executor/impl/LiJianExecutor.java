package tech.punklu.coupon.executor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.punklu.coupon.constant.RuleFlag;
import tech.punklu.coupon.executor.AbstractExecutor;
import tech.punklu.coupon.executor.RuleExecutor;
import tech.punklu.coupon.vo.CouponTemplateSDK;
import tech.punklu.coupon.vo.SettlementInfo;

/**
 * 立减优惠券结算规则执行器
 */
@Slf4j
@Component
public class LiJianExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * 标识是立减优惠券结算规则执行器
     * @return
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.LIJIAN;
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
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlementInfo,goodsSum);
        // 清算信息里的商品类型和优惠券模板里定义的优惠券适用类型不匹配
        if (null != probability){
            log.debug("LiJian Template Is Not Match To GoodsType!");
            return probability;
        }
        // 立减优惠券直接使用，没有门槛
        CouponTemplateSDK templateSDK = settlementInfo.getCouponAndTemplateInfos().get(0).getTemplate();
        // 额度
        double quota = (double)templateSDK.getRule().getDiscount().getQuota();
        // 计算使用优惠券之后的价格 - 结算
        settlementInfo.setCost(retain2Decimals(goodsSum - quota) > minCost() ?
                retain2Decimals(goodsSum - quota): minCost());
        log.debug("Use LiJian Coupon Make Goods Cost From {} To {}",goodsSum,settlementInfo.getCost());
        return settlementInfo;
    }
}
