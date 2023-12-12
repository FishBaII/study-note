package com.ljm.swaggerdoc.controller;

import com.ljm.swaggerdoc.entity.Account;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("sample")
@Tag(name = "Simple sample")
public class SampleController {


    @Operation(summary = "Say hi")
    @GetMapping("/hi")
    public String sayHi(
            @Parameter(name = "name", description = "名称")
            @RequestParam(defaultValue = "swagger")
            String name
    ){
        return "Hi " + name;
    }

    @Operation(summary ="Get account")
    @PostMapping("/get")
    public Account getAccount(@RequestBody Account account) {
        return account;
    }

}
