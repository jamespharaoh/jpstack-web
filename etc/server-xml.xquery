<Server port="8005" shutdown="SHUTDOWN">

	<Listener
		className="org.apache.catalina.core.AprLifecycleListener"
		SSLEngine="on"/>

	<Listener
		className="org.apache.catalina.core.JasperListener"/>

	<Listener
		className="org.apache.catalina.mbeans.ServerLifecycleListener"/>

	<Listener
		className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener"/>

	<GlobalNamingResources>

		<Resource
			name="UserDatabase"
			auth="Container"
			type="org.apache.catalina.UserDatabase"
			description="User database that can be updated and saved"
			factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
			pathname="conf/tomcat-users.xml"/>

	</GlobalNamingResources>

	<Service name="console">

		<Executor
			name="console-executor"
			namePrefix="console-exec-"
			maxThreads="150"
			minSpareThreads="4"/>

		<Connector
			port="8080"
			protocol="HTTP/1.1"
			connectionTimeout="20000"
			redirectPort="8443" />

		<Engine
			name="console-engine"
			defaultHost="localhost">

			<Realm
				className="org.apache.catalina.realm.UserDatabaseRealm"
				resourceName="UserDatabase"/>

			<Host
				name="localhost"
				appBase="apps/console"
				unpackWARs="true"
				autoDeploy="true"
				xmlValidation="false"
				xmlNamespaceAware="false">

			</Host>

		</Engine>

	</Service>

	<Service name="api">

		<Executor
			name="api-executor"
			namePrefix="api-exec-"
			maxThreads="150"
			minSpareThreads="4"/>

		<Connector
			port="8081"
			protocol="HTTP/1.1"
			connectionTimeout="20000"
			redirectPort="8444" />

		<Engine
			name="api-engine"
			defaultHost="localhost">

			<Realm
				className="org.apache.catalina.realm.UserDatabaseRealm"
				resourceName="UserDatabase"/>

			<Host
				name="localhost"
				appBase="apps/api"
				unpackWARs="true"
				autoDeploy="true"
				xmlValidation="false"
				xmlNamespaceAware="false">

			</Host>

		</Engine>

	</Service>

</Server>
