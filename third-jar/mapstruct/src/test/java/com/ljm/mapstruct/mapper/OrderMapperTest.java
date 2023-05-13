package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.dto.OrderDto;
import com.ljm.mapstruct.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;
    @Test
    void toDtoWithDefaultRemark() {

        Order order = new Order();
        order.setRemark("test");

        OrderDto orderDto = orderMapper.toDto(order);
        assertEquals("default remark", orderDto.getRemark());
    }


    @Test
    void toDtoWithAmount() {

        Order order = new Order();
        order.setAmount(new BigDecimal("35.87"));

        OrderDto orderDto = orderMapper.toDto(order);
        assertEquals("36", orderDto.getAmount().toString());
    }


    @Test
    void toDtoWithOrderTime() {

        Order order = new Order();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss");
        String nowStr = now.format(df);
        order.setOrderTime(LocalDateTime.now());

        OrderDto orderDto = orderMapper.toDto(order);
        assertEquals(nowStr, orderDto.getOrderTime());
    }

    @Test
    void toDtoWithPrice() {

        Order order = new Order();
        order.setPrice(new BigDecimal("82.987"));

        OrderDto orderDto = orderMapper.toDto(order);
        assertEquals("$82.99", orderDto.getPrice());
    }


    @Test
    void toModelWithPrice() {

        OrderDto orderDto = new OrderDto();
        orderDto.setPrice("$82.99");
        orderDto.setAmount(new BigDecimal("145.815"));
        orderDto.setRemark("remark");

        Order order = orderMapper.toModel(orderDto);
        assertEquals("82.99", order.getPrice().toString());
    }
}