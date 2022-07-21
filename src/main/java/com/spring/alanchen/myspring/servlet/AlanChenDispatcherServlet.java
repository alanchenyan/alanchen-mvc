package com.spring.alanchen.myspring.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spring.alanchen.myspring.annaotation.AlanChenAutowired;
import com.spring.alanchen.myspring.annaotation.AlanChenController;
import com.spring.alanchen.myspring.annaotation.AlanChenRequestMapping;
import com.spring.alanchen.myspring.annaotation.AlanChenRequestParam;
import com.spring.alanchen.myspring.annaotation.AlanChenService;

/**
 * @author Alan Chen
 * @description
 * 
 * 	SpringMVC加载顺序 1、tomcat 2、web.xml 3、DispatchServlet
 * 
 *  MVC作为入口 启动IOC容器 完成DI
 * 
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

		// 3、初始化IOC容器，将扫描到的类实例化，保存到IOC容器中(IOC部分)
		doInstance();

		// TODO 在DI之前完成AOP(新生成的代理对象)

		// 4、完成依赖注入(DI部分)
		doAutowired();

		// 5、映射访问路径和方法的关系(MVC部分)
		initHandlerMapping();

		System.out.println("alanchenMVC is init.");
	}

	/**
	 * 加载配置文件
	 * 
	 * @param config
	 */
	private void doLoadConfig(ServletConfig config) {
		String contextConfigLocation = config.getInitParameter("contextConfigLocation");
		InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
		try {
			contextConfig.load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		doDispatch(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		doDispatch(req, resp);
	}

	/**
	 * 6、委派，通过URL去找到一个对应的Method并通过Response返回
	 * 
	 * @param req
	 * @param resp
	 */
	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
		String uri = req.getRequestURI();
		String context = req.getContextPath();
		String path = uri.replace(context, "");

		Handler handler = handlerMapping.get(path);
		Method method = handler.getMethod();
		Object controller = handler.getController();

		Object arg[] = hand(req, resp, method);
		Object result=null;
		try {
			result = method.invoke(controller, arg);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		if(result!=null) {
			writeResult(resp,result);
		}else {
			System.out.println("没有返回值");
		}
	}
	
	/**
	 * 将返回值写回客户端
	 * @param resp
	 * @param result
	 */
	private void writeResult(HttpServletResponse resp,Object result) {
		PrintWriter pw = null;
		try {
			String obj = String.valueOf(result);
			pw = resp.getWriter();
			pw.write(obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	/**
	 * 获取请求参数值
	 * 
	 * @param req
	 * @param resp
	 * @param method
	 * @return
	 */
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
				
				//用取反的方式减少if嵌套层数
				if (!clazz.isAnnotationPresent(AlanChenController.class)) {
					continue;
				}
				
				AlanChenRequestMapping mapping = clazz.getAnnotation(AlanChenRequestMapping.class);
				String classPath = "/"+mapping.value();
		
				/**
				 * clazz.getDeclaredMethods() 是获取所有方法
				 * clazz.getMethods() 只获取public修饰的方法
				 * Spring也只处理public的方法
				 */
				Method[] methods = clazz.getMethods();
				
				for (Method method : methods) {
					if (!method.isAnnotationPresent(AlanChenRequestMapping.class)) {
						continue;
					}
					
					AlanChenRequestMapping methodMapping = method.getAnnotation(AlanChenRequestMapping.class);
					String methodPath = "/"+methodMapping.value();

					Handler handler = new Handler();
					handler.setController(instance);
					handler.setMethod(method);
					
					//将多个重复的斜杠替换成一个斜杠
					String url = (classPath + methodPath).replaceAll("/+", "/");
					handlerMapping.put(url, handler);
					
					System.out.println("url="+url+";method="+method.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 完成依赖注入(DI)
	 */
	private void doAutowired() {
		if (beans.entrySet().size() == 0) {
			System.out.println("实例化的类数量为0");
			return;
		}

		try {
			for (Map.Entry<String, Object> entry : beans.entrySet()) {
				Object instance = entry.getValue();
				Class<?> clazz = instance.getClass();
				
				//获取所有属性，包括private修饰的属性
				Field[] fileds = clazz.getDeclaredFields();

				for (Field field : fileds) {
					if (!field.isAnnotationPresent(AlanChenAutowired.class)) {
						continue;
					}
					
					AlanChenAutowired autowired = field.getAnnotation(AlanChenAutowired.class);

					// 即使属性是private，也强制设置为可访问
					field.setAccessible(true);

					// 1、优先通过用户配置的value去取对象
					String beanName = autowired.value();
					if (beanName.isEmpty()) {
						// 2、通过类名首字母小写取对象
						beanName = field.getName();
					}

					Object obj = beans.get(beanName);
					// 如果通过名称无法获取到对象，则通过类型获取
					if (obj == null) {
						// 3、通过接口类型取对象，注入接口的实现类
						beanName = field.getType().getName();
						obj = beans.get(beanName);
					}
					field.set(instance, obj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化IOC容器，将扫描到的类实例化，保存到IOC容器中(IOC部分)
	 */
	private void doInstance() {
		if (classNames.size() == 0) {
			System.out.println("扫描到的class文件数量为0");
			return;
		}

		try {
			for (String className : classNames) {
				Class<?> clazz = Class.forName(className);

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
					if (beanName.isEmpty()) {
						// 2、如果没有配置value值，则用类名首字母小写的类名做为key
						beanName = lowerFirstCase(clazz.getSimpleName());
					}
					System.out.println("beanName=" + beanName);
					beans.put(beanName, instace);

					// 3、用实现接口的类型名称做为key，注入接口的实现类
					Class<?>[] intefaces = clazz.getInterfaces();
					for (Class inteface : intefaces) {
						System.out.println("inteface=" + inteface.getName());
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

	/**
	 * 类名首字母转小写
	 * 
	 * @param str
	 * @return
	 */
	private String lowerFirstCase(String str) {
		if(str == null || str.isEmpty()) {
			return str;
		}
		
		/**
		 * 首字母是大写才转小写，否则不需要处理
		 * 大写字母范围：65-90
		 */
		char[] chars = str.toCharArray();
		char first = chars[0];
		if(first>=65 && first <=90) {
			chars[0] += 32;
			return String.valueOf(chars);
		}
		return str;
	}

	/**
	 * 扫描所有的class文件
	 * 
	 * @param basePackage
	 */
	private void doScanPackage(String basePackage) {
		//硬盘上的目录是用斜杠/分割，因此要将.替换成斜杠/
		URL url = this.getClass().getClassLoader().getResource("/" + basePackage.replaceAll("\\.", "/"));
		
		//fileStr=D://alanchen-mvc/WEB-INF/classes/com/spring/alanchen/
		String fileStr = url.getFile();

		File file = new File(fileStr);
		String[] filesStr = file.list();
		for (String path : filesStr) {
			File filePath = new File(fileStr + path);
			if (filePath.isDirectory()) {
				// 递归直到找到文件为止
				doScanPackage(basePackage + "." + path);
			} else {
				if(filePath.getName().endsWith(".class")) {
					classNames.add(basePackage + "." + filePath.getName().replace(".class", ""));
				}
			}
		}
	}
}
