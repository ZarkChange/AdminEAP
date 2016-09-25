package com.cnpc.framework.base.service.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.cnpc.framework.base.entity.UserRole;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.UserRoleService;

@Service("userRoleService")
public class UserRoleServiceImpl extends BaseServiceImpl implements UserRoleService {

    public Result delete(String ids) {

        DetachedCriteria criteria = DetachedCriteria.forClass(UserRole.class);
        criteria.add(Restrictions.in("id", ids.split(",")));
        List<UserRole> urlist = this.findByCriteria(criteria);
        this.batchDelete(urlist);
        return new Result();
    }
}
