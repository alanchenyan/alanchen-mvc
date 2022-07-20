package com.spring.alanchen.service.impl;

import com.spring.alanchen.myspring.annaotation.AlanChenService;
import com.spring.alanchen.service.IUserSerivce;

/**
 * @author Alan Chen
 * @description
 * @date 2020-07-28
 */
@AlanChenService
public class UserSerivceImpl implements IUserSerivce {

    public String query(String name, String age) {
        return "name="+name+";age"+age;
    }
}
