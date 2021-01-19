package tech.punklu.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 优惠券Kafka消息对象定义
 * 用法：把id为ids的优惠券的状态更新为status
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponKafkaMessage {

    /**
     * 优惠券状态
     */
    private Integer status;

    /**
     * Coupon主键
     */
    private List<Integer> ids;

}
