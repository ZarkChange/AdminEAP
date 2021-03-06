package com.cnpc.framework.base.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.criterion.DetachedCriteria;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.cnpc.framework.base.entity.User;
import com.cnpc.framework.base.pojo.PageInfo;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.UserService;

@Controller
@RequestMapping(value = "/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户列表
     */
    @RequestMapping(method = RequestMethod.GET, value = "/list")
    private String list() {

        return "base/user/user_list";
    }

    /**
     * 用户编辑
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/edit")
    private String eidt(String id, HttpServletRequest request) {

        request.setAttribute("id", id);
        return "base/user/user_edit";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/get")
    @ResponseBody
    private User getUser(String id) {

        return userService.get(User.class, id);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/save")
    @ResponseBody
    private Result saveUser(User user) {

        user.setUpdateDateTime(new Date());
        userService.saveOrUpdate(user);
        return new Result(true);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/delete/{id}")
    @ResponseBody
    private Result deleteUser(@PathVariable("id") String id) {

        User user = userService.get(User.class, id);
        userService.delete(user);
        return new Result(true);
    }

    /**
     * loadData
     *
     * @param pInfo
     * @param conditions
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/loadData")
    @ResponseBody
    public Map<String, Object> loadData(String pInfo, String conditions) {

        Map<String, Object> map = new HashMap<String, Object>();
        PageInfo pageInfo = JSON.parseObject(pInfo, PageInfo.class);
        DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
        pageInfo.setCount(userService.getCountByCriteria(criteria));
        map.put("pageInfo", pageInfo);
        map.put("data", userService.getListByCriteria(criteria, pageInfo));
        return map;
    }

}
