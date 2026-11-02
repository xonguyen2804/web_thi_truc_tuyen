package com.onlineexam.model;

import java.io.Serializable;
/**
 * Model for Role.
 * TODO: Add fields, constructors, getters, and setters based on the agreed
 * database schema.
 */
public class Role implements Serializable{
    private int roleId;
    private String roleName;

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;

    }

    public int getRoleId() {
        return roleId;

    }

    public String getRoleName() {
        return roleName;

    }

}
