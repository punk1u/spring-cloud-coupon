package tech.punklu.coupon.executor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.punklu.coupon.constant.RuleFlag;
import tech.punklu.coupon.executor.AbstractExecutor;
import tech.punklu.coupon.executor.RuleExecutor;
import tech.punklu.coupon.vo.CouponTemplateSDK;
import tech.punklu.coupon.vo.SettlementInfo;

import java.util.Collections;

/**
 * 满减优惠券结算规则执行器
 */
@Slf4j
@Component
public class ManJianExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * 标记是满减结算规则执行器
     * @return
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN;
    }

    /**
     * 优惠券规则的计算
     * @param settlementInfo 包含了选择的优惠券
     * @return
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlementInfo) {
        // 四舍五入、保留两位小数计算商品总价
        double goodsSum = retain2Decimals(goodsCostSum(settlementInfo.getGoodsInfos()));
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlementInfo,goodsSum);
        // 如果商品类型与优惠券适用类型不匹配，直接返回原价
        if (null != probability){
            log.debug("ManJian Template Is not Match To GoodsType!");
            return probability;
        }
        // 判断满减是否符合折扣标准
        // 获取传入的优惠券模板
        CouponTemplateSDK templateSDK = settlementInfo.getCouponAndTemplateInfos().get(0).getTemplate();
        // 获取可以满减地最低值
        double base = (double)templateSDK.getRule().getDiscount().getBase();
        double quota = (double) templateSDK.getRule().getDiscount().getQuota();

        // 如果不符合标准,则直接返回商品总价
        if (goodsSum < base){
            log.debug("Current Goods Cost Sum < ManJian Coupon Base!");
            // 优惠券不可用的情况下，将金额置为原价
            settlementInfo.setCost(goodsSum);
            // 优惠券不可用的情况下，将优惠券列表置为空
            settlementInfo.setCouponAndTemplateInfos(Collections.emptyList());
            return settlementInfo;
        }

        // 符合标准时，计算使用优惠券之后的价格 -结算
        // 当使用优惠券后的价格大于最小支付价格的时候，返回使用优惠券后的结算价格，当小于最小支付价格的时候，返回最小支付价格
        settlementInfo.setCost(retain2Decimals(goodsSum-quota)> minCost() ? (goodsSum - quota) : minCost()) ;
        log.debug("Use ManJian Coupon Make Goods Cost From {} To {}",goodsSum,settlementInfo.getCost());
        return settlementInfo;
    }
}
