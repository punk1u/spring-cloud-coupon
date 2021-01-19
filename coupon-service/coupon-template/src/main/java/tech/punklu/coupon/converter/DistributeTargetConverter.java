package tech.punklu.coupon.converter;

import tech.punklu.coupon.constant.DistributeTarget;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 分发目标枚举属性转换器
 * AttributeConverter<X,Y>
 *     X:是实体属性的类型
 *     Y：是数据库字段的类型
 */
@Converter
public class DistributeTargetConverter implements AttributeConverter<DistributeTarget,Integer> {

    @Override
    public Integer convertToDatabaseColumn(DistributeTarget distributeTarget) {
        return distributeTarget.getCode();
    }

    @Override
    public DistributeTarget convertToEntityAttribute(Integer integer) {
        return DistributeTarget.of(integer);
    }
}
