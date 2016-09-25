package com.cnpc.framework.utils;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import com.cnpc.framework.base.entity.User;

/**
 * SessionUtil session 工具类
 * 
 * @author bin 2016年3月26日 下午10:12:16
 */
public class SessionUtil {

    /**
     * getCurrentUser 获取当前用户
     * 
     * @return
     */
    public static User getCurrentUser() {

        User user = (User) getSession().getAttribute("user");
        return user;
    }

    public static Session getSession() {

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        return session;
    }

}
