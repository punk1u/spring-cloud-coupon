package tech.punklu.coupon.service;

import tech.punklu.coupon.entity.CouponTemplate;

/**
 * 异步服务接口定义
 */
public interface IAsyncService {


    /**
     * 根据模板异步地创建优惠券码
     * @param couponTemplate
     */
    void asyncConstructCouponByTemplate(CouponTemplate couponTemplate);
}
