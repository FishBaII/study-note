package com.ljm.swagger.controller;

import com.ljm.swagger.entity.Order;
import com.ljm.swagger.entity.SystemUser;
import com.ljm.swagger.response.CommonResult;
import com.ljm.swagger.util.TokenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
@Api(tags = "API for user")
public class userController {


    Logger logger = LoggerFactory.getLogger(userController.class);


    @PostMapping("/login")
    @ApiOperation("uesr login")
    public CommonResult login(@RequestBody SystemUser user) throws Exception {

        logger.info(user.toString());

        String token = TokenUtil.createToken(user);

        logger.info("token:" + token);

        return CommonResult.success(token);

    }

}
