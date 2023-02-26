package com.example.third.service;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Service
public class TestServiceImpl implements ITestService {

    private static Logger log = LogManager.getLogger(TestServiceImpl.class);


    @Value("${key.name}")
    private String keyName;

    @Autowired
    LocalContainerEntityManagerFactoryBean entityManagerFactory;


    @Override
    public String getConfig() {

        log.info("This is getConfig");
        log.info("keyName:" + keyName);


        //Get from
        return keyName;
    }


    @Override
    public List<String> getNameListFromDB() {
        log.info("This is getNameListFromDB");
        EntityManager manager = entityManagerFactory.getNativeEntityManagerFactory().createEntityManager();
        Query dataQuery = manager.createNativeQuery("select * from TEST");
        List<Object[]> resultList = dataQuery.getResultList();
        List<String> nameList = new ArrayList<>();
        for(Object[] name: resultList){
            log.info("name:" + name[1].toString());
            nameList.add(name[1].toString());
        }

        return nameList;
    }
}
