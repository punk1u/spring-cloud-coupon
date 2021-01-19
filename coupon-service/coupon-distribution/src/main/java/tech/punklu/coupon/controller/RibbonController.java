package tech.punklu.coupon.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import tech.punklu.coupon.annotation.IgnoreResponseAdvice;

import java.util.List;
import java.util.Map;

/**
 * Ribbon 应用Controller
 */
@RestController
@Slf4j
public class RibbonController {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 通过Ribbon组件调用模板微服务
     * /coupon-distribution/info
     * @return
     */
    @GetMapping("/info")
    @IgnoreResponseAdvice
    public TemplateInfo getTemplateInfo(){
        String infoUrl = "http://eureka-client-coupon-template/coupon-template/info";
        return restTemplate.getForEntity(infoUrl,TemplateInfo.class).getBody();
    }

    /**
     * 模板微服务的元信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TemplateInfo{

        private Integer code;

        private String message;

        private List<Map<String,Object>> data;
    }
}
