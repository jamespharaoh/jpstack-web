declare variable $build := /wbs-build;

<factorypath>

	<factorypathentry
		kind="PLUGIN"
		id="org.eclipse.jst.ws.annotations.core"
		enabled="true"
		runInBatchMode="false"/>

	<factorypathentry
		kind="WKSPJAR"
		id="/{$build/@name}/binaries/libraries/guava-bundle-16.0.1.jar"
		enabled="true"
		runInBatchMode="false"/>

	<factorypathentry
		kind="WKSPJAR"
		id="/{$build/@name}/binaries/libraries/joda-time-jar-2.3.jar"
		enabled="true"
		runInBatchMode="false"/>

	<factorypathentry
		kind="WKSPJAR"
		id="/{$build/@name}/work/wbs-framework.jar"
		enabled="true"
		runInBatchMode="false"/>

</factorypath>
