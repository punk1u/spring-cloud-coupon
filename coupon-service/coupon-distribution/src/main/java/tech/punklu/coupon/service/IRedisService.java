package tech.punklu.coupon.service;

import tech.punklu.coupon.entity.Coupon;
import tech.punklu.coupon.exception.CouponException;

import java.util.List;

/**
 * Redis相关的操作服务接口定义
 * 1、用户的三个状态优惠券Cache 相关操作
 * 2、优惠券模板生成的优惠券码Cache 操作
 */
public interface IRedisService {

    /**
     * 根据userId和状态获取缓存的优惠券信息
     * @param userId 用户编号
     * @param status 优惠券状态
     * @return
     */
    List<Coupon> getCachedCoupons(Long userId,Integer status);

    /**
     * 保存空的优惠券列表到缓存中，防止缓存穿透
     * @param userId 用户id
     * @param status 优惠券状态列表
     */
    void saveEmptyCouponListToCache(Long userId,List<Integer> status);

    /**
     * 从Cache中尝试获取一个优惠券码
     * @param templateId 优惠券模板主键
     * @return 优惠券码
     */
    String tryToAcquireCouponCodeFromCache(Integer templateId);

    /**
     * 把优惠券信息放入缓存
     * @param userId 用户id
     * @param coupons 优惠券
     * @param status 优惠券状态
     * @return 保存成功的个数
     * @throws CouponException
     */
    Integer addCouponToCache(Long userId,List<Coupon> coupons,Integer status) throws CouponException;
}
