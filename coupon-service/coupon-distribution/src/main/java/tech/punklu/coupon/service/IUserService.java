package tech.punklu.coupon.service;

import tech.punklu.coupon.entity.Coupon;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.vo.AcquireTemplateRequest;
import tech.punklu.coupon.vo.CouponTemplateSDK;
import tech.punklu.coupon.vo.SettlementInfo;

import java.util.List;

/**
 * 用户服务相关的接口定义
 * 1、用户的三类优惠券信息展示服务
 * 2、查看用户当前可以领取的优惠券模板 coupon-template 配合实现
 * 3、用户领取优惠券服务
 * 4、用户消费优惠券服务 - coupon-settlement结算微服务配合实现
 */
public interface IUserService {

    /**
     * 根据用户id和状态查询优惠券记录
     * @param userId 用户id
     * @param status 优惠券状态
     * @return
     * @throws CouponException
     */
    List<Coupon> findCouponsByStatus(Long userId,Integer status) throws CouponException;

    /**
     * 根据用户id查找当前可以领取的优惠券模板
     * @param userId 用户编号
     * @return
     * @throws CouponException
     */
    List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException;

    /**
     * 用户领取优惠券
     * @param request
     * @return
     * @throws CouponException
     */
    Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException;

    /**
     * 结算(核销)优惠券
     * @param info
     * @return
     * @throws CouponException
     */
    SettlementInfo settlement(SettlementInfo info) throws CouponException;
}
