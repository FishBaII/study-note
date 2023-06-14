package com.ljm.swagger.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ApiModel("entity for order")
public class Order {

    @ApiModelProperty(value = "order id", position = 0)
    private Long id;

    @ApiModelProperty(value = "order number")
    private String accountNumber;

    @ApiModelProperty(value = "order amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "order price unit")
    private BigDecimal price;

    @ApiModelProperty(value = "order time")
    private LocalDateTime orderTime;

    @ApiModelProperty(hidden = true)
    private String version;

    @ApiModelProperty(value = "used currency")
    private String currency;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", amount=" + amount +
                ", price=" + price +
                ", orderTime=" + orderTime +
                ", version='" + version + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
