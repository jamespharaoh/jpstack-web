declare variable $project := //project;

declare variable $mode external;

<web-app xmlns="http://java.sun.com/xml/ns/j2ee" version="2.4">

	<display-name>WBS console</display-name>

	<!-- context init params -->

	<context-param>
		<param-name>primaryProjectName</param-name>
		<param-value>{ string ($project/@name) }</param-value>
	</context-param>

	<context-param>
		<param-name>primaryProjectPackageName</param-name>
		<param-value>{ string ($project/@package) }</param-value>
	</context-param>

	<context-param>
		<param-name>beanDefinitionOutputPath</param-name>
		<param-value>../work/console-{$mode}-beans</param-value>
	</context-param>

	<context-param>
		<param-name>layerNames</param-name>
		<param-value>{ string-join ((
			'data',
			'entity',
			'schema',
			'sql',
			'model',
			'hibernate',
			'object',
			'logic',
			'web',
			'console',
			if ($mode = 'test') then ('daemon') else ()
		), ',') }</param-value>
	</context-param>

	<context-param>
		<param-name>configNames</param-name>
		<param-value>{$mode},hibernate,console</param-value>
	</context-param>

	<!-- listeners -->

	<listener>
		<listener-class>wbs.platform.servlet.WbsServletListener</listener-class>
	</listener>

	<!-- filters -->

	<filter>
		<filter-name>encodingFilter</filter-name>
		<filter-class>wbs.framework.servlet.SetCharacterEncodingFilter</filter-class>
		<init-param>
			<param-name>encoding</param-name>
			<param-value>UTF-8</param-value>
		</init-param>
	</filter>

	<filter>
		<filter-name>responseFilter</filter-name>
		<filter-class>wbs.framework.servlet.BeanFilterProxy</filter-class>
	</filter>

	<filter>
		<filter-name>authFilter</filter-name>
		<filter-class>wbs.framework.servlet.BeanFilterProxy</filter-class>
	</filter>

	<!-- filter mappings -->

	<filter-mapping>
		<filter-name>encodingFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<filter-mapping>
		<filter-name>responseFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<filter-mapping>
		<filter-name>authFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<!-- servlets -->

	<servlet>
		<display-name>default</display-name>
		<servlet-name>DefaultServlet</servlet-name>
		<servlet-class>wbs.framework.web.PathHandlerServlet</servlet-class>
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

	{ for $extension in (
		'css',
		'js',
		'gif',
		'txt',
		'png',
		'ico',
		'swf',
		'yml'
	) return (

		<servlet-mapping>
			<servlet-name>default</servlet-name>
			<url-pattern>*.{ $extension }</url-pattern>
		</servlet-mapping>

	) }

	<mime-mapping>
		<extension>ico</extension>
		<mime-type>image/vnd.microsoft.icon</mime-type>
	</mime-mapping>

</web-app>
