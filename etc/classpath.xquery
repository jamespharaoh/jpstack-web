declare variable $module := //txt2-module;

<classpath>

	<classpathentry kind="src" path="src"/>
	<classpathentry kind="src" path="test"/>

	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>

	{ for $depend in $module/depend-module
	return (

		<classpathentry
			combineaccessrules="false"
			kind="src"
			path="/txt2-{$depend/@name}"/>

	) }

	{ for $library in document ('libraries.xml') /libraries/library
	return (

		<classpathentry
			kind="lib"
			path="../binaries/libraries/{$library/@name}"/>

	) }

	<classpathentry kind="output" path=".bin"/>

</classpath>
