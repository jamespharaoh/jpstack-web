declare variable $project := //project;

<classpath>

	<classpathentry kind="src" path="src"/>
	<classpathentry kind="src" path="test"/>

	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>

	{ for $depends-project
		in $project/depends-projects/depends-project
	return (

		<classpathentry
			combineaccessrules="false"
			kind="src"
			path="{ concat (
				'/',
				$depends-project/@name
			) }"/>

	) }

	{ for $library in document ('libraries.xml') /libraries/library
	return (

		<classpathentry
			kind="lib"
			path="{ concat (
				'../binaries/libraries/',
				$library/@name,
				'-',
				$library/@type,
				'-',
				$library/@version,
				'.jar'
			) }">

			{ if ($library/@source = 'yes') then (
				attribute sourcepath {
					concat (
						'/home/james/projects/txt2/binaries/libraries/',
						$library/@name,
						'-source-',
						$library/@version,
						'.jar'
					)
				}
			) else () }

		</classpathentry>

	) }

	<classpathentry
		kind="output"
		path=".bin"/>

</classpath>
