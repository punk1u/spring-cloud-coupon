package tech.punklu.coupon.schedule;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tech.punklu.coupon.dao.CouponTemplateDao;
import tech.punklu.coupon.entity.CouponTemplate;
import tech.punklu.coupon.vo.TemplateRule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 定时任务,定时清理已过期的优惠券模板
 */
@Slf4j
@Component
public class ScheduledTask {

    /**
     * 优惠券模板DAO接口
     */
    @Autowired
    private CouponTemplateDao templateDao;

    /**
     * 下线已过期的优惠券模板
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 一个小时执行一次  60分钟 * 60秒 * 1000毫秒
    public void offlineCouponTemplate(){
        log.info("Start To Expire CouponTemplate");
        List<CouponTemplate> templates = templateDao.findAllByExpired(false);
        if (CollectionUtils.isEmpty(templates)){
            log.info("Done To Expire CouponTemplate");
            return;
        }
        Date cur = new Date();
        List<CouponTemplate> expiredTemplates = new ArrayList<>(templates.size());
        templates.forEach(t -> {
            // 根据优惠券模板规则中的 “过期规则” 校验模板是否过期
            TemplateRule rule = t.getRule();
            if (rule.getExpiration().getDeadline() < cur.getTime()){
                t.setExpired(true);
                expiredTemplates.add(t);
            }
        });

        if (CollectionUtils.isNotEmpty(expiredTemplates)){
            log.info("Expired CouponTemplate Num:{}",templateDao.saveAll(expiredTemplates));
        }
        log.info("Done To Expire CouponTemplate");
    }
}
