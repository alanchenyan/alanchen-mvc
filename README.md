# alanchen-mvc

#### 一、实现功能
手写实现简单Spring:MVC、IOC、DI功能。

#### 二、说明
该项目需要部署在tomcat等容器中运行。

#### 三、SpringMVC(alanchen-mvc)加载顺序 
1、tomcat 
2、web.xml 
3、DispatchServlet(AlanChenDispatcherServlet)
4、Servlet.init()初始化方法

#### 四、init核心代码
```java
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
	}
```

#### 五、pom.xml
加入javax.servlet依赖
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>com.alanchen</groupId>
	<artifactId>alanchen-mvc</artifactId>
	<version>0.0.1-SNAPSHOT</version>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>6</source>
                    <target>6</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
    <packaging>war</packaging>

	<dependencies>
		<dependency>
			<groupId>javax.servlet</groupId>
			<artifactId>servlet-api</artifactId>
			<version>3.0-alpha-1</version>
			<scope>provided</scope>
		</dependency>
	</dependencies>
</project>
```

#### 六、WEB-INF/web.xml配置
配置Servlet
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://java.sun.com/xml/ns/javaee"
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
	version="2.5">
	<display-name>alanchen-mvc</display-name>
	
	<servlet>
		<servlet-name>AlanChenDispatcherServlet</servlet-name>
		<servlet-class>com.spring.alanchen.myspring.servlet.AlanChenDispatcherServlet</servlet-class>
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>application.properties</param-value>
		</init-param>
		<!-- 表示容器在应用启动时就加载并初始化这个servlet，实例化并调用其init()方法 -->
		<load-on-startup>0</load-on-startup>
	</servlet>
	
	<servlet-mapping>
		<servlet-name>AlanChenDispatcherServlet</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>
	
</web-app>
```
