package tech.punklu.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 模板微服务启动入口
 */
@EnableScheduling // 启动定时任务注解
@EnableJpaAuditing // 使用JPA将Mysql列属性自动赋值
@EnableEurekaClient // 将此微服务标识为Eureka Client，会自动向Eureka Server注册
@SpringBootApplication
public class TemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateApplication.class,args);
    }
}
