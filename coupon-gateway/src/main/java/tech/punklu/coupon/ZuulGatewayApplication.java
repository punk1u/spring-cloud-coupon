package tech.punklu.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * 网关应用启动入口
 * 1、@EnableZuulProxy标识当前的应用是Zuul Server
 * 2、@SpringCloudApplication是组合注解，包含启动项目的@SpringBootApplication注解、发现Eureka Server的@EnableDiscoveryCient注解，
 * 以及实现网关层面Hystrix熔断的启动注解@EnableCircuitBreaker
 */
@EnableZuulProxy
@SpringCloudApplication
public class ZuulGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulGatewayApplication.class,args);
    }
}
