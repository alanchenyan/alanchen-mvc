package com.spring.alanchen.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Alan Chen
 * @description
 * @date 2020-07-28
 */
public class AlanChenDispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private final String BASE_PATH = "com.spring.alanchen";
	
	private List<String> classNames = new ArrayList<String>();
	

	@Override
    public void init(ServletConfig config){

		// 扫描所有的class文件
		scanPackage(BASE_PATH);
		
    }


	private void scanPackage(String basePackage) {
		URL url = this.getClass().getClassLoader().getResource("/"+basePackage.replaceAll("\\.", "/"));
		String fileStr = url.getFile();
		
		File file = new File(fileStr);
		String[] filesStr = file.list();
		for(String path : filesStr) {
			File filePath = new File(fileStr+path);
			if(filePath.isDirectory()) {
				scanPackage(basePackage+"."+path);
			}else {
				classNames.add(basePackage+"."+filePath.getName());
			}
		}
	}



	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
    
}
