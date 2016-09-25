package com.cnpc.framework.base.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tbl_role_function")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler", "fieldHandler" })
public class RoleFunction extends BaseEntity {

    /**
     *
     */
    private static final long serialVersionUID = -1340123834197348115L;

    @Column(name = "roleId", length = 36)
    private String roleId;

    @Column(name = "functionId", length = 36)
    private String functionId;

    public String getFunctionId() {

        return functionId;
    }

    public void setFunctionId(String functionId) {

        this.functionId = functionId;
    }

    public String getRoleId() {

        return roleId;
    }

    public void setRoleId(String roleId) {

        this.roleId = roleId;
    }

}
