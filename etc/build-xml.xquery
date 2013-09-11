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
		<exec executable="svn">
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
		<exec executable="./service">
			<arg value="tomcat_api"/>
			<arg value="restart"/>
		</exec>
	</target>

	<target name="just-console-restart">
		<exec executable="./service">
			<arg value="tomcat_console"/>
			<arg value="restart"/>
		</exec>
	</target>

	<target name="just-daemon-restart">
		<exec executable="./service">
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
			<link href="http://java.sun.com/j2se/1.5.0/docs/api"/>
			<link href="http://logging.apache.org/log4j/docs/api"/>
			<link href="http://www.hibernate.org/hib_docs/v3/api"/>
			<link href="http://www.xom.nu/apidocs"/>
		</javadoc>
	</target>

	<target name="just-cukes">

		<taskdef
			name="database-init"
			classname="txt2.psychic.cuke.DatabaseInitTask"
			classpathref="classpath"/>

		<database-init/>

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

</project>
