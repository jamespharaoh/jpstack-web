declare variable $module := //txt2-module;

declare variable $envs := ('test', 'live');

<project name="{$module/@name}" basedir="." default="build">

	<property file="build.properties"/>

	<path id="classpath">
		<fileset dir="../lib" includes="*.jar"/>
		{ for $depend in $module/depend-module
		return (
			<pathelement path="../txt2-{$depend/@name}/bin"/>
		) }
		<pathelement path="bin"/>
	</path>

	<target
		name="svn-up"
		depends="{ string-join ((
			'just-svn-up-deps',
			'just-svn-up'
		), ', ') }"/>

	<target
		name="clean"
		depends="{ string-join ((
			'just-clean-deps',
			'just-clean'
		), ', ') }"/>

	<target
		name="build"
		depends="{ string-join ((
			'just-build-deps',
			'just-build'
		), ', ') }"/>

	<target
		name="console-live"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-console-live'
		), ', ') }"/>

	<target
		name="console-test"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-console-test'
		), ', ') }"/>

	<target
		name="console-auto"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-console-live',
			'just-console-restart'
		), ', ') }"/>

	<target
		name="api-live"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-api-live'
		), ', ') }"/>

	<target
		name="api-test"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-api-test'
		), ', ') }"/>

	<target
		name="api-auto"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-api-live',
			'just-api-restart'
		), ', ') }"/>

	<target
		name="daemon-auto"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-daemon-restart'
		), ', ') }"/>

	<target
		name="all-auto"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
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
		name="cukes"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-build-tests',
			'just-cukes'
		), ', ') }"/>

	<target
		name="sql"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-db-drop',
			'just-db-create',
			'just-sql-schema-deps',
			'just-sql-schema',
			'just-sql-data-deps',
			'just-sql-data'
		), ', ') }"/>

	<target
		name="sql-schema"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-db-drop',
			'just-db-create',
			'just-sql-schema-deps',
			'just-sql-schema'
		), ', ') }"/>

	<target
		name="sql-data"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-sql-data-deps',
			'just-sql-data'
		), ', ') }"/>

	<target
		name="fixtures"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-build-tests-deps',
			'just-build-tests',
			'just-db-drop',
			'just-db-create',
			'just-sql-schema-deps',
			'just-sql-schema',
			'just-sql-data-deps',
			'just-sql-data',
			'just-fixtures'
		), ', ') }"/>

	<target name="just-clean-deps">
		{ for $depend in $module/depend-module
		return (
			<ant dir="../txt2-{$depend/@name}" target="just-clean"/>
		) }
	</target>

	<target name="just-clean">
		<delete dir="bin"/>
		{ for $dir in (
			for $env in $envs return (
				concat ('api/', $env),
				concat ('console/', $env),
				concat ('tomcat-', $env)
			)
		) return (
			<delete includeemptydirs="true">
				<fileset
					dir="{$dir}"
					includes="**/*"
					erroronmissingdir="false"/>
			</delete>
		) }
	</target>

	<target name="just-build-deps">
		{ for $depend in $module/depend-module
		return (
			<ant dir="../txt2-{$depend/@name}" target="just-build"/>
		) }
	</target>

	<target name="just-build-tests-deps">
		{ for $depend in $module/depend-module
		return (
			<ant dir="../txt2-{$depend/@name}" target="just-build-tests"/>
		) }
	</target>

	<target name="just-build">

		<mkdir dir="bin"/>

		<javac destdir="bin" debug="on" includeantruntime="false">
			<src path="src"/>
			<classpath refid="classpath"/>
		</javac>

		<copy todir="bin"><fileset dir="src">
			<include name="**/*.xml"/>
			<include name="log4j.properties"/>
		</fileset></copy>

	</target>

	<target name="just-build-tests">

		<javac destdir="bin" debug="on" includeantruntime="false">
			<src path="test"/>
			<classpath refid="classpath"/>
		</javac>

		<copy todir="bin">
			<fileset dir="test">
				<include name="**/*.xml"/>
			</fileset>
		</copy>

	</target>

	<target name="just-svn-up-deps">
		{ for $depend in $module/depend-module
		return (
			<ant dir="../txt2-{$depend/@name}" target="just-svn-up"/>
		) }
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

			<mkdir dir="console/{$env}"/>
			<mkdir dir="console/{$env}/WEB-INF"/>
			<mkdir dir="console/{$env}/WEB-INF/classes"/>
			<mkdir dir="console/{$env}/WEB-INF/lib"/>

			<copy todir="console/{$env}">
				{ for $depend in $module/depend-module
				return (
					<fileset dir="../txt2-{$depend/@name}/console/files"/>
				) }
				<fileset dir="console/files"/>
			</copy>

			<copy todir="console/{$env}/WEB-INF/classes">
				{ for $depend in $module/depend-module
				return (
					<fileset dir="../txt2-{$depend/@name}/bin"/>
				) }
				<fileset dir="bin"/>
			</copy>

			<copy todir="console/{$env}/WEB-INF/lib">
				<fileset dir="lib" excludes="servlet-api.jar"/>
			</copy>

			<copy file="console/web-{$env}.xml" tofile="console/{$env}/WEB-INF/web.xml"/>

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
				<arg line="--file ../../binaries/packages/apache-tomcat-6.0.37.tar.gz"/>
			</exec>

			<delete dir="tomcat-{$env}/**/*"/>

			<move
				file="temp/apache-tomcat-6.0.37"
				tofile="tomcat-{$env}"/>

			<delete dir="temp"/>

			<!-- configure tomcat -->

			<copy
				file="console/server-{$env}.xml"
				tofile="tomcat-{$env}/conf/server.xml"/>

			<copy
				file="../conf/tomcat-users.xml"
				tofile="tomcat-{$env}/conf/tomcat-users.xml"/>

			<!-- deploy console -->

			<delete dir="tomcat-{$env}/apps/console/ROOT"/>

			<copy todir="tomcat-{$env}/apps/console/ROOT">
				<fileset dir="console/test"/>
			</copy>

			<copy todir="tomcat-{$env}/apps/console/manager">
				<fileset dir="tomcat-{$env}/webapps/manager"/>
			</copy>

			<copy todir="tomcat-{$env}/apps/console/host-manager">
				<fileset dir="tomcat-{$env}/webapps/host-manager"/>
			</copy>

			<!-- deploy api -->

			<delete dir="tomcat-{$env}/apps/api/ROOT"/>

			<copy todir="tomcat-{$env}/apps/api/ROOT">
				<fileset dir="api/test"/>
			</copy>

			<copy todir="tomcat-{$env}/apps/api/manager">
				<fileset dir="tomcat-{$env}/webapps/manager"/>
			</copy>

			<copy todir="tomcat-{$env}/apps/api/host-manager">
				<fileset dir="tomcat-{$env}/webapps/host-manager"/>
			</copy>

			<!-- start tomcat -->

			<exec
				failonerror="true"
				dir="tomcat-{$env}"
				executable="bin/catalina.sh">
				<arg line="run"/>
			</exec>

		</target>

	) }

	{ for $env in $envs
	return (

		<target name="just-api-{$env}">
			<mkdir dir="api/{$env}"/>
			<mkdir dir="api/{$env}/WEB-INF"/>
			<mkdir dir="api/{$env}/WEB-INF/classes"/>
			<mkdir dir="api/{$env}/WEB-INF/lib"/>
			<copy todir="api/{$env}">
				{ for $depend in $module/depend-module
				return (
					<fileset dir="../txt2-{$depend/@name}/api/files"/>
				) }
				<fileset dir="api/files"/>
			</copy>
			<copy todir="api/{$env}/WEB-INF/classes">
				{ for $depend in $module/depend-module
				return (
					<fileset dir="../txt2-{$depend/@name}/bin"/>
				) }
				<fileset dir="bin"/>
			</copy>
			<copy todir="api/{$env}/WEB-INF/lib">
				<fileset dir="lib" excludes="servlet-api.jar"/>
			</copy>
			<copy file="api/web-{$env}.xml" tofile="api/{$env}/WEB-INF/web.xml"/>
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

		<javadoc destdir="javadoc" access="private" linksource="yes">

			{ for $depend in $module/depend-module
			return (
				<fileset dir="../txt2-{$depend/@name}/src"/>
			) }

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
			<arg line="txt2-test"/>
		</exec>

	</target>

	<target name="just-db-create">

		<exec
			failonerror="true"
			executable="createdb">
			<arg line="txt2-test"/>
		</exec>

	</target>

	<target name="just-sql-schema-deps">
		{ for $depend in $module/depend-module
		return (
			<ant dir="../txt2-{$depend/@name}" target="just-sql-schema"/>
		) }
	</target>

	<target name="just-sql-data-deps">
		{ for $depend in $module/depend-module
		return (
			<ant dir="../txt2-{$depend/@name}" target="just-sql-data"/>
		) }
	</target>

	<target name="just-sql-schema">

		{ if ($module/sql-schema) then (

			<taskdef
				name="database-init"
				classname="txt2.utils.ant.DatabaseInitTask"
				classpathref="classpath"/>,

			<database-init>
				{ for $sql-schema in $module/sql-schema
				return (
					<script name="sql/{$sql-schema/@name}.sql"/>
				) }
			</database-init>

		) else () }

	</target>

	<target name="just-sql-data">

		{ if ($module/sql-data) then (

			<taskdef
				name="database-init"
				classname="txt2.utils.ant.DatabaseInitTask"
				classpathref="classpath"/>,

			<database-init>
				{ for $sql-data in $module/sql-data
				return (
					<script name="sql/{$sql-data/@name}.sql"/>
				) }
			</database-init>

		) else () }

	</target>

	<target name="just-cukes">

		<java
			classname="cucumber.api.cli.Main"
			fork="true"
			failonerror="false"
			resultproperty="cucumber.exitstatus">

			<classpath refid="classpath"/>

			<arg value="--glue"/>
			<arg value="txt2.psychic.cuke"/>

			<arg value="features"/>

		</java>

	</target>

	<target name="rebuild">

		<exec
			dir=".."
			executable="etc/rebuild"
			failonerror="true"/>

	</target>

	<target name="just-fixtures">

		<java
			classname="txt2.test.TestFixtures"
			classpathref="classpath">
			{ for
				$module-name in (
					$module/@name,
					for $depend in $module/* [name () = 'depend-module']
					return $depend/@name
				),
				$layer-name in (
					'model',
					'hibernate',
					'misc'
				)
			return (
				<arg line="{ concat (
					'classpath:/txt2/',
					replace ($module-name, '-', ''),
					'/',
					$layer-name,
					'/',
					$module-name,
					'-',
					$layer-name,
					'-beans.xml'
				) }"/>
			) }
		</java>

	</target>

</project>
