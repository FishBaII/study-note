package com.ljm.swaggerdoc.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public class Account {

    @Schema(name = "id",description = "Account Id",title = "id")
    private Long id;

    @Schema(name = "accountNumber",description = "Account Number",title = "accountNumber", example = "G12345")
    private String accountNumber;

    @Schema(name = "type",description = "Type",title = "type", example = "weixin")
    private String type;

    @Schema(name = "version",description = "Version",title = "version", example = "1.0.0")
    private String version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
