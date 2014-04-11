declare variable $project := //project;

<factorypath>

	{ if (not ($project/@name = 'wbs-framework')) then (

		<factorypathentry
			kind="PLUGIN"
			id="org.eclipse.jst.ws.annotations.core"
			enabled="true"
			runInBatchMode="false"/>,

		<factorypathentry
			kind="EXTJAR"
			id="/home/james/projects/wbs/wbs-combined/lib/guava-bundle-16.0.1.jar"
			enabled="true"
			runInBatchMode="false"/>,

		<factorypathentry
			kind="WKSPJAR"
			id="/wbs-framework/wbs-framework.jar"
			enabled="true"
			runInBatchMode="false"/>

	) else () }

</factorypath>
