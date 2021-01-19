package tech.punklu.coupon.converter;

import tech.punklu.coupon.constant.CouponCategory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 优惠券分类枚举属性转换器
 * AttributeConverter<X,Y>
 *     X:是实体属性的类型
 *     Y：是数据库字段的类型
 */
@Converter
public class CouponCategoryConverter implements AttributeConverter<CouponCategory,String> {

    /**
     * 将当前实体属性x转换为y存储到数据库中
     * @param couponCategory
     * @return
     */
    @Override
    public String convertToDatabaseColumn(CouponCategory couponCategory) {
        return couponCategory.getCode();
    }

    /**
     * 将数据库中的列值y转换为实体属性x,查询操作时执行
     * @param s
     * @return
     */
    @Override
    public CouponCategory convertToEntityAttribute(String s) {
        return CouponCategory.of(s);
    }
}
