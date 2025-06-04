package com.liviHub.rebbitmq;

import com.alibaba.fastjson.JSON;
import com.liviHub.config.RabbitMQTopicConfig;
import com.liviHub.model.entity.VoucherOrder;
import com.liviHub.service.ISeckillVoucherService;
import com.liviHub.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.util.concurrent.TimeUnit;

/**
 * 消息消费者
 */
@Slf4j
@Service
public class MQReceiver {

    @Resource
    IVoucherOrderService voucherOrderService;

    @Resource
    ISeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 接收秒杀信息并下单
     * @param msg
     */
    @Transactional
    @RabbitListener(queues = RabbitMQTopicConfig.QUEUE)
    public void receiveSeckillMessage(String msg){
        log.info("接收到消息: "+msg);

        VoucherOrder voucherOrder = JSON.parseObject(msg, VoucherOrder.class);
        Long voucherId = voucherOrder.getVoucherId();
        Long userId = voucherOrder.getUserId();
        Long orderId = voucherOrder.getId();
        //5.一人一单逻辑
        //5.1 幂等性检查：orderID作为唯一id，借助redis判断订单是否已经处理
        String orderKey = "order:processed:" + orderId;
        Boolean processed = stringRedisTemplate.opsForValue().setIfAbsent(orderKey, "true", 1, TimeUnit.DAYS);
        if (processed == null || !processed) {
            log.warn("重复处理的订单: {}", orderId);
            return;
        }
//        long count = voucherOrderService.query().eq("user_id",userId).eq("voucher_id", voucherId).count();
//        //5.2 判断是否已经有订单存在
//        if(count>0){
//            //用户已经购买过了
//            log.error("该用户已购买过");
//            return ;
//        }
        log.info("扣减库存");
        //6.扣减库存
        boolean success = seckillVoucherService
                .update()
                .setSql("stock = stock-1")
                .eq("voucher_id", voucherId)
                .gt("stock",0)//cas乐观锁
                .update();
        if(!success){
            log.error("库存不足");
            return;
        }
        //直接保存订单
        voucherOrderService.save(voucherOrder);
    }

}
