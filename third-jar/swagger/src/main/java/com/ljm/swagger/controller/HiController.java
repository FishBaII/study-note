package com.ljm.swagger.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("hi")
public class HiController {

    @GetMapping("/name")
    public String sayHi(String name){
        return name + " hi!";
    }
}
