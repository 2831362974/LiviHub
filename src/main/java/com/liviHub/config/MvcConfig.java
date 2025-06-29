package com.liviHub.config;

import com.liviHub.utils.LoginInterceptor;
import com.liviHub.utils.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).order(0);//刷新

        registry.addInterceptor(new LoginInterceptor()).
                excludePathPatterns("/user/login",
                                    "/user/code",
                                    "/blog/hot",
                                    "/shop/**",
                                    "/shop-type/**",
                                    "/upload/**",
                                    "/ai/**",
                                    "/voucher/**").order(1);
//         excludePathPatterns("/**")  // 排除所有路径
//                .order(1);
    }
}
