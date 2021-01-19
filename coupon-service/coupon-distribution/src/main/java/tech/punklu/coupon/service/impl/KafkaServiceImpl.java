package tech.punklu.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tech.punklu.coupon.constant.Constant;
import tech.punklu.coupon.constant.CouponStatus;
import tech.punklu.coupon.dao.CouponDao;
import tech.punklu.coupon.entity.Coupon;
import tech.punklu.coupon.service.IKafkaService;
import tech.punklu.coupon.vo.CouponKafkaMessage;

import java.util.List;
import java.util.Optional;

/**
 * Kafka相关的服务接口实现
 * 核心思想：将Cache中的Coupon的状态变化同步到DB中
 */
@Slf4j
@Component
public class KafkaServiceImpl implements IKafkaService {

    /**
     * 优惠券DAO接口
     */
    @Autowired
    private CouponDao couponDao;

    /**
     * 消费优惠券Kafka消息
     * @param record 从kafka的topic 中读取的消息的对象的表示
     */
    @Override
    @KafkaListener(topics = {Constant.TOPIC},groupId = "coupon-1")  // kafka监听器，确保有新消息时把消息封装为ConsumerRecord传入此方法
    public void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record) {

        // 获取接收到的Kafka消息
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        // 如果存在消息
        if (kafkaMessage.isPresent()){
            Object message = kafkaMessage.get();
            // 序列化转换
            CouponKafkaMessage couponInfo = JSON.parseObject(message.toString(),CouponKafkaMessage.class);
            log.info("Receive CouponKafkaMessage:{}",message.toString());
            // 获取消息中的状态
            CouponStatus status = CouponStatus.of(couponInfo.getStatus());
            switch (status){
                case USABLE:
                    break;
                case USED:
                    processExpiredCoupons(couponInfo,status);
                    break;
                case EXPIRED:
                    processExpiredCoupons(couponInfo,status);
                    break;
            }
        }
    }

    /**
     * 处理已使用的用户优惠券
     * @param kafkaMessage
     * @param status
     */
    private void processUsedCoupons(CouponKafkaMessage kafkaMessage,CouponStatus status){
        processCouponsByStatus(kafkaMessage,status);
    }

    /**
     * 处理已过期的用户优惠券
     * @param kafkaMessage
     * @param status
     */
    private void processExpiredCoupons(CouponKafkaMessage kafkaMessage,CouponStatus status){
        processCouponsByStatus(kafkaMessage,status);
    }

    /**
     * 根据状态处理优惠券信息
     * @param kafkaMessage
     * @param status
     */
    private void processCouponsByStatus(CouponKafkaMessage kafkaMessage,CouponStatus status){
        List<Coupon> coupons = couponDao.findAllById(kafkaMessage.getIds());
        if (CollectionUtils.isEmpty(coupons) || coupons.size() != kafkaMessage.getIds().size()){
            log.error("Can not find Right Coupon Info : {}",JSON.toJSONString(kafkaMessage));
            return;
        }
        coupons.forEach(c -> c.setStatus(status));
        log.info("CouponKafkaMassage Op Coupon Count : {}",couponDao.saveAll(coupons).size());

    }
}
