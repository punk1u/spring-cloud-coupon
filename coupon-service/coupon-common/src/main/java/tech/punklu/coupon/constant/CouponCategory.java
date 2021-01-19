package tech.punklu.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 优惠券分类
 */
@Getter
@AllArgsConstructor
public enum  CouponCategory {

    MANJIAN("满减券","001"),
    ZHEKOU("折扣券","002"),
    LIJIAN("立减券","003");

    // 优惠券描述信息(分类)
    private String description;

    // 优惠券分类编码
    private String code;

    /**
     * 根据给定的code值查找对应的枚举值
     * @param code
     * @return
     */
    public static CouponCategory of(String code){
        // 检验code是否为空，为空则抛出异常
        Objects.requireNonNull(code);
        // values指代当前枚举类，将当前枚举类转为Stream，并从中过滤出code值等于给定code值的枚举值并返回，如果找不到对应的枚举值抛出参数异常
        return Stream.of(values()).filter(bean -> bean.code.equals(code)).findAny().orElseThrow(() -> new IllegalArgumentException(code + "not exists"));
    }

}
