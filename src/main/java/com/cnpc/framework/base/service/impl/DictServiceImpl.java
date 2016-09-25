package com.cnpc.framework.base.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cnpc.framework.base.entity.Dict;
import com.cnpc.framework.base.pojo.TreeNode;
import com.cnpc.framework.base.service.DictService;
import com.cnpc.framework.utils.StrUtil;

@Service("dictService")
public class DictServiceImpl extends BaseServiceImpl implements DictService {

    @Override
    public List<TreeNode> getTreeData() {

        // 获取数据
        String hql = "from Dict order by levelCode asc";
        List<Dict> dicts = this.find(hql);
        Map<String, TreeNode> nodelist = new LinkedHashMap<String, TreeNode>();
        // root
        /*
         * TreeNode root = new TreeNode(); root.setText("字典管理");
         * root.setHref("0"); root.setParentId("-1");
         * nodelist.put(root.getHref(), root);
         */
        List<TreeNode> tnlist = new ArrayList<TreeNode>();
        for (Dict dict : dicts) {
            TreeNode node = new TreeNode();
            node.setText(dict.getName());
            node.setId(dict.getId());
            node.setParentId(dict.getParentId());
            node.setLevelCode(dict.getLevelCode());
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
}
