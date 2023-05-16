package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.dto.OrderDto;
import com.ljm.mapstruct.entity.Order;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class beanConvertTest {

    @Test
    void simpleConvertTest(){

        Order order = orderInit();

        OrderDto orderDto = new OrderDto();
        orderDto.setAccountNumber(order.getAccountNumber());
        orderDto.setRemark("DEFAULT REMARK");

        // 1.51 --> 2
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
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss");
            orderDto.setOrderTime(order.getOrderTime().format(df));
        }

        assertEquals("$3.01", orderDto.getPrice());
        assertEquals("1", orderDto.getAmount().toString());
        assertTrue(orderDto.getOrderTime() instanceof String);



    }

    @Test
    void springCopyPropertiesTest(){

        Order order = orderInit();
        OrderDto orderDto = new OrderDto();
        BeanUtils.copyProperties(order, orderDto, OrderDto.class);

        assertNull(orderDto.getOrderTime());
        assertNull(orderDto.getPrice());

    }

    private Order orderInit(){
        Order order = new Order();
        order.setId(1L);
        order.setOrderTime(LocalDateTime.now());
        order.setPrice(new BigDecimal("3.0111"));
        order.setAmount(new BigDecimal("1.36"));
        order.setAccountNumber("P-00000001");
        order.setRemark("this is remark");
        return order;

    }

    private DecimalFormat createDecimalFormat(String numberFormat) {
        DecimalFormat df = new DecimalFormat(numberFormat);
        df.setParseBigDecimal(true);
        return df;
    }

    private BigDecimal halfUp(BigDecimal price){
        if(price == null){
            return BigDecimal.ZERO;
        }
        return price.setScale(0, RoundingMode.HALF_UP);
    }
}
