package tech.punklu.coupon.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.punklu.coupon.dao.CouponTemplateDao;
import tech.punklu.coupon.entity.CouponTemplate;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.service.IAsyncService;
import tech.punklu.coupon.service.IBuildTemplateService;
import tech.punklu.coupon.vo.TemplateRequest;

/**
 * 构建优惠券模板接口实现
 */
@Slf4j
@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService {

    /**
     * 异步服务
     */
    @Autowired
    private IAsyncService asyncService;

    /**
     * 优惠券模板DAO
     */
    @Autowired
    private CouponTemplateDao templateDao;

    /**
     * 创建优惠券模板
     * @param request {@link TemplateRequest} 模板信息请求对象
     * @return {@link CouponTemplate} 优惠券模板实体
     * @throws CouponException
     */
    @Override
    public CouponTemplate buildTemplate(TemplateRequest request) throws CouponException {
        // 参数合法性校验
        if (!request.validate()){
            throw new CouponException("BuildTemplate Param Is Not Valid");
        }
        // 判断同名的优惠券是否存在
        if (null != templateDao.findByName(request.getName())){
            throw new CouponException("Exist Same Name Template!");
        }
        // 构造CouponTemplate并保存到数据库中
        CouponTemplate template = requestToTemplate(request);
        template = templateDao.save(template);
        // 根据优惠券模板异步生成优惠券码
        asyncService.asyncConstructCouponByTemplate(template);
        return template;
    }

    /**
     * 将TemplateRequest转换为优惠券实体类CouponTemplate
     * @param request
     * @return
     */
    private CouponTemplate requestToTemplate(TemplateRequest request){

        return new CouponTemplate(
                request.getName(),
                request.getLogo(),
                request.getDesc(),
                request.getCategory(),
                request.getProductLine(),
                request.getCount(),
                request.getUserId(),
                request.getTarget(),
                request.getRule()
        );
    }
}
