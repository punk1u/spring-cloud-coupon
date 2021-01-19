package tech.punklu.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tech.punklu.coupon.constant.Constant;
import tech.punklu.coupon.constant.CouponStatus;
import tech.punklu.coupon.dao.CouponDao;
import tech.punklu.coupon.entity.Coupon;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.feign.SettlementClient;
import tech.punklu.coupon.feign.TemplateClient;
import tech.punklu.coupon.service.IRedisService;
import tech.punklu.coupon.service.IUserService;
import tech.punklu.coupon.vo.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户服务相关的接口实现
 * 所有的操作过程，状态都保存在Redis中，并通过kafka把消息传递到mysql中
 * 使用kafka，而不是直接使用SpringBoot中的异步处理：kafka中的消息写入mysql失败，也可以重新尝试，可以保证消息一致性
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    /**
     * 优惠券DAO接口
     */
    @Autowired
    private CouponDao couponDao;

    /**
     * Redis接口
     */
    @Autowired
    private IRedisService redisService;

    /**
     * 模板微服务的自定义调用类
     */
    @Autowired
    private TemplateClient templateClient;

    /**
     * 结算微服务自定义调用类
     */
    @Autowired
    private SettlementClient settlementClient;

    /**
     *  kafka客户端
     */
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    /**
     * 根据用户id和状态查询优惠券记录
     * @param userId 用户id
     * @param status 优惠券状态
     * @return
     * @throws CouponException
     */
    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException {
        // 根据userId和status优惠券状态查询Redis中缓存的优惠券信息
        List<Coupon> curCached = redisService.getCachedCoupons(userId, status);
        List<Coupon> preTarget;

        if (CollectionUtils.isNotEmpty(curCached)){
            log.debug("Coupon Cache is not empty : {}，{}",userId,status);
            preTarget = curCached;
        }else {
            log.debug("coupon cache is empty , get coupon from db :{},{}",userId,status);
            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus(userId, CouponStatus.of(status));
            // 如果数据库中没有记录，直接返回就可以，在查询Cache的时候已经向Cache中加入了一张无效的优惠券
            if (CollectionUtils.isEmpty(dbCoupons)){
                log.debug("current user do not have coupon :{},{}",userId,status);
                return dbCoupons;
            }
            // 填充dbCoupons的templateSDK字段
            Map<Integer,CouponTemplateSDK> id2TemplateSDK = templateClient.findIds2TemplateSDK(
                    dbCoupons.stream().map(Coupon::getTemplateId).collect(Collectors.toList())).getData();
            dbCoupons.forEach(
                    dc -> dc.setTemplateSDK(id2TemplateSDK.get(dc.getTemplateId()))
            );
            // 数据库中存在记录
            preTarget = dbCoupons;
            // 将记录写入cache
            redisService.addCouponToCache(userId,preTarget,status);
        }
        // 将无效优惠券剔除
        preTarget = preTarget.stream().filter(c -> c.getId() != -1).collect(Collectors.toList());
        // 如果当前获取的是可用优惠券，还需要做对已过期优惠券的延迟处理
        if (CouponStatus.of(status) == CouponStatus.USABLE){
            CouponClassify classify = CouponClassify.classify(preTarget);
            // 如果已过期状态不为空，需要做延迟处理
            if (CollectionUtils.isNotEmpty(classify.getExpired())){
                log.info("Add Expired Coupons To Cache From FindCouponsStatus: {}, {}",userId,status);
                redisService.addCouponToCache(userId,classify.getExpired(),CouponStatus.EXPIRED.getCode());
                // 将过期优惠券信息发送到kafka中，做异步处理更新Mysql中的信息
                kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(new CouponKafkaMessage(
                        CouponStatus.EXPIRED.getCode(),
                        classify.getExpired().stream().map(Coupon::getId).collect(Collectors.toList()))));
            }
            return classify.getUsable();
        }
        return preTarget;
    }

    /**
     * 根据用户id查找当前可以领取的优惠券模板
     * @param userId 用户编号
     * @return
     * @throws CouponException
     */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {
        long curTime = new Date().getTime();
        // 查询所有的优惠券模板
        List<CouponTemplateSDK> templateSDKS =templateClient.findAllUsableTemplate().getData();
        log.debug("Find All Template(From TemplateClient) Count : {}",templateSDKS.size());
        // 过滤可能存在的过期的优惠券模板
        templateSDKS = templateSDKS.stream().filter(
            t -> t.getRule().getExpiration().getDeadline() > curTime
        ).collect(Collectors.toList());
        log.info("Find Usable Template Count:{}",templateSDKS.size());
        // key:templateId 模板编号
        // value中的key是Template limitation，意思为可以被单个用户领取的数量上限，value是优惠券模板
        Map<Integer, Pair<Integer,CouponTemplateSDK>> limit2Template = new HashMap<>(templateSDKS.size());
        templateSDKS.forEach(
                t -> limit2Template.put(t.getId(),Pair.of(t.getRule().getLimitation(),t))
        );

        // 最终结果
        List<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());
        List<Coupon> userUsableCoupons = findCouponsByStatus(userId,CouponStatus.USABLE.getCode());
        log.debug("Current User Has Usable Coupons: {}, {}",userId,userUsableCoupons.size());

        // key: templateId
        // value 用户已经领取的templateId对应的优惠券列表
        Map<Integer,List<Coupon>> templateId2Coupons = userUsableCoupons.stream().collect(Collectors.groupingBy(Coupon::getTemplateId));
        // 根据Template的Rule判断是否可以领取优惠券模板
        limit2Template.forEach((k,v) -> {
            // 优惠券模板被限制领取的次数
            int limitation = v.getLeft();
            CouponTemplateSDK templateSDK = v.getRight();
            // 如果用户之前已经领取了这个优惠券模板，并且用户根据这个优惠券模板领取的优惠券个数大于等于这个优惠券模板最多可领取的次数
            if (templateId2Coupons.containsKey(k) && templateId2Coupons.get(k).size() >= limitation){
                return;
            }
            result.add(templateSDK);
        });
        return result;
    }

    /**
     * 用户领取优惠券
     * 1、从TemplateClient 拿到对应的优惠券并检查是否过期
     * 2、根据limitation判断用户是否可以领取
     * 3、save to db
     * 4、填充CouponTemplateSDK
     * 5、save to cache
     * @param request
     * @return
     * @throws CouponException
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {
        // 获取用户可以领取的优惠券模板
        Map<Integer,CouponTemplateSDK> id2Template = templateClient.findIds2TemplateSDK(
                Collections.singletonList(
                        request.getTemplateSDK().getId()
                )
        ).getData();

        // 优惠券模板是需要存在的
        if (id2Template.size() <= 0){
            log.error("Can not acquire Template From TemplateClient:{}",request.getTemplateSDK().getId());
            throw new CouponException("Can not acquire Template From TemplateClient");
        }
        // 判断用户是否可以领取这张优惠券
        // 获取用户当前拥有的优惠券列表
        List<Coupon> userUsableCoupons = findCouponsByStatus(request.getUserId(),CouponStatus.USABLE.getCode());
        // 对用户当前已经领取的优惠券根据模板id分类
        // key: templateId
        // value: 用户已领取的1templateId对应的优惠券列表
        Map<Integer,List<Coupon>> templateId2Coupons = userUsableCoupons.stream().collect(Collectors.groupingBy(Coupon::getTemplateId));
        // 判断用户是否已领取这张优惠券，如果已领取，判断是否超过最大领取值
        if (templateId2Coupons.containsKey(request.getTemplateSDK().getId()) &&
                templateId2Coupons.get(request.getTemplateSDK().getId()).size() >=
                        request.getTemplateSDK().getRule().getLimitation()){
            log.error("Exceed Template Assign Limitation : {}",request.getTemplateSDK().getId());
            throw new CouponException("Exceed Template Assign Limitation");
        }
        // 尝试去获取优惠券码
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(
                request.getTemplateSDK().getId()
        );
        // 如果优惠券码为空，代表当前优惠券已经领取完了
        if (StringUtils.isEmpty(couponCode)){
            log.error("Can not acquire coupon code:{}",request.getTemplateSDK().getId());
            throw new CouponException("Can not acquire Coupon Code");
        }
        Coupon newCoupon = new Coupon(request.getTemplateSDK().getId(),request.getUserId(),couponCode,CouponStatus.USABLE);
        newCoupon = couponDao.save(newCoupon);
        // 填充Coupon对象的CouponTemplateSDK,一定要在放入缓存之前去填充
        newCoupon.setTemplateSDK(request.getTemplateSDK());
        // 放入缓存中
        redisService.addCouponToCache(request.getUserId(),Collections.singletonList(newCoupon),CouponStatus.USABLE.getCode());
        return newCoupon;
    }

    /**
     * 结算(核销)优惠券
     * 结算：根据优惠券和商品计算，并未真正使用
     * 核销：使用优惠券
     *
     * 规则相关处理需要由Settlement系统去做，当前系统仅仅做业务处理过程（校验过程）
     * @param info
     * @return
     * @throws CouponException
     */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {
        // 当没有传递优惠券时，直接返回商品总价
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = info.getCouponAndTemplateInfos();
        if (CollectionUtils.isEmpty(ctInfos)){
            log.info("Empty Coupons For Settle.");
            double goodsSum = 0.0;
            for (GoodsInfo goodsInfo : info.getGoodsInfos()) {
                goodsSum += goodsInfo.getPrice() * goodsInfo.getCount();
            }
            // 没有优惠券也就不存在优惠券的核销,SettlementInfo其他的字段不需要修改
            info.setCost(retain2Decimals(goodsSum));
        }
        // 校验传递的优惠券是否是用户自己的
        // 获取用户自己拥有的可用的优惠券
        List<Coupon> coupons = findCouponsByStatus(info.getUserId(),CouponStatus.USABLE.getCode());
        // key:优惠券id
        Map<Integer,Coupon> id2Coupon = coupons.stream().collect(Collectors.toMap(Coupon::getId, Function.identity()));
        // 如果查询到的用户可用优惠券为空或传进来的优惠券不是可用优惠券的子集，说明不可用，抛出异常
        if (MapUtils.isEmpty(id2Coupon) || !CollectionUtils.isSubCollection(
                ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId).
                        collect(Collectors.toList()), id2Coupon.keySet())){
            log.info("{}",id2Coupon.keySet());
            log.info("{}",ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId).collect(Collectors.toList()));
            log.error("User Coupon Has Some Problem,It is Not SubCollection of Coupons");
            throw new CouponException("User Coupon Has Some Problem,It is Not SubCollection of Coupons");
        }
        log.debug("Current Settlement Coupons Is User's : {}",ctInfos.size());

        // settleCoupons : 参与结算的优惠券列表
        List<Coupon> settleCoupons = new ArrayList<>(ctInfos.size());
        ctInfos.forEach(ci -> settleCoupons.add(id2Coupon.get(ci.getId())));

        // 通过结算微服务获取结算信息
        SettlementInfo processedInfo = settlementClient.computeRule(info).getData();
        // 如果是要核销，且核算正确的情况
        if (processedInfo.getEmploy() && CollectionUtils.isNotEmpty(processedInfo.getCouponAndTemplateInfos())){
            // 打印参与核销的优惠券
            log.info("Settle User Coupon: {},{}",info.getUserId(),JSON.toJSONString(settleCoupons));
            // 将此用户使用过的优惠券放入缓存Redis中
            redisService.addCouponToCache(info.getUserId(),settleCoupons,CouponStatus.USED.getCode());
            // 更新db
            kafkaTemplate.send(Constant.TOPIC,
                    JSON.toJSONString(new CouponKafkaMessage(CouponStatus.USED.getCode(),
                                                            settleCoupons.stream().map(Coupon::getId).collect(Collectors.toList()))));

        }
        return null;
    }

    /**
     * 保留两位小数
     * @param value
     * @return
     */
    private double retain2Decimals(double value){
        // 保留两位小数，ROUND_HALF_UP代表四舍五入
        return new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
