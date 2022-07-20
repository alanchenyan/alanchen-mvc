package com.spring.alanchen.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spring.alanchen.myspring.annaotation.AlanChenAutowired;
import com.spring.alanchen.myspring.annaotation.AlanChenController;
import com.spring.alanchen.myspring.annaotation.AlanChenRequestMapping;
import com.spring.alanchen.myspring.annaotation.AlanChenRequestParam;
import com.spring.alanchen.service.IUserSerivce;


/**
 * @author Alan Chen
 * @description 访问：http://127.0.0.1:8080/alanchen-mvc/user/query?name=ac&age=18
 * @date 2020-07-28
 */
@AlanChenController
@AlanChenRequestMapping("/user")
public class UserController {

	@AlanChenAutowired
	IUserSerivce userSerivceImpl;

	@AlanChenRequestMapping("/query")
	public String query(HttpServletRequest request, HttpServletResponse response,
			@AlanChenRequestParam("name") String name, @AlanChenRequestParam("age") String age) {
		
		return userSerivceImpl.query(name, age);
	}
}
