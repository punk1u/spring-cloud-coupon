package tech.punklu.coupon.constant;

/**
 *
 * 通用常量定义
 */
public class Constant {

    // kafka topic(用户对优惠券的操作)
    public static final String TOPIC = "user_coupon_op";

    /**
     * Redis key 前缀
     */
    public static class RedisPrefix{

        // 优惠券模板码前缀
        public static final String COUPON_TEMPLATE = "coupon_template_code_";

        // 用户当前所有可用的优惠券 key 前缀
        public static final String USER_COUPON_USABLE = "user_coupon_usable_";

        // 用户当前所有已使用的优惠券 key 前缀
        public static final String USER_COUPON_USED = "user_coupon_used_";

        // 用户当前所有已过期的优惠券 key 前缀
        public static final String USER_COUPON_EXPIRED = "user_coupon_expired_";
     }
}
