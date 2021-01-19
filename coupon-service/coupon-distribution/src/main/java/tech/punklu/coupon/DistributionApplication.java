package tech.punklu.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestTemplate;

/**
 * 分发微服务的启动入口
 */
@SpringBootApplication
@EnableEurekaClient // 作为Eureka Client
@EnableFeignClients // 需要调用其他微服务，所以引入Feign
@EnableCircuitBreaker // 启用Hystrix 断路器
@EnableJpaAuditing // 启动JPA 列自动填充
public class DistributionApplication {

    // Ribbon 会发现微服务，并做负载均衡
    // 定义Ribbon的RestTemplate,通过RestTemplate作为调用其他微服务的入口
    @Bean
    @LoadBalanced // 实现Ribbon的负载均衡
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(DistributionApplication.class,args);
    }

}
