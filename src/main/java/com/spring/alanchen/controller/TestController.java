package com.spring.alanchen.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spring.alanchen.annaotation.AlanChenAutowired;
import com.spring.alanchen.annaotation.AlanChenController;
import com.spring.alanchen.annaotation.AlanChenRequestMapping;
import com.spring.alanchen.annaotation.AlanChenRequestParam;
import com.spring.alanchen.annaotation.service.IAlanChenSerivce;

/**
 * @author Alan Chen
 * @description
 * @date 2020-07-28
 */
@AlanChenController
@AlanChenRequestMapping("/alanchen")
public class TestController {

    @AlanChenAutowired("alanChenSerivceImpl")
    IAlanChenSerivce alanChenSerivceImpl;

    @AlanChenRequestMapping("/query")
    public void query(
            HttpServletRequest request, HttpServletResponse response,
            @AlanChenRequestParam("name") String name, @AlanChenRequestParam("age") String age){

        String reuslt = alanChenSerivceImpl.query(name,age);
        PrintWriter pw = null;
        try {
            pw = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
            pw.close();
        }
        pw.write(reuslt);
        pw.close();
    }
}
