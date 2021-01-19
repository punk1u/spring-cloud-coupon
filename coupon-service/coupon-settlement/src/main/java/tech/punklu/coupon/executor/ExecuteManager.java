package tech.punklu.coupon.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import tech.punklu.coupon.constant.CouponCategory;
import tech.punklu.coupon.constant.RuleFlag;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.vo.SettlementInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券结算规则执行管理器
    即根据用户的请求（SettlementInfo）找到对应的Executor
    BeanPostProcessor：Bean后置处理器，当所有的Bean都被创建出来后才会执行
 */
@Slf4j
@Component
public class ExecuteManager implements BeanPostProcessor {


    /**
     * 规则执行器映射
     */
    private static Map<RuleFlag,RuleExecutor> executorIndex = new HashMap<>(RuleFlag.values().length);

    /**
     * 优惠券结算规则计算的入口
     * 注意：一定要保证传递进来的优惠券个数 >= 1
     * @param settlementInfo
     * @return
     * @throws CouponException
     */
    public SettlementInfo computeRule(SettlementInfo settlementInfo) throws CouponException {
        SettlementInfo result = null;
        // 单类优惠券
        if (settlementInfo.getCouponAndTemplateInfos().size()  == 1){
            // 获取优惠券的类别
            CouponCategory category = CouponCategory.of(settlementInfo.getCouponAndTemplateInfos().get(0).getTemplate().getCategory());
            switch (category){
                case MANJIAN:
                    result = executorIndex.get(RuleFlag.MANJIAN).computeRule(settlementInfo);
                    break;
                case ZHEKOU:
                    result = executorIndex.get(RuleFlag.ZHEKOU).computeRule(settlementInfo);
                    break;
                case LIJIAN:
                    result = executorIndex.get(RuleFlag.LIJIAN).computeRule(settlementInfo);
                    break;
            }
        }else {
            // 多类优惠券,包含的优惠券种类
            List<CouponCategory> categories = new ArrayList<>(settlementInfo.getCouponAndTemplateInfos().size());
            settlementInfo.getCouponAndTemplateInfos().forEach(ct -> categories.add(CouponCategory.of(ct.getTemplate().getCategory())));
            // 只支持两种优惠券类型
            if (categories.size() != 2){
                throw new CouponException("Not Support For More Template Category");
            }else {
                if (categories.contains(CouponCategory.MANJIAN) && categories.contains(CouponCategory.ZHEKOU)){
                    result = executorIndex.get(RuleFlag.MANJIAN_ZHEKOU).computeRule(settlementInfo);
                }else {
                    throw new CouponException("Not Support For Other Template Category");
                }
            }
        }
        return result;
    }

    /**
     * 在Bean初始化之前执行（before）
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 如果传入的bean不是优惠券规则执行器，则直接返回
        if (!(bean instanceof RuleExecutor)){
            return bean;
        }
        // 获取到对应的RuleExecutor和RuleFlag
        RuleExecutor executor = (RuleExecutor)bean;
        RuleFlag ruleFlag = executor.ruleConfig();
        if (executorIndex.containsKey(ruleFlag)){
            throw new IllegalStateException("Thre Is already an executor for rule flag:" + ruleFlag);
        }

        log.info("Load executor {} for rule flag {},",executor.getClass(),ruleFlag);
        executorIndex.put(ruleFlag,executor);
        return null;
    }

    /**
     * 在Bean初始化之后执行（after）
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
