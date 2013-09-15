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
			'just-sql-deps',
			'just-sql'
		), ', ') }"/>

	<target
		name="sql-test"
		depends="{ string-join ((
			'just-build-deps',
			'just-build',
			'just-db-drop',
			'just-db-create',
			'just-sql-deps',
			'just-sql',
			'just-sql-test-deps',
			'just-sql-test'
		), ', ') }"/>

	<target name="just-clean-deps">
		{ for $depend in $module/depend-module
		return (
			<ant dir="../txt2-{$depend/@name}" target="just-clean"/>
		) }
	</target>

	<target name="just-clean">
		<delete dir="bin"/>
		<delete dir="console/live"/>
		<delete dir="console/test"/>
		<delete dir="console/tomcat-live"/>
		<delete dir="console/tomcat-test"/>
		<delete dir="api/live"/>
		<delete dir="api/test"/>
		<delete dir="api/tomcat-live"/>
		<delete dir="api/tomcat-test"/>
	</target>

	<target name="just-build-deps">
		{ for $depend in $module/depend-module
		return (
			<ant dir="../txt2-{$depend/@name}" target="just-build"/>
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

		<target name="tomcat-{$env}" depends="console-{$env}">

			<mkdir dir="temp"/>

			<exec
				failonerror="true"
				dir="temp"
				executable="tar">
				<arg line="--extract"/>
				<arg line="--file ../../binaries/packages/apache-tomcat-6.0.37.tar.gz"/>
			</exec>

			<delete dir="console/tomcat-{$env}"/>

			<move
				file="temp/apache-tomcat-6.0.37"
				tofile="console/tomcat-{$env}"/>

			<delete dir="temp"/>

			<delete dir="console/tomcat-{$env}/webapps/ROOT"/>

			<copy todir="console/tomcat-{$env}/webapps/ROOT">
				<fileset dir="console/test"/>
			</copy>

			<exec
				failonerror="true"
				dir="console/tomcat-{$env}"
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

	<target name="just-sql-deps">
		{ for $depend in $module/depend-module
		return (
			<ant dir="../txt2-{$depend/@name}" target="just-sql"/>
		) }
	</target>

	<target name="just-sql-test-deps">
		{ for $depend in $module/depend-module
		return (
			<ant dir="../txt2-{$depend/@name}" target="just-sql-test"/>
		) }
	</target>

	<target name="just-sql">

		{ if ($module/sql) then (

			<taskdef
					name="database-init"
				classname="txt2.utils.ant.DatabaseInitTask"
				classpathref="classpath"/>,

			<database-init>
				{ for $sql in $module/sql
				return (
					<script name="sql/{$sql/@name}.sql"/>
				) }
			</database-init>

		) else () }

	</target>

	<target name="just-sql-test">

		{ if ($module/sql-test) then (

			<taskdef
				name="database-init"
				classname="txt2.utils.ant.DatabaseInitTask"
				classpathref="classpath"/>,

			<database-init>
				{ for $sql in $module/sql-test
				return (
					<script name="sql/{$sql/@name}.sql"/>
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

</project>
