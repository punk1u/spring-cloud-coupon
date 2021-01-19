package tech.punklu.coupon.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 定制Http消息转换器
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    /**
     * 配置Http消息的转换器
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //  清空现有对象
        converters.clear();
        // 使用MappingJackson2HttpMessageConverter实现Java类与Json的转换
        converters.add(new MappingJackson2HttpMessageConverter());
    }
}
