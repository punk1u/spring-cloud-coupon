package tech.punklu.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import tech.punklu.coupon.constant.PeriodType;

/**
 * 优惠券规则对象定义
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRule {


    // 优惠券过期规则
    private Expiration expiration;

    // 折扣规则
    private Discount discount;

    // 每个人最多可以领几张的限制
    private Integer limitation;

    // 适用范围 地域+商品类型
    private Usage usage;

    // 权重,可以和哪些优惠券叠加使用(同一类的优惠券不能叠加),list[],优惠券的唯一编码
    private String weight;

    /**
     * 有效期限规则
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Expiration{

        // 有效期规则，对应PeriodType的code字段
        private Integer period;

        // 有效间隔，只对变动型有效期有效
        private Integer gap;

        // 优惠券模板的失效日期,两类都有效
        private Long deadline;


        // 校验
        boolean validate(){
            return null != PeriodType.of(period) && gap > 0 && deadline >0;
        }
    }

    /**
     * 折扣，需要与类型配合决定
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Discount{

        // 额度:满减（20），折扣（85），立减（10）
        private Integer quota;

        // 基准，需要买多少才可用
        private Integer base;

        /**
         * 校验
         * @return
         */
        boolean validate(){
            return quota > 0 && base > 0;
        }
    }

    /**
     * 使用范围
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage{

        // 省份
        private String province;

        // 城市
        private String city;

        // 商品类型,list[]
        private String goodsType;

        /**
         * 校验
         */
        boolean validate(){
            return StringUtils.isNotEmpty(province)
                    && StringUtils.isNotEmpty(city)
                    && StringUtils.isNotEmpty(goodsType);
        }

    }

    /**
     * 校验功能
     * @return
     */
    public boolean validate(){
        return expiration.validate() && discount.validate() && limitation > 0 && usage.validate() && StringUtils.isNotEmpty(weight);
    }
}
