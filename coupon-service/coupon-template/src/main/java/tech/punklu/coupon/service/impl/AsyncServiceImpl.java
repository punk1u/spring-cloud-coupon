package tech.punklu.coupon.service.impl;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.punklu.coupon.constant.Constant;
import tech.punklu.coupon.dao.CouponTemplateDao;
import tech.punklu.coupon.entity.CouponTemplate;
import tech.punklu.coupon.service.IAsyncService;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 异步服务接口实现
 */
@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {

    @Autowired
    private CouponTemplateDao couponTemplateDao;

    /**
     * Redis模板类注入
     */
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 根据模板异步地创建优惠券码
     * @param couponTemplate
     */
    @Async("getAsyncExecutor") // 指定异步执行并指定之前自定义的线程池
    @Override
    public void asyncConstructCouponByTemplate(CouponTemplate couponTemplate) {
        Stopwatch watch = Stopwatch.createStarted();
        Set<String> couponCodes = buildCouponCode(couponTemplate);

        // 获取Redis key
        String redisKey = String.format("%s%s", Constant.RedisPrefix.COUPON_TEMPLATE,couponTemplate.getId().toString());
        log.info("Push CouponCode To Redis :{}",redisTemplate.opsForList().rightPushAll(redisKey,couponCodes));
        // 优惠券模板生成后设置为可用状态
        couponTemplate.setAvailable(true);
        couponTemplateDao.save(couponTemplate);
        watch.stop();
        log.info("Construct Coupon By Template Cost : {}ms",watch.elapsed(TimeUnit.MILLISECONDS));
        // TODO 发送短信邮件通知优惠券模板已经可用
        log.info("CouponTemplate({}) Is available",couponTemplate.getId());
    }

    /**
     * 构造优惠券码
     * 优惠券码（对应于每一张优惠券，18位信息）：前四位（产品线1位 + 类型） + 中间6位（日期随机） + 后8位（0-9的随机数构成）
     * @param couponTemplate {@link CouponTemplate}
     * @return Set<String> 与couponTemplate的count值相同个数的优惠券码
     */
    private Set<String> buildCouponCode(CouponTemplate couponTemplate){
        Stopwatch watch = Stopwatch.createStarted();
        Set<String> result = new HashSet<>(couponTemplate.getCount());

        // 前四位的生成 (产品线+类型)
        String prefix4 = couponTemplate.getProductLine().getCode().toString() + couponTemplate.getCategory().getCode();
        // 创建时间
        String date = new SimpleDateFormat("yyMMdd").format(couponTemplate.getCreateTime());

        for (int i = 0;i != couponTemplate.getCount();++i){
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }
        while (result.size() < couponTemplate.getCount()){
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }
        assert result.size() == couponTemplate.getCount();

        watch.stop();
        log.info("Build Coupon code  cost :{}ms",watch.elapsed(TimeUnit.MILLISECONDS));
        return result;
    }

    /**
     * 构造优惠券码的后14位
     * @param date 创建时间
     * @return 14 位优惠券码
     */
    private String buildCouponCodeSuffix14(String date){
        char[] bases = new char[]{'1','2','3','4','5','6','7','8','9'};
        // 中间6位
        // 获取日期里的所有字符集合
        List<Character> chars = date.chars().mapToObj(e -> (char)e).collect(Collectors.toList());
        Collections.shuffle(chars);
        String mid6 = chars.stream().map(Object::toString).collect(Collectors.joining());
        // 后8位
        String suffix8 = RandomStringUtils.random(1,bases) + RandomStringUtils.randomNumeric(7);
        return mid6 + suffix8;
    }
}
