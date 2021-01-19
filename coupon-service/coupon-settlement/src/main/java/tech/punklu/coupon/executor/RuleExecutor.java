package tech.punklu.coupon.executor;

import tech.punklu.coupon.constant.RuleFlag;
import tech.punklu.coupon.vo.SettlementInfo;

/**
 * 优惠券模板规则处理器接口定义
 */
public interface RuleExecutor {

    /**
     * 规则类型的标记，标识是对哪一个计算规则的处理
     * @return
     */
    RuleFlag ruleConfig();

    /**
     * 优惠券规则的计算
     * @param settlementInfo 包含了选择的优惠券
     * @return 修正过的结算信息
     */
    SettlementInfo computeRule(SettlementInfo settlementInfo);
}
