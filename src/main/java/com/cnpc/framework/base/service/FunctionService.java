package com.cnpc.framework.base.service;

import java.util.List;

import com.cnpc.framework.base.entity.Function;
import com.cnpc.framework.base.pojo.TreeNode;

public interface FunctionService extends BaseService {

    List<TreeNode> getTreeData();

    List<Function> getAll();

}
