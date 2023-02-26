package com.example.parent;

import com.example.third.service.ITestService;
import com.example.third.util.TestUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ParentApplicationTests {

    private static Logger log = LogManager.getLogger(ParentApplicationTests.class);


    @Autowired
    private ITestService testService;

    @Test
    void contextLoads() {
        log.info("Get str in parent:" + TestUtil.getStr());
        log.info("Get key name in parent:" + testService.getConfig());
        log.info("Get name list in parent:" + testService.getNameListFromDB());

    }

}
