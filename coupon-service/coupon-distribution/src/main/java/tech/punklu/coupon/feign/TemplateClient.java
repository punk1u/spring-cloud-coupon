package tech.punklu.coupon.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import tech.punklu.coupon.feign.hystrix.TemplateClientHystrix;
import tech.punklu.coupon.vo.CommonResponse;
import tech.punklu.coupon.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 优惠券模板微服务 Feign接口定义
 */
// value值即为要调用的微服务的sprng allication name中定义的值,fallback指定服务熔断兜底处理类
@FeignClient(value = "eureka-client-coupon-template",fallback = TemplateClientHystrix.class)
public interface TemplateClient {

    /**
     * 查找所有可用的优惠券模板
     * @return
     */
    // value即为在模板微服务中定义的findAllUsableTemplate的@GetMapping的value
    @RequestMapping(value = "/coupon-template/template/sdk/all",method = RequestMethod.GET)
    CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate();

    /**
     * 获取模板ids到CouponTemplateSDK的映射
     * @return
     */
    @RequestMapping(value = "/coupon-template/template/sdk/infos",method = RequestMethod.GET)
    CommonResponse<Map<Integer,CouponTemplateSDK>> findIds2TemplateSDK(@RequestParam("ids") Collection<Integer> ids);
}
