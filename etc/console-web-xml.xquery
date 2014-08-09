declare variable $build := /wbs-build;

declare variable $mode external;

<web-app
	xmlns="http://java.sun.com/xml/ns/j2ee"
	version="2.4">

	<display-name>{
		'WBS console'
	}</display-name>

	<!-- context init params -->

	<context-param>

		<param-name>{
			'beanDefinitionOutputPath'
		}</param-name>

		<param-value>{
			concat (
				'work/',
				$mode,
				'/output/console-beans'
			)
		}</param-value>

	</context-param>

	<context-param>

		<param-name>{
			'layerNames'
		}</param-name>

		<param-value>{
			string-join ((
				'config',
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
				if ($mode = 'test') then (
					'daemon'
				) else ()
			), ',')
		}</param-value>

	</context-param>

	<context-param>

		<param-name>{
			'configNames'
		}</param-name>

		<param-value>{
			concat (
				$mode,
				',hibernate,console'
			)
		}</param-value>

	</context-param>

	<!-- listeners -->

	<listener>

		<listener-class>{
			'wbs.platform.servlet.WbsServletListener'
		}</listener-class>

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
		<filter-name>coreAuthFilter</filter-name>
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
		<filter-name>coreAuthFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<!-- servlets -->

	<servlet>
		<display-name>default</display-name>
		<servlet-name>pathHandlerServlet</servlet-name>
		<servlet-class>wbs.framework.servlet.BeanServletProxy</servlet-class>
	</servlet>

	<!-- servlet mappings -->

	<servlet-mapping>
		<servlet-name>pathHandlerServlet</servlet-name>
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
