package com.ljm.swagger.controller;


import com.ljm.swagger.entity.Order;
import com.ljm.swagger.response.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("order")
@Api(tags = "API for order")
public class OrderController {

    Logger logger = LoggerFactory.getLogger(OrderController.class);


    @PutMapping
    @ApiOperation("create order")
    public CommonResult add(@RequestBody Order order){

        logger.info(order.toString());

        return CommonResult.success(order);

    }

    @DeleteMapping("{id}")
    @ApiOperation("delete order")
    public CommonResult delete(@PathVariable @ApiParam("order id") Long id){

        logger.info(id.toString());
        return CommonResult.success(id);

    }



    @PostMapping
    @ApiOperation("update order")
    public CommonResult updated(@RequestBody Order order){

        logger.info(order.toString());
        return CommonResult.success(order);

    }

    @GetMapping
    @ApiOperation("find order")
    public CommonResult getOrderList(){

        Order order = new Order();
        order.setId(1L);
        order.setAccountNumber("P-0234345345");
        List<Order> list = new ArrayList<>();
        list.add(order);
        return CommonResult.success(list);

    }
}
