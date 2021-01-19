package tech.punklu.coupon.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.punklu.coupon.constant.CouponStatus;
import tech.punklu.coupon.entity.Coupon;

import java.util.List;

/**
 * CouponDAO接口定义
 */
public interface CouponDao extends JpaRepository<Coupon,Integer> {

    /**
     * 根据userId + 状态寻找优惠券记录
     * @param userId
     * @param status
     * @return
     */
    List<Coupon> findAllByUserIdAndStatus(Long userId, CouponStatus status);
}
