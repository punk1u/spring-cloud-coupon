package tech.punklu.coupon.executor;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import tech.punklu.coupon.vo.GoodsInfo;
import tech.punklu.coupon.vo.SettlementInfo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则执行器抽象类,定义通用方法
 */
public abstract class AbstractExecutor {

    /**
     * 校验商品类型与优惠券是否匹配
     * 需要注意：
     * 1、这里实现的是单品类校验，多品类优惠券在自定义的executor中重载此方法
     * 2、商品只需要有一个优惠券要求的商品类型去匹配就可以
     * @param settlement
     * @return
     */
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement){
        // 获取传入的结算信息中所有的商品类型
        List<Integer> goodsType = settlement.getGoodsInfos().stream().map(GoodsInfo::getType).collect(Collectors.toList());
        // 获取模板支持的商品类型
        List<Integer> templateGoodsType = JSON.parseObject(
                settlement.getCouponAndTemplateInfos().get(0).getTemplate().getRule().getUsage().getGoodsType(),List.class);
        // 判断是否存在交集
        return CollectionUtils.isNotEmpty(
                CollectionUtils.intersection(goodsType,templateGoodsType)
        );
    }

    /**
     * 处理商品类型与优惠券限制不匹配的情况
     * @param settlementInfo 用户传递的结算信息
     * @param goodsSum 商品总价
     * @return 已经修改过的结算信息
     */
    protected SettlementInfo processGoodsTypeNotSatisfy(SettlementInfo settlementInfo,double goodsSum){
        boolean isGoodsTypeSatify = isGoodsTypeSatisfy(settlementInfo);
        // 不符合的情况下,直接返回总价，并清空优惠券
        if (!isGoodsTypeSatify){
            // 设置结算金额为原始总价
            settlementInfo.setCost(goodsSum);
            // 优惠券不满足条件，不可用的情况下，设置结算信息里的优惠券列表为空
            settlementInfo.setCouponAndTemplateInfos(Collections.emptyList());
            return settlementInfo;
        }
        return null;
    }

    /**
     * 计算商品总价
     * @param goodsInfos
     * @return
     */
    protected double goodsCostSum(List<GoodsInfo> goodsInfos){
        return goodsInfos.stream().mapToDouble(g -> g.getPrice() * g.getCount()).sum();
    }

    /**
     * 保留两位小数，四舍五入
     * @param value
     * @return
     */
    protected double retain2Decimals(double value){
        return new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 返回最小支付费用
     */
    protected double minCost(){
        return 0.1;
    }
}
