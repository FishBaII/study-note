package com.ljm.swagger.entity;

import io.swagger.annotations.ApiModelProperty;

public class SystemUser {

    @ApiModelProperty(example = "1")
    private Long id;

    @ApiModelProperty(example = "admin")
    private String userName;

    @ApiModelProperty(example = "12345")
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "SystemUser{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                '}';
    }
}
