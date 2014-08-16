declare variable $build := /wbs-build;

declare variable $envs := ('test', 'live');

declare function local:project (
	$build-project as element (project)
) as element (project) {

	document (
		concat (
			'../src/',
			replace (
				$build-project/@package,
				'\.',
				'/'),
			'/',
			$build-project/@name,
			'-project.xml'
		)
	) / project

};

declare function local:plugin (
	$project as element (project),
	$project-plugin as element (plugin)
) as element (plugin) {

	let

		$plugin :=
			document (
				concat (
					'../src/',
					replace (
						$project/@package,
						'\.',
						'/'),
					'/',
					replace (
						$project-plugin/@package,
						'\.',
						'/'),
					'/',
					$project-plugin/@name,
					'-plugin.xml'
				)
			) / plugin

	return

		element plugin {
			$plugin/@*,
			$plugin/*,
			$project
		}

};

declare variable $all-projects :=

	for

		$build-project
			in $build/projects/project

	return

		local:project (
			$build-project);


declare variable $all-plugins :=
	for

		$project
			in $all-projects,

		$project-plugin
			in $project/plugin

	return

		local:plugin (
			$project,
			$project-plugin);

<project
	name="{$build/@name}"
	basedir="."
	default="build">

	<property
		environment="env"/>

	<property
		file="${{env.WBS_BUILD_PROPERTIES}}"/>

	<path
		id="classpath">

		<fileset
			dir="lib"
			includes="*.jar"/>

		<pathelement
			path="work/bin"/>

	</path>

	<target
		name="svn-up"
		depends="{ string-join ((
			'just-svn-up'
		), ', ') }"/>

	<target
		name="clean"
		depends="{ string-join ((
			'just-clean'
		), ', ') }"/>

	<target
		name="build"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest'
		), ', ') }"/>

	<target
		name="framework-jar"
		depends="{ string-join ((
			'just-build-framework',
			'just-framework-jar'
		), ', ') }"/>

	<target
		name="console-live"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-console-live'
		), ', ') }"/>

	<target
		name="console-test"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-console-test'
		), ', ') }"/>

	<target
		name="console-auto"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-console-live',
			'just-console-restart'
		), ', ') }"/>

	<target
		name="api-live"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-api-live'
		), ', ') }"/>

	<target
		name="api-test"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-api-test'
		), ', ') }"/>

	<target
		name="api-auto"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-api-live',
			'just-api-restart'
		), ', ') }"/>

	<target
		name="daemon-auto"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-daemon-restart'
		), ', ') }"/>

	<target
		name="all-auto"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-api-live',
			'just-api-restart',
			'just-console-live',
			'just-console-restart',
			'just-daemon-restart'
		), ', ') }"/>

	<target
		name="javadoc-auto"
		depends="{ string-join ((
			'just-javadoc'
		), ', ') }"/>

	<target
		name="sql"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-db-drop',
			'just-db-create',
			'just-sql-schema',
			'just-sql-data'
		), ', ') }"/>

	<target
		name="sql-schema"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-db-drop',
			'just-db-create',
			'just-sql-schema'
		), ', ') }"/>

	<target
		name="sql-data"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-sql-data'
		), ', ') }"/>

	<target
		name="fixtures"
		depends="{ string-join ((
			'just-build-framework',
			'just-build-rest',
			'just-db-drop',
			'just-db-create',
			'just-schema-create',
			'just-sql-schema',
			'just-sql-data',
			'just-fixtures'
		), ', ') }"/>

	<target name="just-clean">
		<delete dir="work"/>
	</target>

	<target name="just-build-framework">

		<mkdir
			dir="work/bin"/>

		<javac
			destdir="work/bin"
			debug="on"
			includeantruntime="false"
			srcdir="src"
			includes="wbs/framework/**"
			classpathref="classpath"/>

		<mkdir
			dir="work/bin/META-INF/services"/>

		<echo
			file="work/bin/META-INF/services/javax.annotation.processing.Processor"
			message="wbs.framework.object.ObjectHelperAnnotationProcessor"/>

	</target>

	<target name="just-framework-jar">

		<jar
			destfile="work/wbs-framework.jar"
			basedir="work/bin">

			<include
				name="wbs/framework/**"/>

			<service
				type="javax.annotation.processing.Processor"
				provider="wbs.framework.object.ObjectHelperAnnotationProcessor"/>

		</jar>

	</target>

	<target name="just-build-rest">

		<javac
			destdir="work/bin"
			debug="on"
			includeantruntime="false"
			srcdir="src"
			excludes="wbs/framework/**"
			classpathref="classpath"/>

		<copy todir="work/bin"><fileset dir="src">
			<include name="**/*.xml"/>
			<include name="log4j.properties"/>
		</fileset></copy>

		<copy
			file="wbs-build.xml"
			todir="work/bin"/>

	</target>

	<target name="just-svn-up">

		<exec
			failonerror="true"
			executable="svn">
			<arg value="up"/>
		</exec>

	</target>

	{ for $env in $envs
	return (

		<target name="just-console-{$env}">

			<mkdir dir="work/{$env}/console"/>
			<mkdir dir="work/{$env}/console/WEB-INF"/>
			<mkdir dir="work/{$env}/console/WEB-INF/classes"/>
			<mkdir dir="work/{$env}/console/WEB-INF/lib"/>

			<copy
				todir="work/{$env}/console"
				failonerror="false">

				{ for

					$plugin in
						$all-plugins

				return (

					<fileset
						dir="{ concat (
							'src/',
							replace (
								$plugin/project/@package,
								'\.',
								'/'),
							'/',
							replace (
								$plugin/@package,
								'\.',
								'/'),
							if (
								matches (
									$plugin/@package,
									'(^|\.)console($|\.)')
							) then (
								'/files'
							) else (
								'/console/files'
							)
						) }"/>

				) }

				<fileset
					dir="console/files"/>

			</copy>

			<copy todir="work/{$env}/console/WEB-INF/classes">
				<fileset dir="work/bin"/>
			</copy>

			<copy todir="work/{$env}/console/WEB-INF/lib">
				<fileset dir="lib" excludes="servlet-api.jar"/>
			</copy>

			<copy
				file="console/web-{$env}.xml"
				tofile="work/{$env}/console/WEB-INF/web.xml"/>

		</target>,

		<target
			name="tomcat-{$env}"
			depends="console-{$env}, api-{$env}">

			<!-- install tomcat -->

			<mkdir dir="temp"/>

			<exec
				failonerror="true"
				dir="temp"
				executable="tar">
				<arg line="--extract"/>
				<arg line="--file ../binaries/packages/apache-tomcat-6.0.37.tar.gz"/>
			</exec>

			<delete
				dir="work/{$env}/tomcat/**/*"/>

			<move
				file="temp/apache-tomcat-6.0.37"
				tofile="work/{$env}/tomcat"/>

			<delete dir="temp"/>

			<!-- configure tomcat -->

			<copy
				file="console/server-{$env}.xml"
				tofile="work/{$env}/tomcat/conf/server.xml"/>

			<copy
				file="conf/tomcat-users.xml"
				tofile="work/{$env}/tomcat/conf/tomcat-users.xml"/>

			<!-- deploy console -->

			<delete dir="work/{$env}/tomcat/apps/console/ROOT"/>

			<copy todir="work/{$env}/tomcat/apps/console/ROOT">
				<fileset dir="work/{$env}/console"/>
			</copy>

			<copy todir="work/{$env}/tomcat/apps/console/manager">
				<fileset dir="work/{$env}/tomcat/webapps/manager"/>
			</copy>

			<copy todir="work/{$env}/tomcat/apps/console/host-manager">
				<fileset dir="work/{$env}/tomcat/webapps/host-manager"/>
			</copy>

			<!-- deploy api -->

			<delete dir="work/{$env}/tomcat/apps/api/ROOT"/>

			<copy todir="work/{$env}/tomcat/apps/api/ROOT">
				<fileset dir="work/{$env}/api"/>
			</copy>

			<copy todir="work/{$env}/tomcat/apps/api/manager">
				<fileset dir="work/{$env}/tomcat/webapps/manager"/>
			</copy>

			<copy todir="work/{$env}/tomcat/apps/api/host-manager">
				<fileset dir="work/{$env}/tomcat/webapps/host-manager"/>
			</copy>

			<!-- start tomcat -->

			<exec
				failonerror="true"
				executable="work/{$env}/tomcat/bin/catalina.sh">
				<arg line="run"/>
			</exec>

		</target>

	) }

	{ for $env in $envs
	return (

		<target name="just-api-{$env}">

			<mkdir dir="work/{$env}/api"/>
			<mkdir dir="work/{$env}/api/WEB-INF"/>
			<mkdir dir="work/{$env}/api/WEB-INF/classes"/>
			<mkdir dir="work/{$env}/api/WEB-INF/lib"/>

			<copy
				todir="work/{$env}/api"
				failonerror="false">

				{ for

					$plugin in
						$all-plugins

				return (

					<fileset
						dir="{ concat (
							'src/',
							replace (
								$plugin/project/@package,
								'\.',
								'/'),
							'/',
							replace (
								$plugin/@package,
								'\.',
								'/'),
							'/api/files'
						) }"/>

				) }

				<fileset dir="api/files"/>

			</copy>

			<copy todir="work/{$env}/api/WEB-INF/classes">
				<fileset dir="work/bin"/>
			</copy>

			<copy
				todir="work/{$env}/api/WEB-INF/lib">

				<fileset
					dir="lib"
					excludes="servlet-api.jar"/>

			</copy>

			<copy
				file="api/web-{$env}.xml"
				tofile="work/{$env}/api/WEB-INF/web.xml"/>

		</target>

	) }

	<target name="just-api-restart">
		<exec executable="./service" failonerror="true">
			<arg value="tomcat_api"/>
			<arg value="restart"/>
		</exec>
	</target>

	<target name="just-console-restart">
		<exec executable="./service" failonerror="true">
			<arg value="tomcat_console"/>
			<arg value="restart"/>
		</exec>
	</target>

	<target name="just-daemon-restart">
		<exec executable="./service" failonerror="true">
			<arg value="daemon"/>
			<arg value="restart"/>
		</exec>
	</target>

	<target name="just-javadoc">

		<mkdir dir="javadoc"/>

		<javadoc
			destdir="javadoc"
			access="private"
			linksource="yes">

			<fileset dir="src"/>

			<classpath refid="classpath"/>

			<link href="http://java.sun.com/j2se/1.6.0/docs/api"/>
			<link href="http://logging.apache.org/log4j/docs/api"/>
			<link href="http://www.hibernate.org/hib_docs/v3/api"/>
			<link href="http://www.xom.nu/apidocs"/>

		</javadoc>

	</target>

	<target name="just-db-drop">

		<exec
			failonerror="false"
			executable="dropdb">

			<arg line="${{database.name}}"/>

		</exec>

	</target>

	<target name="just-db-create">

		<exec
			failonerror="true"
			executable="createdb">

			<arg line="${{database.name}}"/>

		</exec>

	</target>

	<target name="just-sql-schema">

		<taskdef
			name="database-init"
			classname="wbs.framework.utils.ant.DatabaseInitTask"
			classpathref="classpath"/>

		<database-init>

			{ for

				$plugin in
					$all-plugins,

				$project in
					$plugin/project,

				$sql-schema in
					$plugin/sql-scripts/sql-schema

			return (

				<script
					name="{ concat (
						'src/',
						replace (
							$project/@package,
							'\.',
							'/'),
						'/',
						replace (
							$plugin/@package,
							'\.',
							'/'),
						'/model/',
						$sql-schema/@name,
						'.sql'
					) }"/>

			) }

		</database-init>

	</target>

	<target name="just-sql-data">

		<taskdef
			name="database-init"
			classname="wbs.framework.utils.ant.DatabaseInitTask"
			classpathref="classpath"/>

		<database-init>

			{ for

				$plugin in
					$all-plugins,

				$project in
					$plugin/project,

				$sql-data in
					$plugin/sql-scripts/sql-data

			return (

				<script
					name="{ concat (
						'src/',
						replace (
							$project/@package,
							'\.',
							'/'),
						'/',
						replace (
							$plugin/@package,
							'\.',
							'/'),
						'/model/',
						$sql-data/@name,
						'.sql'
					) }"/>

			) }

		</database-init>

	</target>

	<target name="rebuild">

		<exec
			executable="etc/rebuild"
			failonerror="true"/>

	</target>

	<target name="just-fixtures">

		<java
			classname="wbs.platform.application.tools.BeanRunner"
			classpathref="classpath"
			failonerror="true">

			<arg line="wbs-test"/>
			<arg line="wbs.test"/>

			<arg line="{ string-join ((
				'config',
				'data',
				'entity',
				'schema',
				'sql',
				'model',
				'hibernate',
				'object',
				'logic',
				'fixture'
			), ',') }"/>

			<arg line="test,hibernate"/>
			<arg line="wbs.framework.fixtures.FixturesTool"/>
			<arg line="createFixtures"/>

		</java>

	</target>

	<target name="just-schema-create">

		<java
			classname="wbs.platform.application.tools.BeanRunner"
			classpathref="classpath"
			failonerror="true">
			<arg line="wbs-test"/>
			<arg line="wbs.test"/>
			<arg line="config,data,entity,schema,sql,schema-tool"/>
			<arg line="test"/>
			<arg line="wbs.framework.schema.tool.SchemaTool"/>
			<arg line="schemaCreate"/>
		</java>

	</target>

</project>
