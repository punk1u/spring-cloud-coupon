package tech.punklu.coupon.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tech.punklu.coupon.constant.CouponStatus;
import tech.punklu.coupon.converter.CouponStatusConverter;
import tech.punklu.coupon.serialization.CouponSerialize;
import tech.punklu.coupon.vo.CouponTemplateSDK;

import javax.persistence.*;
import java.util.Date;

/**
 * 优惠券（用户领取的优惠券记录）实体表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class) // 实现属性列自动填充
@Table(name = "coupon")
@JsonSerialize(using = CouponSerialize.class) // 指定此实体类的序列号器
public class Coupon {

    /**
     * 自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增
    @Column(name = "id",nullable = false)
    private Integer id;

    /**
     * 关联优惠券模板的主键（逻辑外键，不是真正外键）
     */
    @Column(name = "template_id",nullable = false)
    private Integer templateId;

    /**
     * 领取用户
     */
    @Column(name = "user_id",nullable = false)
    private Long userId;

    /**
     * 优惠券码
     */
    @Column(name = "coupon_code",nullable = false)
    private String couponCode;

    /**
     * 领取时间
     */
    @CreatedDate //JPA 自动填充时间
    @Column(name = "assign_time",nullable = false)
    private Date assignTime;

    /**
     * 优惠券状态
     */
    @Column(name = "status",nullable = false)
    @Convert(converter = CouponStatusConverter.class) // 指定转换器以便实现枚举值与状态码的转换
    private CouponStatus status;

    /**
     * 用户优惠券对应的模板信息
     */
    @Transient
    private CouponTemplateSDK templateSDK;

    /**
     * 返回一个无效的Coupon 对象
     * @return
     */
    public static Coupon invalidCoupon(){
        Coupon  coupon = new Coupon();
        coupon.setId(-1);
        return coupon;
    }

    /**
     * 构造优惠券
     */
    public Coupon(Integer templateId,Long userId,String couponCode,CouponStatus status){
        this.templateId = templateId;
        this.userId = userId;
        this.couponCode = couponCode;
        this.status = status;
    }

}
