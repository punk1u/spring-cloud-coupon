package tech.punklu.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 产品线枚举,用于标识优惠券可用于哪个产品线
 */
@Getter
@AllArgsConstructor
public enum  ProductLine {

    DAMAO("大猫",1),
    DABAO("大宝",2);

    // 产品线描述
    private String description;

    // 产品线编码
    private Integer code;

    /**
     * 根据给定的code值查找对应的枚举值
     * @param code
     * @return
     */
    public static ProductLine of(Integer code){
        // 检验code是否为空，为空则抛出异常
        Objects.requireNonNull(code);
        // values指代当前枚举类，将当前枚举类转为Stream，并从中过滤出code值等于给定code值的枚举值并返回，如果找不到对应的枚举值抛出参数异常
        return Stream.of(values()).filter(bean -> bean.code.equals(code)).findAny().orElseThrow(() -> new IllegalArgumentException(code + "not exists"));
    }

}
