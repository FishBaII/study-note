package com.ljm.mapstruct.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public class OrderDto {

    private Long id;

    private String accountNumber;

    private BigDecimal amount;

    private String price;

    private String orderTime;

    private String remark;

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

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
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
        return "OrderDto{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", amount=" + amount +
                ", price='" + price + '\'' +
                ", orderTime='" + orderTime + '\'' +
                ", remark='" + remark + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
