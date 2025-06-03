package com.liviHub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liviHub.model.dto.LoginFormDTO;
import com.liviHub.model.dto.Result;
import com.liviHub.model.entity.User;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session) throws MessagingException, jakarta.mail.MessagingException;

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();
}
