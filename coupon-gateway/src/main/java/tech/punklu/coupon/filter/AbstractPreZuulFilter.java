package tech.punklu.coupon.filter;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * Pre类型的抽象Zuul过滤器
 */
public abstract class AbstractPreZuulFilter extends AbstractZuulFilter {

    /**
     * 指定此过滤器类型为Pre类型
     * @return
     */
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }
}
