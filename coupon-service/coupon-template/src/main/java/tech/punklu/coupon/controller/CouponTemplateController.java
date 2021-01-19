package tech.punklu.coupon.controller;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.punklu.coupon.entity.CouponTemplate;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.service.IBuildTemplateService;
import tech.punklu.coupon.service.ITemplateBaseService;
import tech.punklu.coupon.vo.CouponTemplateSDK;
import tech.punklu.coupon.vo.TemplateRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 优惠券模板相关的功能控制器
 */
@Slf4j
@RestController
public class CouponTemplateController {

    /**
     * 构建优惠券模板Service
     */
    @Autowired
    private IBuildTemplateService buildTemplateService;

    /**
     * 优惠券模板基础服务
     */
    @Autowired
    private ITemplateBaseService templateBaseService;

    /**
     * 构造优惠券模板
     * @param request
     * @return
     * @throws CouponException
     */
    @PostMapping("/template/build")
    public CouponTemplate buildTemplate(@RequestBody  TemplateRequest request) throws CouponException{
        log.info("Build Template: {}", JSON.toJSONString(request));
        return buildTemplateService.buildTemplate(request);
    }

    /**
     * 构造优惠券模板详情
     * @param id
     * @return
     * @throws CouponException
     */
    @GetMapping("/template/info")
    public CouponTemplate buildTemplateInfo(@RequestParam("id") Integer id) throws CouponException{
        log.info("Build Template info for :{}",id);
        return templateBaseService.buildTemplateInfo(id);
    }

    /**
     * 查找所有可以用的优惠券模板
     * @return
     */
    @GetMapping("/template/sdk/all")
    public List<CouponTemplateSDK> findAllUsableTemplate(){
        log.info("Find All Usable Template");
        return templateBaseService.findAllUsableTemplate();
    }

    /**
     * 获取模板 ids 到 CouponTemplateSDK的映射
     * @param ids
     * @return
     */
    @GetMapping("/template/sdk/infos")
    public Map<Integer,CouponTemplateSDK> findIdsTemplateSDK(@RequestParam Collection<Integer> ids){
        log.info("FindIds2TemplateSDK : {}",JSON.toJSONString(ids));
        return templateBaseService.findIds2TemplateSDK(ids);
    }
}
