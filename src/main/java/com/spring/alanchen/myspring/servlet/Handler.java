package com.spring.alanchen.myspring.servlet;

import java.lang.reflect.Method;

/**
 * @author Alan Chen
 * @description
 * @date 2020-07-28
 */
public class Handler {

	private Object controller;
	
	private Method method;

	public Object getController() {
		return controller;
	}

	public void setController(Object controller) {
		this.controller = controller;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
}
