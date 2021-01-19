package tech.punklu.coupon.service;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tech.punklu.coupon.constant.CouponStatus;
import tech.punklu.coupon.exception.CouponException;

/**
 * 用户服务功能测试用例
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    /**
     * fake一个userId
     */
    private Long fakeUserId = 20001L;

    @Autowired
    private IUserService userService;

    @Test
    public void testFindCouponByStatus() throws CouponException {
        System.out.println(JSON.toJSONString(userService.findCouponsByStatus(fakeUserId, CouponStatus.USABLE.getCode())));
    }

    /**
     * 因为没有模板微服务，会调用到hystrix的兜底方法
     * @throws CouponException
     */
    @Test
    public void testFindAvaiableTemplate() throws CouponException{
        System.out.println(JSON.toJSONString(userService.findAvailableTemplate(fakeUserId)));
    }
}
