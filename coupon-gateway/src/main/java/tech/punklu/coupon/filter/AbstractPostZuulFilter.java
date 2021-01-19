package tech.punklu.coupon.filter;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * Post类型的抽象Zuul过滤器类
 */
public abstract class AbstractPostZuulFilter extends AbstractZuulFilter{

    /**
     * 指定此过滤器类型为Post类型
     * @return
     */
    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }
}
