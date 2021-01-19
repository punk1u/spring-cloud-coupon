package tech.punklu.coupon.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Kafka相关的服务接口定义
 */
public interface IKafkaService {

    /**
     * 消费优惠券Kafka消息
     * @param record 从kafka的topic 中读取的消息的对象的表示
     */
    void consumeCouponKafkaMessage(ConsumerRecord<?,?> record);

}
