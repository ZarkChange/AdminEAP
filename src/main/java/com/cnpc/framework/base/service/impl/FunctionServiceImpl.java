package com.cnpc.framework.base.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cnpc.framework.base.entity.Function;
import com.cnpc.framework.base.pojo.TreeNode;
import com.cnpc.framework.base.service.FunctionService;
import com.cnpc.framework.utils.StrUtil;

@Service("functionService")
public class FunctionServiceImpl extends BaseServiceImpl implements FunctionService {

    @Override
    public List<TreeNode> getTreeData() {

        // 获取数据
        String hql = "from Function order by levelCode asc";
        List<Function> funcs = this.find(hql);
        Map<String, TreeNode> nodelist = new LinkedHashMap<String, TreeNode>();
        // root
        /*
         * TreeNode root = new TreeNode(); root.setText("字典管理");
         * root.setHref("0"); root.setParentId("-1");
         * nodelist.put(root.getHref(), root);
         */
        List<TreeNode> tnlist = new ArrayList<TreeNode>();
        for (Function func : funcs) {
            TreeNode node = new TreeNode();
            node.setText(func.getName());
            node.setId(func.getId());
            node.setParentId(func.getParentId());
            node.setLevelCode(func.getLevelCode());
            node.setIcon(func.getIcon());
            nodelist.put(node.getId(), node);
        }
        // 构造树形结构
        for (String id : nodelist.keySet()) {
            TreeNode node = nodelist.get(id);
            if (StrUtil.isEmpty(node.getParentId())) {
                tnlist.add(node);
            } else {
                if (nodelist.get(node.getParentId()).getNodes() == null)
                    nodelist.get(node.getParentId()).setNodes(new ArrayList<TreeNode>());
                nodelist.get(node.getParentId()).getNodes().add(node);
            }
        }
        return tnlist;
    }

    @Override
    public List<Function> getAll() {

        String hql = "from Function where (deleted=0 or deleted is null) order by levelCode";
        return this.find(hql);
    }
}
