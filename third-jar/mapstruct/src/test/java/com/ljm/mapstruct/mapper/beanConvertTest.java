package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.dto.OrderDto;
import com.ljm.mapstruct.entity.Order;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Or;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class beanConvertTest {

    @Test
    void simpleConvertTest(){

        Order order = new Order();
        //skip init data
        order.setPrice(new BigDecimal("3.0111"));

        OrderDto orderDto = new OrderDto();
        orderDto.setAmount(order.getAmount());
        orderDto.setId(order.getId());
        orderDto.setAccountNumber(order.getAccountNumber());
        // 3.0111 --> $3.01
        orderDto.setPrice("$" + order.getPrice().setScale(2, RoundingMode.HALF_UP));
        orderDto.setRemark("DEFAULT REMARK");

    }
}
