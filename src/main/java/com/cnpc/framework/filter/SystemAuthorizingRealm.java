package com.cnpc.framework.filter;

import java.util.List;

import javax.annotation.Resource;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Service;

import com.cnpc.framework.base.entity.User;
import com.cnpc.framework.base.service.UserService;

/**
 * @author bin 系统安全认证实现类
 * 
 */
@Service
public class SystemAuthorizingRealm extends AuthorizingRealm {

    /**
     * 认证回调函数, 登录时调用
     */
    @Resource
    private UserService userService;

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) {

        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
        // 校验用户名密码
        User user = new User();
        user.setLoginName(token.getUsername());
        user.setPassword(String.copyValueOf(token.getPassword()));
        List<User> userList = userService.findByExample(user);
        if (userList.size() > 0) {
            // 注意此处的返回值没有使用加盐方式,如需要加盐，可以在密码参数上加
            return new SimpleAuthenticationInfo(userList.get(0), token.getPassword(), token.getUsername());
        }
        return null;
    }

    /**
     * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用 shiro 权限控制有三种 1、通过xml配置资源的权限
     * 2、通过shiro标签控制权限 3、通过shiro注解控制权限
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        return null;
    }

}
