package com.ljm.mapstruct.mapper;

import com.alibaba.fastjson2.JSON;
import com.ljm.mapstruct.dto.OrderDto;
import com.ljm.mapstruct.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class BeanMapTest {

    @Test
    void simpleMapTest(){

        Order order = orderInit();

        OrderDto orderDto = new OrderDto();
        orderDto.setAccountNumber(order.getAccountNumber());
        orderDto.setCur(order.getCurrency());
        //set constant
        orderDto.setVersion("3.0.0");
        // 1.51 --> 2;  -2 --> 0; null --> 0
        orderDto.setAmount(halfUp(order.getAmount()));
        // 3.0111 --> $3.01
        if (order.getPrice() != null) {
            orderDto.setPrice(this.createDecimalFormat("$#.00").format(order.getPrice()));
        }

        if (order.getId() != null) {
            orderDto.setId(order.getId());
        } else {
            orderDto.setId(-1L);
        }

        if (order.getOrderTime() != null) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            orderDto.setOrderTime(order.getOrderTime().format(df));
        }

        assertEquals("$3.01", orderDto.getPrice());
        assertEquals("1", orderDto.getAmount().toString());
        assertTrue(orderDto.getOrderTime() instanceof String);

    }

    private DecimalFormat createDecimalFormat(String numberFormat) {
        DecimalFormat df = new DecimalFormat(numberFormat);
        df.setParseBigDecimal(true);
        return df;
    }

    private BigDecimal halfUp(BigDecimal price){
        if(price == null || price.compareTo(BigDecimal.ZERO) < 0){
            return BigDecimal.ZERO;
        }
        return price.setScale(0, RoundingMode.HALF_UP);
    }

    @Test
    void springCopyPropertiesTest(){

        Order order = orderInit();
        OrderDto orderDto = new OrderDto();
        BeanUtils.copyProperties(order, orderDto, OrderDto.class);

        assertNull(orderDto.getOrderTime());
        assertNull(orderDto.getPrice());
        assertNull(orderDto.getCur());


    }


    @Test
    void jsonMapTest(){

        Order order = orderInit();
        String jsonStr = JSON.toJSONString(order);
        OrderDto orderDto = JSON.parseObject(jsonStr, OrderDto.class);

        assertNull(orderDto.getCur());

    }

    @Test
    void mapStructTest(){

        Order order = orderInit();
        OrderDto orderDto = OrderMapper.INSTANCE.toDto(order);

        assertEquals("$3.01", orderDto.getPrice());
        assertEquals("1", orderDto.getAmount().toString());
        assertTrue(orderDto.getOrderTime() instanceof String);


    }

    private Order orderInit(){
        Order order = new Order();
        order.setId(1L);
        order.setOrderTime(LocalDateTime.now());
        order.setPrice(new BigDecimal("3.0111"));
        order.setAmount(new BigDecimal("1.36"));
        order.setAccountNumber("P-00000001");
        order.setVersion("0.0.1");
        order.setCurrency("SGD");
        return order;

    }


}
