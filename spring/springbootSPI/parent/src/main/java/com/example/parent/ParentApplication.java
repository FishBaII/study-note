package com.example.parent;

import com.example.parent.annotate.EnableThirdBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@ComponentScan(value = "com.example.third")
public class ParentApplication {



    public static void main(String[] args) {
        SpringApplication.run(ParentApplication.class, args);
    }


}
