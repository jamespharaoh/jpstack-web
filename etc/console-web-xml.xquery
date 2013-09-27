declare variable $module := //txt2-module;

declare variable $mode external;

<web-app xmlns="http://java.sun.com/xml/ns/j2ee" version="2.4">

	<display-name>Txt2 console</display-name>

	<!-- ============================================== context init params -->

	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>{ string-join ((
			concat (
				'classpath:txt2/',
				replace ($module/@name, '-', ''),
				'/console/',
				$module/@name,
				'-console-beans.xml'
			),
			concat (
				'classpath:txt2/',
				replace ($module/@name, '-', ''),
				'/hibernate/',
				$module/@name,
				'-hibernate-beans.xml'
			),
			concat (
				'classpath:txt2/',
				replace ($module/@name, '-', ''),
				'/misc/',
				$module/@name,
				'-misc-beans.xml'
			),
			if ($mode = 'test') then (
				'classpath:txt2/test/console/test-console-beans.xml',
				'classpath:txt2/test/daemon/test-daemon-beans.xml',
				'classpath:txt2/test/hibernate/test-hibernate-beans.xml',
				'classpath:txt2/test/misc/test-misc-beans.xml'
			) else ()
		), ' ') }</param-value>
	</context-param>

	<context-param>
		<param-name>contextInitializerClasses</param-name>
		<param-value>{ string-join ((
			'txt2.servlet.Txt2WebContextInitializer'
		), ' ') }</param-value>
	</context-param>

	<!-- ======================================================== listeners -->

	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

	<!-- ========================================================== filters -->

	<filter>
		<filter-name>authFilter</filter-name>
		<filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
	</filter>

	<!-- ================================================== filter mappings -->

	<filter-mapping>
		<filter-name>authFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<!-- ========================================================= servlets -->

	<servlet>
		<display-name>default</display-name>
		<servlet-name>DefaultServlet</servlet-name>
		<servlet-class>txt2.servlet.PathHandlerServlet</servlet-class>
		<init-param>
			<param-name>pathHandler</param-name>
			<param-value>rootPathHandler</param-value>
		</init-param>
		<init-param>
			<param-name>notFoundHandler</param-name>
			<param-value>notFoundHandler</param-value>
		</init-param>
		<init-param>
			<param-name>exceptionHandler</param-name>
			<param-value>exceptionHandler</param-value>
		</init-param>
	</servlet>

	<!-- servlet mappings -->

	<servlet-mapping>
		<servlet-name>DefaultServlet</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>
	<servlet-mapping>
		<servlet-name>default</servlet-name>
		<url-pattern>*.css</url-pattern>
	</servlet-mapping>
	<servlet-mapping>
		<servlet-name>default</servlet-name>
		<url-pattern>*.js</url-pattern>
	</servlet-mapping>
	<servlet-mapping>
		<servlet-name>default</servlet-name>
		<url-pattern>*.gif</url-pattern>
	</servlet-mapping>
	<servlet-mapping>
		<servlet-name>default</servlet-name>
		<url-pattern>*.txt</url-pattern>
	</servlet-mapping>
	<servlet-mapping>
		<servlet-name>default</servlet-name>
		<url-pattern>*.png</url-pattern>
	</servlet-mapping>
	<servlet-mapping>
		<servlet-name>default</servlet-name>
		<url-pattern>*.ico</url-pattern>
	</servlet-mapping>
	<servlet-mapping>
		<servlet-name>default</servlet-name>
		<url-pattern>*.swf</url-pattern>
	</servlet-mapping>
	<servlet-mapping>
		<servlet-name>default</servlet-name>
		<url-pattern>*.yml</url-pattern>
	</servlet-mapping>

	<mime-mapping>
		<extension>ico</extension>
		<mime-type>image/vnd.microsoft.icon</mime-type>
	</mime-mapping>

</web-app>
