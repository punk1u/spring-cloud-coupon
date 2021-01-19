package tech.punklu.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * fake 商品信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsInfo {

    /**
     *  商品类型{@link tech.punklu.coupon.constant.GoodsType}
     */
    private Integer type;

    /**
     * 商品价格
     */
    private Double price;

    /**
     * 商品数量
     */
    private Integer count;
}
