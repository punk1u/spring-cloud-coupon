package tech.punklu.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class AccessLogFilter extends AbstractPostZuulFilter {

    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();
        // 从请求上下文中取出在PreRequestFilter中设置的请求时间戳
        Long startTime = (Long) context.get("startTime");
        String uri = request.getRequestURI();
        long duration = System.currentTimeMillis()-startTime;
        // 打印从网关通过的请求的uri以及执行花费的时间
        log.info("uri: {} , duration: {} ",uri,duration);
        return success();
    }

    @Override
    public int filterOrder() {
        // 设置执行时间为返回所有的过滤器之前
        return FilterConstants.SEND_RESPONSE_FILTER_ORDER-1;
    }
}
