package tech.punklu.coupon.converter;

import tech.punklu.coupon.constant.ProductLine;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 产品线枚举属性转换器
 * AttributeConverter<X,Y>
 *     X:是实体属性的类型
 *     Y：是数据库字段的类型
 */
@Converter
public class ProductLineConverter implements AttributeConverter<ProductLine,Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProductLine productLine) {
        return productLine.getCode();
    }

    @Override
    public ProductLine convertToEntityAttribute(Integer s) {
        return ProductLine.of(s);
    }
}
