declare variable $build := /wbs-build;

<classpath>

	<classpathentry
		kind="src"
		path="src"/>

	<classpathentry
		kind="con"
		path="org.eclipse.jdt.launching.JRE_CONTAINER"/>

	{ for

		$library in
			document ('libraries.xml') /libraries/library

	return (

		<classpathentry
			kind="lib"
			path="{ concat (
				'binaries/libraries/',
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
						'/home/vagrant/',
						$build/@name,
						'/binaries',
						'/libraries/',
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
		kind="src"
		path="work/eclipse/generated">

		<attributes>

			<attribute
				name="optional"
				value="true"/>

		</attributes>

	</classpathentry>

	<classpathentry
		kind="output"
		path="work/eclipse/bin"/>

</classpath>
