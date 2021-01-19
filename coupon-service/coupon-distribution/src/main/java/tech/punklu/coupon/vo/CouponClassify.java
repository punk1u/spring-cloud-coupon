package tech.punklu.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.time.DateUtils;
import tech.punklu.coupon.constant.CouponStatus;
import tech.punklu.coupon.constant.PeriodType;
import tech.punklu.coupon.entity.Coupon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户优惠券的分类,根据优惠券状态
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponClassify {

    /**
     * 可用的优惠券
     */
    private List<Coupon> usable;

    /**
     * 已使用的
     */
    private List<Coupon> used;

    /**
     * 已过期的
     */
    private List<Coupon> expired;

    /**
     * 对当前的优惠券进行分类
     * @param coupons
     * @return
     */
    public static CouponClassify classify(List<Coupon> coupons){
        List<Coupon> usable = new ArrayList<>(coupons.size());
        List<Coupon> used = new ArrayList<>(coupons.size());
        List<Coupon> expired = new ArrayList<>(coupons.size());
        // 判断优惠券是否过期
        coupons.forEach(c -> {
            boolean isTimeExpire;
            long curTime = new Date().getTime();

            // 因为优惠券本身的过期是延迟的策略，所以需要对查询出的优惠券做过期判断
            // 固定日期过期的类型
            if (c.getTemplateSDK().getRule().getExpiration().getPeriod().equals(PeriodType.REGULAR.getCode())){
                isTimeExpire = c.getTemplateSDK().getRule().getExpiration().getDeadline() <= curTime;
            }else {
                // 可变动日期类型判断
                isTimeExpire = DateUtils.addDays(c.getAssignTime(),c.getTemplateSDK().getRule().getExpiration().getGap()).getTime() <= curTime;
            }
            if (c.getStatus() == CouponStatus.USED){
                used.add(c);
            }else if(c.getStatus() == CouponStatus.EXPIRED || isTimeExpire){
                expired.add(c);
            }else {
                usable.add(c);
            }
        });
        return new CouponClassify(usable,used,expired);
    }
}
