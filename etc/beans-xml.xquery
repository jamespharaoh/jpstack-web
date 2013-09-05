declare variable $module := //txt2-module;

declare variable $mode external;

<beans
	xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xsi:schemaLocation="{ string-join ((

		'http://www.springframework.org/schema/beans',
		'http://www.springframework.org/schema/beans/spring-beans-3.0.xsd',

		'http://www.springframework.org/schema/context',
		'http://www.springframework.org/schema/context/spring-context-3.0.xsd'

	), ' ') }">

	<context:annotation-config/>

	{ for $depend in $module/* [name () = 'depend-module']
	return (
		<import resource="{ concat (
			'classpath:txt2/',
			replace ($depend/@name, '-', ''),
			'/',
			$mode,
			'/',
			$depend/@name,
			'-',
			$mode,
			'-beans.xml'
		) }"/>
	) }

	{ for $package in $module/* [name () = 'package']
	return (
		<context:component-scan
			base-package="{ concat (
				'txt2.',
				$package/@name,
				'.',
				$mode
			) }"/>
	) }

	{ for $beans in $module/* [name () = concat ($mode, '-beans')]
	return $beans/* }

</beans>
