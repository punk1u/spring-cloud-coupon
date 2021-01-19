package tech.punklu.coupon.service;

import tech.punklu.coupon.entity.CouponTemplate;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 优惠券模板基础服务定义
 */
public interface ITemplateBaseService {

    /**
     * 根据优惠券模板id获取优惠券模板信息
     * @param id 模板id
     * @return 优惠券模板
     * @throws CouponException
     */
    CouponTemplate buildTemplateInfo(Integer id) throws CouponException;


    /**
     * 查找所有可用的优惠券模板信息
     * @return
     */
    List<CouponTemplateSDK> findAllUsableTemplate();


    /**
     * 获取模板ids 到 CouponTemplateSDK的映射
     * @param ids 模板ids
     * @return Map<key : 模板id，value：CouponTemplateSDK>
     */
    Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids);
}
