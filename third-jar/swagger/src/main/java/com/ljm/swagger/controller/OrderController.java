package com.ljm.swagger.controller;


import com.ljm.swagger.entity.Order;
import com.ljm.swagger.entity.OrderType;
import com.ljm.swagger.response.CommonResult;
import com.ljm.swagger.security.UserLoginToken;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("order")
@Api(tags = "API for order")
public class OrderController {

    Logger logger = LoggerFactory.getLogger(OrderController.class);


    @PutMapping
    @ApiOperation("create order")
    @UserLoginToken
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "token is invalid", examples =
            @Example(value = {
                    @ExampleProperty(
                            mediaType = MediaType.APPLICATION_JSON_VALUE, value = "{\n\"code\": 401 \n\"message\": \"token is invalid!\"\n}")
            }))
    })

    public CommonResult add(@RequestBody Order order){

        logger.info(order.toString());

        return CommonResult.success(order);

    }

    @DeleteMapping("{id}")
    @ApiOperation("delete order")
    @UserLoginToken
    @ApiImplicitParam(name = "token", value = "token in header", paramType = "header")
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
    @UserLoginToken
    public CommonResult getOrderList(){

        Order order = new Order();
        order.setId(1L);
        order.setAccountNumber("P-0234345345");
        List<Order> list = new ArrayList<>();
        list.add(order);
        return CommonResult.success(list);

    }

    @GetMapping("/type")
    @ApiOperation("find order by type")
    public CommonResult getOrderListByType(OrderType type){
        Order order = new Order();
        order.setId(1L);
        order.setOrderType(type);
        order.setAccountNumber("P-0234345345");
        List<Order> list = new ArrayList<>();
        list.add(order);
        return CommonResult.success(list);
    }


    @GetMapping("/list")
    @ApiOperation("select by map")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", paramType = "query", dataType="Long"),
            @ApiImplicitParam(name = "currency", value = "币种", paramType = "query", dataType="String", allowableValues = "USD,SGD"),
            @ApiImplicitParam(name = "accountNumber", value = "账号", paramType = "query", dataType="String", example = "P-000001")
    })
    public CommonResult getByMap(@ApiIgnore @RequestParam Map<String, Object> params){
        logger.info(params.toString());
        return CommonResult.success(null);
    }
}
