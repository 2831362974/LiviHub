package com.liviHub.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.liviHub.model.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//第一个拦截器，拦截所有请求
//检查是否携带token进来并根据token检查redis中有没有这个token对应的用户信息
//没token或者没有信息（token过期）就交给登录拦截器，判断这个请求需不需要登录，需要的话登录拦截器就会要求用户登录
//如果没有过期，就把用户信息存到这个请求对应的这个线程的threadlocal上，最后进行token的刷新，放行
//线程结束的时候通过aftercomptetion释放掉threadloacl避免内存泄漏
public class RefreshTokenInterceptor implements HandlerInterceptor {
    //这里并不是自动装配，因为RefreshTokenInterceptor是我们手动在WebConfig里new出来的
    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1. 获取请求头中的token
        String token = request.getHeader("authorization");
        //2. 如果token是空，直接放行，交给LoginInterceptor处理
        if (StrUtil.isBlank(token)) {
            return true;
        }
        String key = RedisConstants.LOGIN_USER_KEY + token;
        //3. 基于token获取Redis中的用户数据
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        //4. 判断用户是否存在，不存在，也放行，交给LoginInterceptor
        if (userMap.isEmpty()) {
            return true;
        }
        //5. 将查询到的Hash数据转化为UserDto对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //6. 将用户信息保存到ThreadLocal
        UserHolder.saveUser(userDTO);
        //7. 刷新tokenTTL，这里的存活时间根据需要自己设置，这里的常量值我改为了30分钟
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除threadlocal用户，避免内存泄漏
        UserHolder.removeUser();
    }
}
