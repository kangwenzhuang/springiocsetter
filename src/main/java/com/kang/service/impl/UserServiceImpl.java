package com.kang.service.impl;

import com.kang.dao.UserDao;
import com.kang.service.UserService;

public class UserServiceImpl implements UserService {
    private UserDao userDao;

    public void addUser() {
        userDao.addUser();
    }
}
