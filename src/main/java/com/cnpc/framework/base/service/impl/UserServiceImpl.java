package com.cnpc.framework.base.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.cnpc.framework.base.dao.BaseDao;
import com.cnpc.framework.base.entity.User;
import com.cnpc.framework.base.service.UserService;

@Service("userService")
public class UserServiceImpl extends BaseServiceImpl implements UserService {

    @Resource
    private BaseDao baseDao;

    private List<User> getUsers() {

        return baseDao.find("from User");
    }

}
