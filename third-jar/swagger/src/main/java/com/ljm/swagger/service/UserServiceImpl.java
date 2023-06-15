package com.ljm.swagger.service;

import com.ljm.swagger.entity.SystemUser;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserServiceImpl implements UserService{
    @Override
    public SystemUser findUserById(Long id) {

        SystemUser user = null;
        //get user
        if(Objects.equals(id, 1L)){
            user = new SystemUser();
            user.setUserName("admin");
            user.setId(1L);
            user.setPassword("12345");
        }

        return user;
    }
}
