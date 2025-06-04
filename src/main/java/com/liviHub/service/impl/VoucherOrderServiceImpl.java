package com.liviHub.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.RateLimiter;
import com.liviHub.model.dto.Result;
import com.liviHub.model.entity.VoucherOrder;
import com.liviHub.mapper.VoucherOrderMapper;
import com.liviHub.rebbitmq.MQSender;
import com.liviHub.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liviHub.utils.RedisIdWorker;
import com.liviHub.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private MQSender mqSender;

    private RateLimiter rateLimiter = RateLimiter.create(10);

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //lua脚本
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static { //静态代码块，在类加载时执行一次，初始化lua脚本配置
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));//设置lua脚本位置
        SECKILL_SCRIPT.setResultType(Long.class);//设置返回值类型
    }

    /**
     * 优惠券秒杀 要求一人一单 使用rabbitmq异步解耦
     * 秒杀消息生产者 中只通过预热的redis数据进行库存/资格验证，通过发送消息给消费者
     * @param voucherId
     * @return
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        //令牌桶算法 限流
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            return Result.fail("目前网络正忙，请重试");
        }
        //1.1 获取用户ID，执行lua脚本判断秒杀资格
        Long userId = UserHolder.getUser().getId();
        Long r = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),//KEYS
                voucherId.toString(),//ARGV[1]
                userId.toString()//ARGV[2]
        );
        //2.1 判断结果是否为0
        int result = r.intValue();
        if (result != 0) {
            //不为0代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "该用户重复下单");
        }
        //2.2 为0代表有购买资格,将下单信息保存到阻塞队列

        //2.3 创建订单对象
        VoucherOrder voucherOrder = new VoucherOrder();
        // 订单id 使用封装好的生成器生成
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);

        //2.4 将订单信息放入MQ中异步处理秒杀订单
        mqSender.sendSeckillMessage(JSON.toJSONString(voucherOrder));

        //2.5 返回订单id
        return Result.ok(orderId);
    }
}

//    @Transactional
//    public Result createVoucherOrder(Long voucherId) {
//        // 一人一单逻辑
//        Long userId = UserHolder.getUser().getId();
//
//
//        int count = query().eq("voucher_id", voucherId).eq("user_id", userId).count();
//        if (count > 0){
//            return Result.fail("你已经抢过优惠券了哦");
//        }
//
//        //5. 扣减库存
//        boolean success = seckillVoucherService.update()
//                .setSql("stock = stock - 1")
//                .eq("voucher_id", voucherId)
//                .gt("stock",0)   //加了CAS 乐观锁，Compare and swap
//                .update();
//
//        if (!success) {
//            return Result.fail("库存不足");
//        }
//
////        库存足且在时间范围内的，则创建新的订单
//        //6. 创建订单
//        VoucherOrder voucherOrder = new VoucherOrder();
//        //6.1 设置订单id，生成订单的全局id
//        long orderId = redisIdWorker.nextId("order");
//        //6.2 设置用户id
//        Long id = UserHolder.getUser().getId();
//        //6.3 设置代金券id
//        voucherOrder.setVoucherId(voucherId);
//        voucherOrder.setId(orderId);
//        voucherOrder.setUserId(id);
//        //7. 将订单数据保存到表中
//        save(voucherOrder);
//        //8. 返回订单id
//        return Result.ok(orderId);
//    }

