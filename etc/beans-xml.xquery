declare variable $module := //txt2-module;

declare variable $mode external;

declare function local:hyphenated-to-camel-case (
	$string as xs:string
) as xs:string {

	string-join (
		for $word at $position in tokenize ($string, '-')
		return if ($position = 1) then (
			$word
		) else concat (
			upper-case (substring ($word, 1, 1)),
			substring ($word, 2)
		),
		''
	)

};

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

	{ for $package in $module/* [name () = 'package']
	return (

		for $group in $package/* [name () = 'group']
		return (

			for $model in $group/* [name () = 'model']
			return (

				if ($mode = 'hibernate') then (

					<bean
						id="{$model/@name}ObjectHelperProvider"
						class="txt2.core.hibernate.object.ObjectHelperProviderFactory">

						<property name="modelHelper">
							<ref bean="{$model/@name}ModelHelper"/>
						</property>

					</bean>

				) else if ($mode = 'model') then (

					<bean
						id="{$model/@name}ObjectHelper"
						class="txt2.core.misc.object.ObjectHelperFactory">

						<property name="objectName">
							<value>{ string ($model/@name) }</value>
						</property>

						<property name="objectHelperClassName">
							<value>{ concat (
								'txt2.',
								$package/@name,
								'.model.',
								$group/@name,
								'.',
								upper-case (substring ($model/@name, 1, 1)),
								substring ($model/@name, 2),
								'Rec$',
								upper-case (substring ($model/@name, 1, 1)),
								substring ($model/@name, 2),
								'ObjectHelper'
							) }</value>
						</property>

					</bean>

				) else if ($mode = 'console') then (

					<bean
						id="{$model/@name}ConsoleHelper"
						class="txt2.core.console.object.ConsoleHelperFactory">

						<property name="consoleHelperProvider">
							<ref bean="{$model/@name}ConsoleHelperProvider"/>
						</property>

						<property name="consoleHelperClassName">
							<value>{ concat (
								'txt2.',
								$package/@name,
								'.console.',
								$group/@name,
								'.',
								upper-case (substring ($model/@name, 1, 1)),
								substring ($model/@name, 2),
								'ConsoleHelper'
							) }</value>
						</property>

					</bean>

				) else ()

			)

		)

	) }

	{ if ($mode = 'misc') then (
		<bean
			id="{local:hyphenated-to-camel-case ($module/@name)}Module"
			class="txt2.core.misc.module.Txt2ModuleFactory">
			<property name="name">
				<value>{string ($module/@name)}</value>
			</property>
		</bean>
	) else () }

	{ for $beans in $module/* [name () = concat ($mode, '-beans')]
	return $beans/* }

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

</beans>
