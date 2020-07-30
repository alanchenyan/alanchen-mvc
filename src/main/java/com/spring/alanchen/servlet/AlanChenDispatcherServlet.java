package com.spring.alanchen.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spring.alanchen.annaotation.AlanChenAutowired;
import com.spring.alanchen.annaotation.AlanChenController;
import com.spring.alanchen.annaotation.AlanChenRequestMapping;
import com.spring.alanchen.annaotation.AlanChenRequestParam;
import com.spring.alanchen.annaotation.AlanChenService;

/**
 * @author Alan Chen
 * @description
 * @date 2020-07-28
 */
public class AlanChenDispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private Properties contextConfig = new Properties();

	private List<String> classNames = new ArrayList<String>();

	private Map<String, Object> beans = new HashMap<String, Object>();

	private Map<String, Handler> handlerMapping = new HashMap<String, Handler>();

	@Override
	public void init(ServletConfig config) {
		
		// 1、加载配置文件
		doLoadConfig(config);

		// 2、扫描所有的class文件
		doScanPackage(contextConfig.getProperty("scanPackage"));

		// 3、根据扫描的全类名进行实例化
		doInstance();

		// 4、处理依赖注入
		doIoc();

		// 5、映射访问路径和方法的关系
		initHandlerMapping();
	}

	/**
	 * 加载配置文件
	 * @param config
	 */
	private void doLoadConfig(ServletConfig config) {
		String contextConfigLocation = config.getInitParameter("contextConfigLocation");
		InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
		try {
			contextConfig.load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(inStream!=null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doService(req,resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doService(req,resp);
	}
	
	private void doService(HttpServletRequest req, HttpServletResponse resp) {
		String uri = req.getRequestURI();
		String context = req.getContextPath();
		String path = uri.replace(context, "");

		Handler handler = handlerMapping.get(path);
		Method method =handler.getMethod();
		Object controller =handler.getController();

		Object arg[] = hand(req, resp, method);
		try {
			method.invoke(controller, arg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Object[] hand(HttpServletRequest req, HttpServletResponse resp, Method method) {
		Class<?>[] paramClazzs = method.getParameterTypes();
		Object[] args = new Object[paramClazzs.length];

		int args_i = 0;
		int index = 0;

		for (Class<?> paramClazz : paramClazzs) {
			if (ServletRequest.class.isAssignableFrom(paramClazz)) {
				args[args_i++] = req;
			}

			if (ServletResponse.class.isAssignableFrom(paramClazz)) {
				args[args_i++] = resp;
			}

			Annotation[] paramAns = method.getParameterAnnotations()[index];
			if (paramAns.length > 0) {
				for (Annotation paramAn : paramAns) {
					if (AlanChenRequestParam.class.isAssignableFrom(paramAn.getClass())) {
						AlanChenRequestParam rp = (AlanChenRequestParam) paramAn;
						args[args_i++] = req.getParameter(rp.value());
					}
				}
			}
			index++;
		}

		return args;
	}

	/**
	 * 映射访问路径和方法的关系
	 */
	private void initHandlerMapping() {
		if (beans.entrySet().size() == 0) {
			System.out.println("实例化的类数量为0");
			return;
		}

		try {

			for (Map.Entry<String, Object> entry : beans.entrySet()) {
				Object instance = entry.getValue();
				Class<?> clazz = instance.getClass();
				if (clazz.isAnnotationPresent(AlanChenController.class)) {
					AlanChenRequestMapping mapping = clazz.getAnnotation(AlanChenRequestMapping.class);
					String classPath = mapping.value();

					Method[] methods = clazz.getMethods();
					for (Method method : methods) {
						if (method.isAnnotationPresent(AlanChenRequestMapping.class)) {
							AlanChenRequestMapping methodMapping = method.getAnnotation(AlanChenRequestMapping.class);
							String methodPath = methodMapping.value();
							
							Handler handler = new Handler();
							handler.setController(instance);
							handler.setMethod(method);
							
							handlerMapping.put(classPath + methodPath, handler);
						} else {
							continue;
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理依赖注入
	 */
	private void doIoc() {
		if (beans.entrySet().size() == 0) {
			System.out.println("实例化的类数量为0");
			return;
		}

		try {

			for (Map.Entry<String, Object> entry : beans.entrySet()) {
				Object instance = entry.getValue();
				Class<?> clazz = instance.getClass();
				Field[] fileds = clazz.getDeclaredFields();

				for (Field field : fileds) {
					if (field.isAnnotationPresent(AlanChenAutowired.class)) {
						AlanChenAutowired autowired = field.getAnnotation(AlanChenAutowired.class);
						field.setAccessible(true);

						String beanName = autowired.value();
						if(beanName.isEmpty()) {
							beanName = field.getType().getName();
						}
						
						Object obj = beans.get(beanName);

						field.set(instance, obj);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 根据扫描的全类名进行实例化
	 */
	private void doInstance() {
		if (classNames.size() == 0) {
			System.out.println("扫描到的class文件数量为0");
			return;
		}

		try {
			for (String className : classNames) {
				String cn = className.replace(".class", "");
				Class<?> clazz = Class.forName(cn);

				if (clazz.isAnnotationPresent(AlanChenController.class)) {
					// 初始化Controller类
					Object instace = clazz.newInstance();
					String beanName = lowerFirstCase(clazz.getSimpleName());
					beans.put(beanName, instace);
				} else if (clazz.isAnnotationPresent(AlanChenService.class)) {
					// 初始化Service类
					Object instace = clazz.newInstance();
					AlanChenService service = clazz.getAnnotation(AlanChenService.class);
					
					// 1、优先使用用户配置的value值做为key
					String beanName = service.value();
					if(beanName.isEmpty()) {
						// 2、如果没有配置value值，则用类名首字母小写的类名做为key
						beanName = lowerFirstCase(clazz.getSimpleName());
					}
					beans.put(beanName, instace);
					
					// 3、用实现接口的类型名称做为key，注入接口的实现类
					Class<?>[] intefaces = clazz.getInterfaces();
					for(Class inteface : intefaces) {
						beans.put(inteface.getName(), instace);
					}
					
				} else {
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String lowerFirstCase(String str) {
		char[] chars = str.toCharArray();
		chars[0]+=32;
		return String.valueOf(chars);
	}

	/**
	 * 扫描所有的class文件
	 * 
	 * @param basePackage
	 */
	private void doScanPackage(String basePackage) {
		URL url = this.getClass().getClassLoader().getResource("/" + basePackage.replaceAll("\\.", "/"));
		String fileStr = url.getFile();

		File file = new File(fileStr);
		String[] filesStr = file.list();
		for (String path : filesStr) {
			File filePath = new File(fileStr + path);
			if (filePath.isDirectory()) {
				doScanPackage(basePackage + "." + path);
			} else {
				classNames.add(basePackage + "." + filePath.getName());
			}
		}
	}

}
