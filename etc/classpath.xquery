declare variable $module := //txt2-module;

<classpath>

	<classpathentry kind="src" path="src"/>
	<classpathentry kind="src" path="test"/>

	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
	<classpathentry kind="con" path="org.eclipse.jdt.USER_LIBRARY/txt2-libs"/>

	{ for $depend in $module/depend-module
	return (

		<classpathentry
			combineaccessrules="false"
			kind="src"
			path="/txt2-{$depend/@name}"/>

	) }

	<classpathentry kind="output" path=".bin"/>

</classpath>
