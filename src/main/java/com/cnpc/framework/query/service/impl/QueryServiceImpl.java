package com.cnpc.framework.query.service.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.cnpc.framework.base.service.impl.BaseServiceImpl;
import com.cnpc.framework.query.entity.QueryConfig;
import com.cnpc.framework.query.service.QueryService;

@Service("queryService")
public class QueryServiceImpl extends BaseServiceImpl implements QueryService {

    public void deleteAndSave(QueryConfig queryConfig) {

        // 获取原有列表并删除
        delete(queryConfig);
        // 再保存最新的列表
        this.save(queryConfig);
    }

    public void delete(QueryConfig queryConfig) {

        // 获取原有列表并删除
        DetachedCriteria criteria = DetachedCriteria.forClass(QueryConfig.class);
        criteria.add(Restrictions.eq("userid", queryConfig.getUserid()));
        criteria.add(Restrictions.eq("pageName", queryConfig.getPageName()));
        criteria.add(Restrictions.eq("queryId", queryConfig.getQueryId()));
        List<QueryConfig> list = this.findByCriteria(criteria);
        this.batchDelete(list);
    }

}
