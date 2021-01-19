package tech.punklu.coupon.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tech.punklu.coupon.annotation.IgnoreResponseAdvice;
import tech.punklu.coupon.vo.CommonResponse;

/**
 * 对SpringMVC内置的ResponseBody进行增强
 * 拦截系统中所有Controller的返回,并作特殊处理
 */
@RestControllerAdvice // 组合了@ControllerAdvice和@ResponseBody注解，是对这两者的增强
public class CommonResponseDataAdvice implements ResponseBodyAdvice<Object> {

    /**
     * 判断是否需要对响应进行处理,需要处理的话执行下面的beforeBodyWrite方法
     * @param methodParameter controller方法的定义
     * @param aClass 消息转换器
     * @return
     */
    @Override
    public boolean supports(MethodParameter methodParameter,
                            Class<? extends HttpMessageConverter<?>> aClass) {
        // 判断当前方法所在的类标识了@IgnoreResponseAdvice注解，不需要处理
        if (methodParameter.getDeclaringClass().isAnnotationPresent(IgnoreResponseAdvice.class)){
            return false;
        }
        // 判断当前方法标识了@IgnoreResponseAdvice注解，不需要处理
        if (methodParameter.getMethod().isAnnotationPresent(IgnoreResponseAdvice.class)){
            return false;
        }
        // 对响应进行处理
        return true;
    }

    /**
     *
     * @param o controller返回对象
     * @param methodParameter controller的声明方法
     * @param mediaType
     * @param aClass
     * @param serverHttpRequest 请求对象
     * @param serverHttpResponse 响应对象
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object o,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass,
                                  ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {

        // 定义最终返回对象
        CommonResponse<Object> response = new CommonResponse<>(0,"");
        // 如果o 是null，即返回对象是空，response不需要设置data
        if (null == o){
            return response;
        }else if (o instanceof CommonResponse){ // 如果当前 o已经是CommonResponse类型，不需要再次处理
            response = (CommonResponse<Object>) o;
        }else {
            response.setData(o); // 否则把响应对象作为CommonResponse的data部分
        }
        return response;
    }
}
