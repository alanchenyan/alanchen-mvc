package com.spring.alanchen.annaotation.service.impl;

import com.spring.alanchen.annaotation.AlanChenService;
import com.spring.alanchen.annaotation.service.IAlanChenSerivce;

/**
 * @author Alan Chen
 * @description
 * @date 2020-07-28
 */
@AlanChenService("alanChenSerivceImpl")
public class AlanChenSerivceImpl implements IAlanChenSerivce {

    public String query(String name, String age) {
        return "name="+name+";age"+age;
    }
}
