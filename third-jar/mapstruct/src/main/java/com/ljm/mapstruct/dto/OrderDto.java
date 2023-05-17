package com.ljm.mapstruct.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public class OrderDto {

    private Long id;

    private String accountNumber;

    private BigDecimal amount;

    private String price;

    private String orderTime;

    private String version;

    private String cur;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCur() {
        return cur;
    }

    public void setCur(String cur) {
        this.cur = cur;
    }


    @Override
    public String toString() {
        return "OrderDto{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", amount=" + amount +
                ", price='" + price + '\'' +
                ", orderTime='" + orderTime + '\'' +
                ", version='" + version + '\'' +
                ", cur='" + cur + '\'' +
                '}';
    }
}
