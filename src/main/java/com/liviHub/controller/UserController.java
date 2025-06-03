package com.liviHub.controller;


import cn.hutool.core.bean.BeanUtil;
import com.liviHub.model.dto.LoginFormDTO;
import com.liviHub.model.dto.Result;
import com.liviHub.model.dto.UserDTO;
import com.liviHub.model.entity.User;
import com.liviHub.model.entity.UserInfo;
import com.liviHub.service.IUserInfoService;
import com.liviHub.service.IUserService;
import com.liviHub.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 发送手机验证码 ！！在逻辑中已经修改为邮箱验证码便于实现
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) throws MessagingException, jakarta.mail.MessagingException {
        return userService.sendCode(phone, session);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码 【手机号应当为邮箱】
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        // 实现登录功能
        return userService.login(loginForm,session);
    }

    /**
     * 登出功能
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(){
        // TODO 实现登出功能
        return Result.fail("功能未完成");
    }

    @GetMapping("/me")
    public Result me(){
        //  获取当前登录的用户并返回
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    @GetMapping("/{id}")
    public Result queryById(@PathVariable("id") Long userId) {
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }

    //签到
    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    //统计每月签到
    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }
}
