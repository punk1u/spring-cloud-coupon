package tech.punklu.coupon.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.punklu.coupon.exception.CouponException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康检查接口
 */
@Slf4j
@RestController
@SuppressWarnings("all")
public class HealthCheck {

    /**
     *  服务发现客户端，微服务向Eureka Server注册后，可通过DiscoveryClient从Eureka Server获取所有微服务的信息
     */
    @Autowired
    private DiscoveryClient client;

    /**
     *  服务注册接口，提供了获取服务id的方法
     */
    @Autowired
    private Registration registration;

    @GetMapping("/health")
    private String health(){
        log.debug("view health api");
        return "CouponTemplate Is OK!";
    }

    /**
     * 异常测试接口
     * @return
     * @throws CouponException
     */
    @GetMapping("/exception")
    public String exception() throws CouponException{
        log.debug("view exception api");
        throw new CouponException("CouponTemplate Has Some Problem");
    }

    /**
     * 获取Eureka Server上的微服务元信息
     * @return
     */
    @GetMapping("/info")
    public List<Map<String,Object>> info(){
        // 大约需要等待两分钟时间才能获取到稳定的注册信息
        List<ServiceInstance> instances = client.getInstances(registration.getServiceId());
        List<Map<String,Object>> result = new ArrayList<>(instances.size());

        instances.forEach(i ->{
            Map<String,Object> info = new HashMap<>();
            // 服务id
            info.put("serviceId",i.getServiceId());
            // 服务的实例id
            info.put("instanceId",i.getInstanceId());
            // 获取服务端口号
            info.put("port",i.getPort());
            result.add(info);
        });
        return result;
    }
}
