package tech.punklu.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 校验请求中传递的token是否存在的Zuul过滤器
 */
@Slf4j
@Component
public class TokenFilter extends AbstractPreZuulFilter {

    /**
     * 请求的token校验
     * @return
     */
    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();
        // 打印请求的方法，URL
        log.info(String.format("%s request to %s",request.getMethod(),request.getRequestURL().toString()));
        Object token = request.getParameter("token");
        if (null == token){
            log.error("error: token is empty");
            // 401:没有权限访问
            return fail(401,"error: token is empty");
        }
        return success();
    }

    @Override
    public int filterOrder() {
        return 1;
    }
}
