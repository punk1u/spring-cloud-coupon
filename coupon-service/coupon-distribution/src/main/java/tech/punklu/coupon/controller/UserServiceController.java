package tech.punklu.coupon.controller;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.punklu.coupon.entity.Coupon;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.service.IUserService;
import tech.punklu.coupon.vo.AcquireTemplateRequest;
import tech.punklu.coupon.vo.CouponTemplateSDK;
import tech.punklu.coupon.vo.SettlementInfo;

import java.util.List;

/**
 * 用户服务Controller
 */
@Slf4j
@RestController
public class UserServiceController {

    /**
     * 用户服务接口
     */
    @Autowired
    private IUserService userService;


    /**
     * 根据用户id和优惠券状态查找用户优惠券
     * @param userId
     * @param status
     * @return
     * @throws CouponException
     */
    @GetMapping("/coupons")
    public List<Coupon> findCouponsByStatus(@RequestParam("userId") Long userId, @RequestParam("status") Integer status) throws CouponException{
        log.info("Find Coupons By Status:{}",userId,status);
        return userService.findCouponsByStatus(userId,status);
    }

    /**
     * 根据用户id查找当前可以领取的优惠券模板
     * @param userId
     * @return
     */
    @GetMapping("/template")
    public List<CouponTemplateSDK> findAvailableTemplate(@RequestParam("userId") Long userId) throws CouponException{
        log.info("Find Available Template:{}",userId);
        return userService.findAvailableTemplate(userId);
    }

    /**
     *  用户领取优惠券
     * @param request
     * @return
     * @throws CouponException
     */
    @PostMapping("/acquire/template")
    public Coupon acquireTemplate(@RequestBody  AcquireTemplateRequest request) throws CouponException{
        log.info("Acquire Template:{}", JSON.toJSONString(request));
        return userService.acquireTemplate(request);
    }

    /**
     * 结算/核销 优惠券
     * @param settlementInfo
     * @return
     * @throws CouponException
     */
    @PostMapping("/settlement")
    public SettlementInfo settlement(@RequestBody SettlementInfo settlementInfo) throws CouponException{
        log.info("Settlement:{}",JSON.toJSONString(settlementInfo));
        return userService.settlement(settlementInfo);
    }
}
