package tech.punklu.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tech.punklu.coupon.constant.Constant;
import tech.punklu.coupon.constant.CouponStatus;
import tech.punklu.coupon.entity.Coupon;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.service.IRedisService;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis相关的操作服务接口实现
 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {

    /**
     * Redis客户端
     */
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 根据userId和状态获取缓存的优惠券信息
     * @param userId 用户编号
     * @param status 优惠券状态
     * @return
     */
    @Override
    public List<Coupon> getCachedCoupons(Long userId, Integer status) {
        log.info("Get Coupons From Cache: {},{}",userId,status);
        String redisKey = status2RedisKey(status,userId);
        List<String> couponStrs = redisTemplate.opsForHash().values(redisKey).stream().map(o -> Objects.toString(o,null)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(couponStrs)){
            // 如果为空，向缓存中塞入一个状态为status，用户id为userId的值，避免后续可能出现的缓存穿透
            saveEmptyCouponListToCache(userId, Collections.singletonList(status));
            return Collections.emptyList();
        }
        return couponStrs.stream().map(cs ->JSON.parseObject(cs,Coupon.class)).collect(Collectors.toList());
    }


    /**
     * 保存空的优惠券列表到缓存中，防止缓存穿透
     * @param userId 用户id
     * @param status 优惠券状态列表
     */
    @Override
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {
        log.info("Save Empty List To Cache For User:{},Status:{}",userId, JSON.toJSONString(status));
        // key 是couponId，value是序列化的coupon
        Map<String,String> invalidCouponMap = new HashMap<>();
        invalidCouponMap.put("-1",JSON.toJSONString(Coupon.invalidCoupon()));

        // 用户优惠券缓存信息
        // kv
        // k: status ->redisKey
        // v: {coupon_id: 序列化的Coupon}

        // 使用SessionCallback把数据命令放入到Redis的pipeline（pipeline可实现一次性执行多条命令，不需要顺序逐条执行）
        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>(){
            @Override
            public  Objects execute(RedisOperations redisOperations) throws DataAccessException {
                status.forEach(s -> {
                    String redisKey = status2RedisKey(s,userId);
                    redisOperations.opsForHash().putAll(redisKey,invalidCouponMap);
                });
                return null;
            }
        };
        log.info("Pipeline Exe Result :{}",JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
    }

    /**
     * 从Cache中尝试获取一个优惠券码
     * @param templateId 优惠券模板主键
     * @return 优惠券码
     */
    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        String redisKey = String.format("%s%s",Constant.RedisPrefix.COUPON_TEMPLATE,templateId);
        // 因为优惠券码不存在顺序关系，leftPop和rightPop没影响
        String couponCode = redisTemplate.opsForList().leftPop(redisKey);
        log.info("Acquire Coupon Code ： {},{},{}",templateId,redisKey,couponCode);
        return couponCode;
    }

    /**
     * 把优惠券信息放入缓存
     * @param userId 用户id
     * @param coupons 优惠券
     * @param status 优惠券状态
     * @return 保存成功的个数
     * @throws CouponException
     */
    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException {
        log.info("Add Coupon To Cache：{},{},{}",userId,JSON.toJSONString(coupons),status);
        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus){
            case USABLE:
                result = addCouponToCacheForUsable(userId,coupons);
                break;
            case USED:
                addCouponToCacheForUsed(userId,coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheForExpired(userId,coupons);
                break;
        }
        return result;
    }

    /**
     * 把新增加的优惠券放入到Cache中
     * @param userId
     * @param coupons
     * @return
     */
    private Integer addCouponToCacheForUsable(Long userId,List<Coupon> coupons){
        // 如果status是usable，代表是新增加的优惠券，只会影响一个Cache：USER_COUPON_USABLE
        log.debug("Add Coupon To Cache For Usable");
        Map<String,String> needCachedObject = new HashMap<>();
        coupons.forEach(c -> needCachedObject.put(c.getId().toString(),JSON.toJSONString(c)));
        String redisKey = status2RedisKey(CouponStatus.USABLE.getCode(),userId);
        redisTemplate.opsForHash().putAll(redisKey,needCachedObject);
        log.info("Add {} Coupons To Cache:{},{}",needCachedObject.size(),userId,redisKey);
        redisTemplate.expire(redisKey,getRandomExpireTime(1,2), TimeUnit.SECONDS);
        return needCachedObject.size();
    }

    /**
     * 将已使用的优惠券加入到Cache中
     * @param userId
     * @param coupons
     * @return
     * @throws CouponException
     */
    private Integer addCouponToCacheForUsed(Long userId,List<Coupon> coupons) throws CouponException{
        // 如果status是USED，代表用户操作是使用当前的优惠券,将会影响到两个Cache USABLE、USED
        log.debug("Add Coupon To Cache For Used");
        Map<String,String> needCachedForUsed = new HashMap<>(coupons.size());
        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(),userId);
        String redisKeyForUsed = status2RedisKey(CouponStatus.USED.getCode(),userId);
        // 获取当前用户可用的优惠券
        List<Coupon> currentUsableCoupons = getCachedCoupons(userId,CouponStatus.USABLE.getCode());
        // 当前可用的优惠券个数一定是大于1的
        assert currentUsableCoupons.size() > coupons.size();
        coupons.forEach(c -> needCachedForUsed.put(c.getId().toString(),JSON.toJSONString(c)));

        // 校验当前的优惠券参数是否与cache中的匹配
        List<Integer> curUsableIds = currentUsableCoupons.stream().map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream().map(Coupon::getId).collect(Collectors.toList());
        // 如果paramIds 是curUsableIds的一个子集才为true
        if (!CollectionUtils.isSubCollection(paramIds,curUsableIds)){
            log.error("CurCoupons Is Not Equals ToCache:{},{},{}",userId,JSON.toJSONString(curUsableIds),JSON.toJSONString(paramIds));
            throw new CouponException("CurCoupons Is Not Equals ToCache!");
        }
        List<String> needCleanKey = paramIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public  Objects execute(RedisOperations redisOperations) throws DataAccessException {
                // 1、已使用的优惠券Cache缓存
                redisOperations.opsForHash().putAll(redisKeyForUsed,needCachedForUsed);
                // 2、可用的优惠券Cache 需要清理
                redisOperations.opsForHash().delete(redisKeyForUsable,needCleanKey.toArray());
                // 3、重置过期时间
                redisOperations.expire(redisKeyForUsable,getRandomExpireTime(1,2),TimeUnit.SECONDS);
                redisOperations.expire(redisKeyForUsed,getRandomExpireTime(1,2),TimeUnit.SECONDS);
                return null;
            }
        };
        log.info("Pipeline Exe Result :{}",JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    /**
     * 将已过期的优惠券加入到Cache中
     * @return
     * @throws CouponException
     */
    private Integer addCouponToCacheForExpired(Long userId,List<Coupon> coupons) throws CouponException{
        // status 是EXPIRED，代表是已有的没被使用的优惠券过期了,也会影响到两个Cache USABLE和EXPIRED
        log.debug("Add Coupon To Cache For Expired");
        // 最终需要保存的Cache
        Map<String,String> needCachedForExpired = new HashMap<>(coupons.size());
        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(),userId);
        String redisKeyForExpired = status2RedisKey(CouponStatus.EXPIRED.getCode(),userId);
        // 获取此用户所有的可用优惠券
        List<Coupon> curUsableCoupons = getCachedCoupons(userId,CouponStatus.USABLE.getCode());
        // 获取当前用户所有的已过期优惠券
        List<Coupon> curExpiredCoupons = getCachedCoupons(userId,CouponStatus.EXPIRED.getCode());

        // 当前可用的优惠券个数一定是大于1的
        assert curUsableCoupons.size() > coupons.size();
        coupons.forEach(c -> needCachedForExpired.put(c.getId().toString(),JSON.toJSONString(c)));

        // 校验当前的优惠券参数是否与Cache中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream().map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream().map(Coupon::getId).collect(Collectors.toList());

        if (CollectionUtils.isSubCollection(paramIds,curUsableCoupons)){
            log.error("CurCoupons Is Not Equal To Cache:{},{},{}",userId,JSON.toJSONString(curUsableIds),JSON.toJSONString(paramIds));
            throw new CouponException("CurCoupons Is Not Equal To Cache");
        }
        List<String> needCleanKey = paramIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public  Objects execute(RedisOperations redisOperations) throws DataAccessException {
                // 1、已过期的优惠券Cache缓存
                redisOperations.opsForHash().putAll(redisKeyForExpired,needCachedForExpired);
                // 2、可用的优惠券 Cache需要清理
                redisOperations.opsForHash().delete(redisKeyForUsable,needCleanKey.toArray());
                // 重置过期时间
                redisOperations.expire(redisKeyForUsable,getRandomExpireTime(1,2),TimeUnit.SECONDS);
                redisOperations.expire(redisKeyForExpired,getRandomExpireTime(1,2),TimeUnit.SECONDS);
                return null;
            }
        };
        log.info("Pipeline Exe Result: {}" + JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    /**
     * 根据status获取到对应的Redis key
     * @param status
     * @param userId
     * @return
     */
    private String status2RedisKey(Integer status,Long userId){
        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus){
            case USABLE:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USABLE,userId);
                break;
            case USED:
                redisKey = String.format("%s%s",Constant.RedisPrefix.USER_COUPON_USED,userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s",Constant.RedisPrefix.USER_COUPON_EXPIRED,userId);
                break;
        }
        return redisKey;
    }

    /**
     * 获取一个随机的过期时间
     * 缓存雪崩：key 在同一时间失效,为了避免缓存雪崩，让key拥有不同的过期时间
     * @param min 最小的小时数
     * @param max 最大的小时数
     * @return min和max之间的随机秒数
     */
    private Long getRandomExpireTime(Integer min,Integer max){
        return RandomUtils.nextLong(min*60*60,max*60*60);
    }
}
