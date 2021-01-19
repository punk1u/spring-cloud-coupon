package tech.punklu.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 结算信息
 * 包含：
 * 1、userId
 * 2、商品信息
 * 3、优惠券列表
 * 4、结算结果金额(原价-优惠券优惠金额)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettlementInfo {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 商品信息
     */
    private List<GoodsInfo> goodsInfos;

    /**
     * 优惠券列表
     */
    private List<CouponAndTemplateInfo> couponAndTemplateInfos;

    /**
     * 是否使结算生效,即核销
     */
    private Boolean employ;

    /**
     * 结果结算金额(原价-优惠金额)
     */
    private Double cost;

    /**
     * 优惠券和模板信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CouponAndTemplateInfo{

        /**
         * Coupon优惠券的id
         */
        private Integer id;

        /**
         * 优惠券对应的模板对象
         */
        private CouponTemplateSDK template;

    }
}
