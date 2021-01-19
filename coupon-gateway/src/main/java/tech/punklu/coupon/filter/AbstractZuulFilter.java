package tech.punklu.coupon.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

/**
 * 通用的抽象Zuul网关过滤器(需要继承ZuulFilter类)
 * ZuulFilter方法：
 * 1、filterType():过滤器类型，可为pre,route,post和error,route负责分发到具体微服务，pre和post分别是在此之前和之后，error是错误过滤器
 * 2、filterOrder()：指定过滤器执行顺序，越小越先执行，需要注意的是只对同一类型的过滤器进行排序
 * 3、shouldFilter():返回boolean类型，true时表示执行该过滤器的run方法，false表示不执行，可用于根据条件判断是否执行
 * 4、run():过滤器的实际执行方法
 */
public abstract class AbstractZuulFilter extends ZuulFilter {

    // 用于在过滤器之间传递信息,数据保存在每个请求的ThreadLocal中
    // 扩展了Map
    RequestContext context;

    // 标识请求是否已经结束，用于判断是否继续向下执行
    private final static String NEXT = "next";

    @Override
    public boolean shouldFilter() {
        // 获取当前线程的RequestContext
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取是否包含名为next的key，默认值为true，即为继续向下执行
        return (boolean)ctx.getOrDefault(NEXT,true);
    }

    @Override
    public Object run() throws ZuulException {
        // 请求上下文初始化
        context = RequestContext.getCurrentContext();
        return cRun();
    }

    // 抽象的Zuul过滤器类，不要有真正的实现类,所以定义为抽象方法
    protected abstract Object cRun();

    // 过滤不通过的情况下的返回信息
    Object fail(int code,String msg){
        // 执行失败，把next设为false，过滤器不再继续向下执行
        context.set(NEXT,false);
        // 设置Zuul对请求返回false
        context.setSendZuulResponse(false);
        // 设置响应的内容类型
        context.getResponse().setContentType("text/html;charset=UTF-8");
        // 设置返回错误的响应码
        context.setResponseStatusCode(code);
        // 设置响应内容
        context.setResponseBody(String.format("{\"result\": \"%s!\"}",msg));
        return null;
    }

    // 设置过滤成功情况下的返回信息
    Object success(){
        context.set(NEXT,true);
        return null;
    }
}
