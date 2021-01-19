package tech.punklu.coupon.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.punklu.coupon.dao.CouponTemplateDao;
import tech.punklu.coupon.entity.CouponTemplate;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.service.ITemplateBaseService;
import tech.punklu.coupon.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 优惠券模板基础服务接口实现
 */
@Slf4j
@Service
public class TemplateBaseServiceImpl implements ITemplateBaseService {

    /**
     * 优惠券模板DAO注入
     */
    @Autowired
    private CouponTemplateDao couponTemplateDao;

    /**
     * 根据优惠券模板id获取优惠券模板信息
     * @param id 模板id
     * @return 优惠券模板
     * @throws CouponException
     */
    @Override
    public CouponTemplate buildTemplateInfo(Integer id) throws CouponException {
        Optional<CouponTemplate> template = couponTemplateDao.findById(id);
        if (!template.isPresent()){
            throw new CouponException("Template Is Not Exist:" + id);
        }
        return template.get();
    }


    /**
     * 查找所有可用的优惠券模板信息
     * @return
     */
    @Override
    public List<CouponTemplateSDK> findAllUsableTemplate() {
        // 查询可以使用且没有过期的,但是因为使用定时任务处理过期优惠券模板，所以这里的数据可能存在差异
        List<CouponTemplate> templates = couponTemplateDao.findAllByAvailableAndExpired(true,false);
        return templates.stream().map(this::template2TemplateSDK).collect(Collectors.toList());
    }


    /**
     * 获取模板ids 到 CouponTemplateSDK的映射
     * @param ids 模板ids
     * @return Map<key : 模板id，value：CouponTemplateSDK>
     */
    @Override
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids) {
        List<CouponTemplate> templates = couponTemplateDao.findAllById(ids);
        // 将查询结果拼装为Map<Integer,CouponTemplateSDK>
        return templates.stream().map(this::template2TemplateSDK).collect(Collectors.toMap(CouponTemplateSDK::getId, Function.identity()));
    }

    /**
     * 将CouponTemplate转换为CouponTemplateSDK
     * @param template
     * @return
     */
    private CouponTemplateSDK template2TemplateSDK(CouponTemplate template){
        return new CouponTemplateSDK(
            template.getId(),
            template.getName(),
            template.getLogo(),
            template.getDesc(),
            template.getCategory().getCode(),
            template.getProductLine().getCode(),
            template.getKey(), // 并不是拼装好的key,只是优惠券模板,还不是优惠券
            template.getTarget().getCode(),
            template.getRule()
        );
    }
}
