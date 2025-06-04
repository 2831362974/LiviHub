package com.liviHub.rebbitmq;

import com.alibaba.fastjson.JSON;
import com.liviHub.config.RabbitMQTopicConfig;
import com.liviHub.model.entity.VoucherOrder;
import com.liviHub.service.ISeckillVoucherService;
import com.liviHub.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀消息消费者 获取到秒杀订单消息后，再次通过数据库审核秒杀资格和库存
 * （验证库存时可能出现并发的情况）
 */
@Slf4j
@Service
public class MQReceiver {
    @Autowired
    private MQReceiver selfProxy; // 注入自身代理对象

    @Resource
    IVoucherOrderService voucherOrderService;

    @Resource
    ISeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = RabbitMQTopicConfig.QUEUE)
    public void receiveSeckillMessage(String msg) {
        log.info("接收到消息: " + msg);
        VoucherOrder voucherOrder = JSON.parseObject(msg, VoucherOrder.class);
        Long orderId = voucherOrder.getId();

        // 1. 幂等性检查（移至事务外部）
        String orderKey = "order:processed:" + orderId;
        Boolean processed = stringRedisTemplate.opsForValue().setIfAbsent(orderKey, "true", 1, TimeUnit.DAYS);
        if (Boolean.FALSE.equals(processed)) {
            log.warn("重复订单处理: {}", orderId);
            return;
        }

        try {
            selfProxy.processOrderInTransaction(voucherOrder);
        } catch (Exception e) {
            // 出错则删除标记，以便后续重试可生效
            stringRedisTemplate.delete(orderKey);
            throw e;
        }
    }

    //添加事务，要求扣减库存后就必须创建订单，保证数据库操作的原子性（redis操作的原子性已经通过lua脚本保证）  防止并发下的竞态条件
    @Transactional
    public void processOrderInTransaction(VoucherOrder voucherOrder) {
        Long voucherId = voucherOrder.getVoucherId();
        Long userId = voucherOrder.getUserId();

        // 3. 从数据库检查用户是否已经购买过
        long count = voucherOrderService.query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            log.error("该用户已购买过");
            throw new RuntimeException("用户已购买过"); // 回滚事务
        }

        // 4. 扣减库存
        boolean success = seckillVoucherService
                .update()
                .setSql("stock = stock-1")
                .eq("voucher_id", voucherId)
                .ge("stock", 1)
                .update();
        if (!success) {
            log.error("库存不足或已被其他线程扣减");
            throw new RuntimeException("库存不足"); // 回滚事务
        }

        // 5. 保存订单
        voucherOrderService.save(voucherOrder);
    }
}