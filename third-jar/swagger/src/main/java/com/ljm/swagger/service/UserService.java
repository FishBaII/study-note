package com.ljm.swagger.service;

import com.ljm.swagger.entity.SystemUser;

public interface UserService {

    SystemUser findUserById(Long id);
}
