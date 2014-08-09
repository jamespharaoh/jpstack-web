declare variable $build := /wbs-build;

declare variable $mode external;

<web-app
	xmlns="http://java.sun.com/xml/ns/j2ee"
	version="2.4">

	<display-name>{
		'WBS API'
	}</display-name>

	<!-- context params -->

	<context-param>

		<param-name>{
			'projects'
		}</param-name>

		<param-value>{ string-join (
			for $project in $build/projects/project
			return concat (
				'src/',
				replace ($project/@package, '\\.', '/'),
				'/',
				$project/@name,
				'-project.xml'
			),
		',' ) }</param-value>

	</context-param>

	<context-param>

		<param-name>{
			'beanDefinitionOutputPath'
		}</param-name>

		<param-value>{
			concat (
				'work/api-',
				$mode,
				'-beans'
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
				'api'
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
				',hibernate,api'
			)
		}</param-value>

	</context-param>

	<!-- listeners -->

	<listener>
		<listener-class>wbs.platform.servlet.WbsServletListener</listener-class>
	</listener>

	<!-- filters -->

	<filter>
		<filter-name>responseFilter</filter-name>
		<filter-class>wbs.framework.servlet.BeanFilterProxy</filter-class>
	</filter>

	<!-- filter mappings -->

	<filter-mapping>
		<filter-name>responseFilter</filter-name>
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

</web-app>
