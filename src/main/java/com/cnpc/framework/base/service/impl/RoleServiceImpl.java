package com.cnpc.framework.base.service.impl;

import org.springframework.stereotype.Service;

import com.cnpc.framework.base.entity.Role;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.RoleService;

@Service("roleService")
public class RoleServiceImpl extends BaseServiceImpl implements RoleService {

    @Override
    public Result delete(String id) {

        String hql = "from UserRole where roleId='" + id + "'";
        if (this.find(hql).isEmpty()) {
            Role role = this.get(Role.class, id);
            this.delete(role);
            return new Result(true);
        }
        return new Result(false, "该角色已经绑定用户，请先解绑用户");
    }
}
