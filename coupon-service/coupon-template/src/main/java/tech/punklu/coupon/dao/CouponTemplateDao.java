package tech.punklu.coupon.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.punklu.coupon.entity.CouponTemplate;

import java.util.List;

/**
 * 优惠券模板DAO定义
 */
public interface CouponTemplateDao extends JpaRepository<CouponTemplate,Integer> {

    /**
     * 根据模板名称查询模板
     * @param name
     * @return
     */
    CouponTemplate findByName(String name);

    /**
     * 根据available和expired标记查找模板记录
     * @param available
     * @param expired
     * @return
     */
    List<CouponTemplate> findAllByAvailableAndExpired(Boolean available,Boolean expired);

    /**
     * 根据expired标记查找模板记录
     * @param expired
     * @return
     */
    List<CouponTemplate> findAllByExpired(Boolean expired);


}
