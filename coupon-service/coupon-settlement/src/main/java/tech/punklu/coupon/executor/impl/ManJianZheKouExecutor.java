package tech.punklu.coupon.executor.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import tech.punklu.coupon.constant.CouponCategory;
import tech.punklu.coupon.constant.RuleFlag;
import tech.punklu.coupon.executor.AbstractExecutor;
import tech.punklu.coupon.executor.RuleExecutor;
import tech.punklu.coupon.vo.GoodsInfo;
import tech.punklu.coupon.vo.SettlementInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 满减+折扣优惠券结算规则执行器
 */
@Slf4j
@Component
public class ManJianZheKouExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * 定义这个结算规则执行器的类型
     * @return
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN_ZHEKOU;
    }


    /**
     * 优惠券规则的计算
     * @param settlementInfo 包含了选择的优惠券
     * @return 修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlementInfo) {
        // 获取商品原始总价
        double goodsSum = retain2Decimals(goodsCostSum(settlementInfo.getGoodsInfos()));
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlementInfo,goodsSum);
        // 如果不为空,说明不符合要求
        if (null != probability){
            log.debug("ManJian And Zhekou Template Is Not Match To GoodsType!");
            return probability;
        }
        SettlementInfo.CouponAndTemplateInfo manJian = null;
        SettlementInfo.CouponAndTemplateInfo zheKou = null;

        for (SettlementInfo.CouponAndTemplateInfo couponAndTemplateInfo : settlementInfo.getCouponAndTemplateInfos()) {
            // 如果优惠券模板属性等于满减
            if (CouponCategory.of(couponAndTemplateInfo.getTemplate().getCategory()) == CouponCategory.MANJIAN){
                manJian = couponAndTemplateInfo;
            }else {
                zheKou = couponAndTemplateInfo;
            }
        }
        assert null != manJian;
        assert null != zheKou;

        // 当前的优惠券和满减券如果不能共用（一起使用），清空优惠券，返回商品原价
        if (!isTemplateCanShared(manJian,zheKou)){
            log.debug("Current ManJian And ZheKou Can Not Shared!");
            settlementInfo.setCost(goodsSum);
            settlementInfo.setCouponAndTemplateInfos(Collections.emptyList());
            return settlementInfo;
        }

        // 使用到的满减和折扣的优惠券
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = new ArrayList<>();
        // 先计算满减
        // 满减优惠券的优惠达标价格
        double manJianBase = (double)manJian.getTemplate().getRule().getDiscount().getBase();
        // 满减优惠券的额度
        double manJianQuota = (double) manJian.getTemplate().getRule().getDiscount().getQuota();

        // 最终价格
        double targetSum = goodsSum;
        // 如果原价大于满减条件
        if (targetSum >= manJianBase){
            targetSum -= manJianQuota;
            ctInfos.add(manJian);
        }
        // 再计算折扣
        // 折扣的额度
        double zheKouQuota = (double)zheKou.getTemplate().getRule().getDiscount().getQuota();
        targetSum *= zheKouQuota * 1.0/100;
        ctInfos.add(zheKou);
        settlementInfo.setCouponAndTemplateInfos(ctInfos);
        settlementInfo.setCost(retain2Decimals(targetSum > minCost() ? targetSum : minCost()));
        log.debug("Use ManJian And ZheKou Coupon Make Goods Cost From {} To {}",goodsSum,settlementInfo.getCost());
        return settlementInfo;
    }

    /**
     *  校验商品类型与优惠券是否匹配
     *  满折+折扣优惠券的校验
     *  如果想要使用多类优惠券，则必须要清算信息里的所有的商品类型都包含在优惠券模板中定义的商品类型内，即差集为空
     * @param settlement 用户传递的计算信息
     * @return
     */
    @Override
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement) {
        log.debug("Check ManJian And ZheKou Is Match Or Not!");
        // 获取传递进来的清算信息里的商品类型
        List<Integer> goodsType = settlement.getGoodsInfos().stream().map(GoodsInfo::getType).collect(Collectors.toList());
        // 获取优惠券模板里定义的可以使用优惠券的商品类型
        List<Integer> templateGoodsType = new ArrayList<>();
        settlement.getCouponAndTemplateInfos().forEach(ct -> {
            templateGoodsType.addAll(JSON.parseObject(ct.getTemplate().getRule().getUsage().getGoodsType(),List.class));
        });
        // 如果想要使用多类优惠券，则必须要所有的商品类型都包含在内，即差集为空
        return CollectionUtils.isEmpty(CollectionUtils.subtract(goodsType,templateGoodsType));


    }

    /**
     * 判断两个优惠券是否能一起使用
     * 即校验TemplateRule中的weight是否满足条件
     * @return
     */
    private boolean isTemplateCanShared(SettlementInfo.CouponAndTemplateInfo manJian,
                                        SettlementInfo.CouponAndTemplateInfo zheKou){
        String manJianKey = manJian.getTemplate().getKey() + String.format("%04d",manJian.getTemplate().getId());

        String zheKouKey = zheKou.getTemplate().getKey() + String.format("%04d",zheKou.getTemplate().getId());

        List<String> allSharedKeysForManJian = new ArrayList<>();
        allSharedKeysForManJian.add(manJianKey);
        allSharedKeysForManJian.addAll(JSON.parseObject(manJian.getTemplate().getRule().getWeight(),List.class));

        List<String> allSharedKeysForZheKou = new ArrayList<>();
        allSharedKeysForZheKou.add(zheKouKey);
        allSharedKeysForZheKou.addAll(JSON.parseObject(zheKou.getTemplate().getRule().getWeight(),List.class));

        return CollectionUtils.isSubCollection(Arrays.asList(manJianKey,zheKouKey),allSharedKeysForManJian)
                || CollectionUtils.isSubCollection(Arrays.asList(manJianKey,zheKouKey),allSharedKeysForZheKou);
    }
}
