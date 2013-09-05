declare variable $module := //txt2-module;

<projectDescription>

	<name>txt2-{string ($module/@name)}</name>
	<comment></comment>
	<projects>
	</projects>

	<buildSpec>
		<buildCommand>
			<name>org.eclipse.jdt.core.javabuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
		<buildCommand>
			<name>com.stateofflow.eclipse.metrics.MetricsBuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>

	<natures>
		<nature>org.eclipse.jdt.core.javanature</nature>
		<nature>com.stateofflow.eclipse.metrics.MetricsNature</nature>
	</natures>

	<filteredResources>
		<filter>
			<id>1378377069204</id>
			<name></name>
			<type>10</type>
			<matcher>
				<id>org.eclipse.ui.ide.multiFilter</id>
				<arguments>1.0-projectRelativePath-matches-false-false-bin</arguments>
			</matcher>
		</filter>
	</filteredResources>

</projectDescription>
