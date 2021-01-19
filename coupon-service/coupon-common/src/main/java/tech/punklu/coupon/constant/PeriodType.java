package tech.punklu.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 有限期类型枚举
 */
@Getter
@AllArgsConstructor
public enum PeriodType {

    REGULAR("固定的（固定日期）",1),
    SHIFT("变动的（以领取之日开始计算）",2);

    // 有效期描述
    private String description;

    // 有限期编码
    private Integer code;

    /**
     * 根据给定的code值查找对应的枚举值
     * @param code
     * @return
     */
    public static PeriodType of(Integer code){
        // 检验code是否为空，为空则抛出异常
        Objects.requireNonNull(code);
        // values指代当前枚举类，将当前枚举类转为Stream，并从中过滤出code值等于给定code值的枚举值并返回，如果找不到对应的枚举值抛出参数异常
        return Stream.of(values()).filter(bean -> bean.code.equals(code)).findAny().orElseThrow(() -> new IllegalArgumentException(code + "not exists"));
    }
}
