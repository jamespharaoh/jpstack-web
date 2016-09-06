package wbs.framework.application.tools;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.hyphenToCamel;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;
import static wbs.framework.utils.etc.TypeUtils.classForName;
import static wbs.framework.utils.etc.TypeUtils.classForNameRequired;
import static wbs.framework.utils.etc.TypeUtils.isNotSubclassOf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.api.module.ApiModule;
import wbs.api.module.ApiModuleFactory;
import wbs.api.module.ApiModuleSpec;
import wbs.api.module.ApiModuleSpecFactory;
import wbs.console.helper.ConsoleHelperFactory;
import wbs.console.helper.EnumConsoleHelper;
import wbs.console.helper.EnumConsoleHelperFactory;
import wbs.console.module.ConsoleMetaModule;
import wbs.console.module.ConsoleMetaModuleFactory;
import wbs.console.module.ConsoleModule;
import wbs.console.module.ConsoleModuleFactory;
import wbs.console.module.ConsoleModuleSpec;
import wbs.console.module.ConsoleModuleSpecFactory;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.activitymanager.ActivityManagerImplementation;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.ProxiedRequestComponent;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.application.context.ApplicationContextImplementation;
import wbs.framework.application.context.ComponentDefinition;
import wbs.framework.application.context.MethodComponentFactory;
import wbs.framework.application.scaffold.BuildPluginSpec;
import wbs.framework.application.scaffold.BuildSpec;
import wbs.framework.application.scaffold.PluginApiModuleSpec;
import wbs.framework.application.scaffold.PluginComponentSpec;
import wbs.framework.application.scaffold.PluginComponentTypeSpec;
import wbs.framework.application.scaffold.PluginConsoleModuleSpec;
import wbs.framework.application.scaffold.PluginCustomTypeSpec;
import wbs.framework.application.scaffold.PluginDependencySpec;
import wbs.framework.application.scaffold.PluginEnumTypeSpec;
import wbs.framework.application.scaffold.PluginFixtureSpec;
import wbs.framework.application.scaffold.PluginLayerSpec;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginModelsSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHooks;

@Accessors (fluent = true)
@Log4j
public
class ApplicationContextBuilder {

	// properties

	@Getter @Setter
	String primaryProjectPackageName;

	@Getter @Setter
	String primaryProjectName;

	@Getter @Setter
	List <String> configNames =
		new ArrayList<> ();

	@Getter @Setter
	List <String> layerNames =
		new ArrayList<> ();

	@Getter @Setter
	String outputPath;

	// state

	ActivityManager activityManager;

	List<PluginSpec> plugins;
	PluginManager pluginManager;

	Map <String, Object> singletonComponents =
		new LinkedHashMap<> ();

	List <ComponentDefinition> componentDefinitionsToRegister =
		new ArrayList<> ();

	ApplicationContextImplementation applicationContext;

	// implementation

	public
	ApplicationContextBuilder addSingletonComponent (
			@NonNull String singletonName,
			@NonNull Object singletonComponent) {

		singletonComponents.put (
			singletonName,
			singletonComponent);

		return this;

	}

	@SneakyThrows (Exception.class)
	public
	ApplicationContext build () {

		activityManager =
			new ActivityManagerImplementation ();

		loadPlugins ();

		createPluginManager ();

		initContext ();

		return applicationContext;

	}

	private
	void loadPlugins () {

		String buildPath =
			"/wbs-build.xml";

		DataFromXml buildDataFromXml =
			new DataFromXmlBuilder ()

			.registerBuilderClasses (
				BuildSpec.class,
				BuildPluginSpec.class)

			.build ();

		BuildSpec build =
			(BuildSpec)
			buildDataFromXml.readClasspath (
				buildPath);

		ImmutableList.Builder <PluginSpec> pluginsBuilder =
			ImmutableList.builder ();

		DataFromXml pluginDataFromXml =
			new DataFromXmlBuilder ()

			.registerBuilderClasses (
				PluginApiModuleSpec.class,
				PluginComponentSpec.class,
				PluginComponentTypeSpec.class,
				PluginConsoleModuleSpec.class,
				PluginCustomTypeSpec.class,
				PluginEnumTypeSpec.class,
				PluginFixtureSpec.class,
				PluginLayerSpec.class,
				PluginModelSpec.class,
				PluginModelsSpec.class,
				PluginDependencySpec.class,
				PluginSpec.class)

			.build ();

		for (
			BuildPluginSpec buildPlugin
				: build.plugins ()
		) {

			String pluginPath =
				stringFormat (
					"/%s",
					buildPlugin.packageName ().replace (".", "/"),
					"/%s-plugin.xml",
					buildPlugin.name ());

			PluginSpec plugin =
				(PluginSpec)
				pluginDataFromXml.readClasspath (
					pluginPath,
					ImmutableList.of (
						build));

			pluginsBuilder.add (
				plugin);

		}

		plugins =
			pluginsBuilder.build ();

	}

	private
	void createPluginManager () {

		pluginManager =
			new PluginManager.Builder ()
				.plugins (plugins)
				.build ();

		addSingletonComponent (
			"pluginManager",
			pluginManager);

	}

	private
	void initContext ()
		throws Exception {

		TaskLogger taskLog =
			new TaskLogger (
				log);

		createApplicationContext ();

		registerLayerComponents (
			taskLog);

		registerConfigComponents (
			taskLog);

		registerSingletonComponents (
			taskLog);

		if (taskLog.errors ()) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					taskLog.errorCount ()));

		}

		for (
			ComponentDefinition componentDefinition
				: componentDefinitionsToRegister
		) {

			applicationContext.registerComponentDefinition (
				componentDefinition);

		}

		log.info (
			stringFormat (
				"initialising application"));

		applicationContext.init ();

		log.info (
			stringFormat (
				"application initialised"));

	}

	private
	void registerLayerComponents (
			@NonNull TaskLogger taskLog) {

		for (
			String layerName
				: layerNames
		) {

			log.info (
				stringFormat (
					"Loading components for layer %s",
					layerName));

			for (
				PluginSpec plugin
					: plugins
			) {

				PluginLayerSpec layer =
					plugin.layersByName ().get (
						layerName);

				if (layer != null) {

					registerLayerComponents (
						taskLog,
						plugin,
						layer);

				}

			}

			for (
				PluginSpec plugin
					: plugins
			) {

				registerLayerAutomaticComponents (
					taskLog,
					plugin,
					layerName);

			}

		}

	}

	void registerLayerComponents (
			@NonNull TaskLogger taskLog,
			@NonNull PluginSpec plugin,
			@NonNull PluginLayerSpec layer) {

		for (
			PluginComponentSpec component
				: layer.components ()
		) {

			registerLayerComponent (
				taskLog,
				component);

		}

	}

	void registerLayerAutomaticComponents (
			@NonNull TaskLogger taskLog,
			@NonNull PluginSpec plugin,
			@NonNull String layerName) {

		if (
			stringEqualSafe (
				layerName,
				"api")
		) {

			registerApiLayerComponents (
				taskLog,
				plugin);

			plugin.apiModules ().forEach (
				apiModule ->
					registerApiModule (
						taskLog,
						apiModule));

		}

		if (
			stringEqualSafe (
				layerName,
				"console")
		) {

			registerConsoleLayerComponents (
				taskLog,
				plugin);

			plugin.consoleModules ().forEach (
				consoleModule ->
					registerConsoleModule (
						taskLog,
						consoleModule));

		}

		if (
			stringEqualSafe (
				layerName,
				"hibernate")
		) {

			registerHibernateLayerComponents (
				taskLog,
				plugin);

		}

		if (
			stringEqualSafe (
				layerName,
				"object")
		) {

			registerObjectLayerComponents (
				taskLog,
				plugin);

		}

		if (
			stringEqualSafe (
				layerName,
				"fixture")
		) {

			registerFixtureLayerComponents (
				taskLog,
				plugin);

		}

	}

	long registerApiModule (
			@NonNull TaskLogger taskLog,
			@NonNull PluginApiModuleSpec pluginApiModuleSpec) {

		String xmlResourceName =
			stringFormat (
				"/%s/api/%s-api.xml",
				pluginApiModuleSpec
					.plugin ()
					.packageName ()
					.replace (".", "/"),
				pluginApiModuleSpec
					.name ());

		String apiModuleSpecComponentName =
			stringFormat (
				"%sApiModuleSpec",
				hyphenToCamel (
					pluginApiModuleSpec.name ()));

		String apiModuleComponentName =
			stringFormat (
				"%sApiModule",
				hyphenToCamel (
					pluginApiModuleSpec.name ()));

		applicationContext.registerComponentDefinition (
			new ComponentDefinition ()

			.name (
				apiModuleSpecComponentName)

			.componentClass (
				ApiModuleSpec.class)

			.scope (
				"singleton")

			.factoryClass (
				ApiModuleSpecFactory.class)

			.addValueProperty (
				"xmlResourceName",
				xmlResourceName)

		);

		applicationContext.registerComponentDefinition (
			new ComponentDefinition ()

			.name (
				apiModuleComponentName)

			.componentClass (
				ApiModule.class)

			.scope (
				"singleton")

			.factoryClass (
				ApiModuleFactory.class)

			.addReferenceProperty (
				"apiModuleSpec",
				apiModuleSpecComponentName)

		);

		return 0;

	}

	void registerConsoleModule (
			@NonNull TaskLogger taskLog,
			@NonNull PluginConsoleModuleSpec pluginConsoleModuleSpec) {

		String xmlResourceName =
			stringFormat (
				"/%s/console/%s-console.xml",
				pluginConsoleModuleSpec
					.plugin ()
					.packageName ()
					.replace (".", "/"),
				pluginConsoleModuleSpec
					.name ());

		String consoleSpecComponentName =
			stringFormat (
				"%sConsoleModuleSpec",
				hyphenToCamel (
					pluginConsoleModuleSpec.name ()));

		String consoleModuleComponentName =
			stringFormat (
				"%sConsoleModule",
				hyphenToCamel (
					pluginConsoleModuleSpec.name ()));

		String consoleMetaModuleComponentName =
			stringFormat (
				"%sConsoleMetaModule",
				hyphenToCamel (
					pluginConsoleModuleSpec.name ()));

		applicationContext.registerComponentDefinition (
			new ComponentDefinition ()

			.name (
				consoleSpecComponentName)

			.componentClass (
				ConsoleModuleSpec.class)

			.scope (
				"singleton")

			.factoryClass (
				ConsoleModuleSpecFactory.class)

			.addValueProperty (
				"xmlResourceName",
				xmlResourceName)

		);

		applicationContext.registerComponentDefinition (
			new ComponentDefinition ()

			.name (
				consoleModuleComponentName)

			.componentClass (
				ConsoleModule.class)

			.scope (
				"singleton")

			.factoryClass (
				ConsoleModuleFactory.class)

			.addReferenceProperty (
				"consoleSpec",
				consoleSpecComponentName)

		);

		applicationContext.registerComponentDefinition (
			new ComponentDefinition ()

			.name (
				consoleMetaModuleComponentName)

			.componentClass (
				ConsoleMetaModule.class)

			.scope (
				"singleton")

			.factoryClass (
				ConsoleMetaModuleFactory.class)

			.addReferenceProperty (
				"consoleSpec",
				consoleSpecComponentName)

		);

	}

	void registerFixtureLayerComponents (
			@NonNull TaskLogger taskLog,
			@NonNull PluginSpec plugin) {

		for (
			PluginFixtureSpec fixture
				: plugin.fixtures ()
		) {

			String fixtureProviderComponentName =
				stringFormat (
					"%sFixtureProvider",
					fixture.name ());

			String fixtureProviderClassName =
				stringFormat (
					"%s.fixture.%sFixtureProvider",
					plugin.packageName (),
					capitalise (
						fixture.name ()));

			Class<?> fixtureProviderClass;

			try {

				fixtureProviderClass =
					Class.forName (
						fixtureProviderClassName);

			} catch (ClassNotFoundException exception) {

				taskLog.errorFormat (
					"Can't find fixture provider of type %s ",
					fixtureProviderClassName,
					"for fixture %s ",
					fixture.name (),
					"from %s",
					plugin.name ());

				continue;

			}

			applicationContext.registerComponentDefinition (
				new ComponentDefinition ()

				.name (
					fixtureProviderComponentName)

				.componentClass (
					fixtureProviderClass)

				.scope (
					"prototype"));

		}

	}

	void registerHibernateLayerComponents (
			@NonNull TaskLogger taskLog,
			@NonNull PluginSpec plugin) {

		plugin.models ().models ().forEach (
			projectModelSpec ->
				registerDaoHibernate (
					taskLog,
					projectModelSpec));

	}

	void registerObjectLayerComponents (
			@NonNull TaskLogger taskLog,
			@NonNull PluginSpec plugin) {

		plugin.models ().models ().forEach (
			projectModelSpec -> {

			registerObjectHooks (
				taskLog,
				projectModelSpec);

			registerObjectHelper (
				taskLog,
				projectModelSpec);

			registerObjectHelperMethodsImplementation (
				taskLog,
				projectModelSpec);

		});

	}

	void registerApiLayerComponents (
			@NonNull TaskLogger taskLog,
			@NonNull PluginSpec plugin) {

		doNothing ();

	}

	void registerConsoleLayerComponents (
			@NonNull TaskLogger taskLog,
			@NonNull PluginSpec plugin) {

		plugin.models ().models ().forEach (
			pluginModelSpec ->
				registerConsoleHelper (
					taskLog,
					pluginModelSpec));

		plugin.models ().enumTypes ().forEach (
			pluginEnumTypeSpec ->
				registerEnumConsoleHelper (
					taskLog,
					pluginEnumTypeSpec));

		plugin.models ().customTypes ().forEach (
			pluginCustomTypeSpec ->
				registerCustomConsoleHelper (
					taskLog,
					pluginCustomTypeSpec));

	}

	void registerLayerComponent (
			@NonNull TaskLogger taskLog,
			@NonNull PluginComponentSpec componentSpec) {

		log.debug (
			stringFormat (
				"Loading %s from %s",
				componentSpec.className (),
				componentSpec.plugin ().name ()));

		String componentClassName =
			stringFormat (
				"%s.%s",
				componentSpec.plugin ().packageName (),
				componentSpec.className ());

		Class <?> componentClass;

		try {

			componentClass =
				Class.forName (
					componentClassName);

		} catch (ClassNotFoundException exception) {

			taskLog.errorFormat (
				"No such class %s in %s.%s.%s",
				componentClassName,
				componentSpec.plugin ().name (),
				componentSpec.layer ().name (),
				componentSpec.className ());

			return;

		}

		String componentName = null;

		SingletonComponent singletonComponent =
			componentClass.getAnnotation (
				SingletonComponent.class);

		if (singletonComponent != null) {

			componentName =
				singletonComponent.value ();

			applicationContext.registerComponentDefinition (
				new ComponentDefinition ()

				.name (
					componentName)

				.componentClass (
					componentClass)

				.scope (
					"singleton")

			);

		}

		PrototypeComponent prototypeComponent =
			componentClass.getAnnotation (
				PrototypeComponent.class);

		if (prototypeComponent != null) {

			componentName =
				prototypeComponent.value ();

			applicationContext.registerComponentDefinition (
				new ComponentDefinition ()

				.name (
					componentName)

				.componentClass (
					componentClass)

				.scope (
					"prototype")

			);

		}

		ProxiedRequestComponent proxiedRequestComponent =
			componentClass.getAnnotation (
				ProxiedRequestComponent.class);

		if (proxiedRequestComponent != null) {

			componentName =
				proxiedRequestComponent.value ();

			String targetComponentName =
				stringFormat (
					"%sTarget",
					componentName);

			applicationContext.registerComponentDefinition (
				new ComponentDefinition ()

				.name (
					targetComponentName)

				.componentClass (
					componentClass)

				.scope (
					"prototype")

				.hide (
					true)

			);

			applicationContext.registerComponentDefinition (
				new ComponentDefinition ()

				.name (
					componentName)

				.componentClass (
					proxiedRequestComponent.proxyInterface ())

				.factoryClass (
					ThreadLocalProxyComponentFactory.class)

				.scope (
					"singleton")

				.addValueProperty (
					"componentName",
					componentName)

				.addValueProperty (
					"componentClass",
					proxiedRequestComponent.proxyInterface ())

			);

			applicationContext.requestComponentNames ().add (
				componentName);

		}

		if (componentName == null) {

			taskLog.errorFormat (
				"Could not find component annotation on %s",
				componentClass.getName ());

			return;

		}

		for (
			Method method
				: componentClass.getDeclaredMethods ()
		) {

			Named namedAnnotation =
				method.getAnnotation (
					Named.class);

			SingletonComponent singletonComponentAnnotation =
				method.getAnnotation (
					SingletonComponent.class);

			if (
				isNotNull (
					singletonComponentAnnotation)
			) {

				if (
					stringNotEqualSafe (
						method.getName (),
						singletonComponentAnnotation.value ())
				) {

					log.warn (
						stringFormat (
							"Factory method name '%s' ",
							method.getName (),
							"does not match component name '%s' ",
							singletonComponentAnnotation.value ()));

				}

				applicationContext.registerComponentDefinition (
					new ComponentDefinition ()

					.name (
						singletonComponentAnnotation.value ())

					.componentClass (
						method.getReturnType ())

					.scope (
						"singleton")

					.factoryClass (
						MethodComponentFactory.class)

					.addReferenceProperty (
						"factoryComponent",
						componentName)

					.addValueProperty (
						"factoryMethodName",
						method.getName ())

					.addValueProperty (
						"initialized",
						false)

					.hide (
						isNotNull (
							namedAnnotation))

				);

			}

			PrototypeComponent prototypeComponentAnnotation =
				method.getAnnotation (
					PrototypeComponent.class);

			if (prototypeComponentAnnotation != null) {

				if (
					stringNotEqualSafe (
						method.getName (),
						prototypeComponentAnnotation.value ())
				) {

					log.warn (
						stringFormat (
							"Factory method name '%s' ",
							method.getName (),
							"does not match component name '%s' ",
							prototypeComponentAnnotation.value ()));

				}

				applicationContext.registerComponentDefinition (
					new ComponentDefinition ()

					.name (
						prototypeComponentAnnotation.value ())

					.componentClass (
						method.getReturnType ())

					.scope (
						"prototype")

					.factoryClass (
						MethodComponentFactory.class)

					.addReferenceProperty (
						"factoryComponent",
						componentName)

					.addValueProperty (
						"factoryMethodName",
						method.getName ())

					.addValueProperty (
						"initialized",
						false)

					.hide (
						isNotNull (
							namedAnnotation))

				);

			}

		}

	}

	void registerObjectHooks (
			@NonNull TaskLogger taskLog,
			@NonNull PluginModelSpec model) {

		String objectHooksComponentName =
			stringFormat (
				"%sHooks",
				model.name ());

		String objectHooksClassName =
			stringFormat (
				"%s.logic.%sHooks",
				model.plugin ().packageName (),
				capitalise (model.name ()));

		Class <?> objectHooksClass;

		try {

			objectHooksClass =
				Class.forName (
					objectHooksClassName);

			applicationContext.registerComponentDefinition (
				new ComponentDefinition ()

				.name (
					objectHooksComponentName)

				.componentClass (
					objectHooksClass)

				.scope (
					"singleton")

			);

		} catch (ClassNotFoundException exception) {

			applicationContext.registerComponentDefinition (
				new ComponentDefinition ()

				.name (
					objectHooksComponentName)

				.componentClass (
					ObjectHooks.DefaultImplementation.class)

				.scope (
					"singleton")

			);

		}

	}

	void registerObjectHelper (
			@NonNull TaskLogger taskLog,
			@NonNull PluginModelSpec model) {

		String objectHelperComponentName =
			stringFormat (
				"%sObjectHelper",
				model.name ());

		String objectHelperImplementationClassName =
			stringFormat (
				"%s.logic.%sObjectHelperImplementation",
				model.plugin ().packageName (),
				capitalise (
					model.name ()));

		Class <?> objectHelperImplementationClass =
			classForNameRequired (
				objectHelperImplementationClassName);

		applicationContext.registerComponentDefinition (
			new ComponentDefinition ()

			.name (
				objectHelperComponentName)

			.componentClass (
				objectHelperImplementationClass)

			.scope (
				"singleton")

		);

	}

	void registerObjectHelperMethodsImplementation (
			@NonNull TaskLogger taskLog,
			@NonNull PluginModelSpec model) {

		String objectHelperMethodsImplementationComponentName =
			stringFormat (
				"%sObjectHelperMethodsImplementation",
				model.name ());

		String objectHelperMethodsImplementationClassName =
			stringFormat (
				"%s.logic.%sObjectHelperMethodsImplementation",
				model.plugin ().packageName (),
				capitalise (
					model.name ()));

		Class <?> objectHelperMethodsImplementationClass;

		try {

			objectHelperMethodsImplementationClass =
				Class.forName (
					objectHelperMethodsImplementationClassName);

		} catch (ClassNotFoundException exception) {

			/*
			log.warn (sf (
				"No object helper implementation for %s.%s.%s",
				model.project ().packageName (),
				model.plugin ().packageName (),
				model.name ()));
			*/

			return;

		}

		applicationContext.registerComponentDefinition (
			new ComponentDefinition ()

			.name (
				objectHelperMethodsImplementationComponentName)

			.componentClass (
				objectHelperMethodsImplementationClass)

			.scope (
				"singleton")

		);

		return;

	}

	void registerDaoHibernate (
			@NonNull TaskLogger taskLog,
			@NonNull PluginModelSpec pluginModelSpec) {

		String daoComponentName =
			stringFormat (
				"%sDao",
				pluginModelSpec.name ());

		String daoClassName =
			stringFormat (
				"%s.model.%sDao",
				pluginModelSpec.plugin ().packageName (),
				capitalise (
					pluginModelSpec.name ()));

		boolean gotDaoClass;

		try {

			Class.forName (
				daoClassName);

			gotDaoClass = true;

		} catch (ClassNotFoundException exception) {

			gotDaoClass = false;

		}

		String daoHibernateClassName =
			stringFormat (
				"%s.hibernate.%sDaoHibernate",
				pluginModelSpec.plugin ().packageName (),
				capitalise (
					pluginModelSpec.name ()));

		Class<?> daoHibernateClass = null;
		boolean gotDaoHibernateClass;

		try {

			daoHibernateClass =
				Class.forName (
					daoHibernateClassName);

			gotDaoHibernateClass = true;

		} catch (ClassNotFoundException exception) {

			gotDaoHibernateClass = false;

		}

		if (
			! gotDaoClass
			&& ! gotDaoHibernateClass
		) {
			return;
		}

		if (
			! gotDaoClass
			|| ! gotDaoHibernateClass
		) {

			taskLog.errorFormat (
				"DAO methods or implementation missing for %s in %s",
				pluginModelSpec.name (),
				pluginModelSpec.plugin ().name ());

			return;

		}

		applicationContext.registerComponentDefinition (
			new ComponentDefinition ()

			.name (
				daoComponentName)

			.componentClass (
				daoHibernateClass)

			.scope (
				"singleton")

		);

		return;

	}

	void registerConsoleHelper (
			@NonNull TaskLogger taskLog,
			@NonNull PluginModelSpec model) {

		String objectHelperComponentName =
			stringFormat (
				"%sObjectHelper",
				model.name ());

		String consoleHelperComponentName =
			stringFormat (
				"%sConsoleHelper",
				model.name ());

		String consoleHelperClassName =
			stringFormat (
				"%s.console.%sConsoleHelper",
				model.plugin ().packageName (),
				capitalise (model.name ()));

		Class<?> consoleHelperClass;

		try {

			consoleHelperClass =
				Class.forName (
					consoleHelperClassName);

		} catch (ClassNotFoundException exception) {

			taskLog.errorFormat (
				"No such class %s",
				consoleHelperClassName);

			return;

		}

		applicationContext.registerComponentDefinition (
			new ComponentDefinition ()

			.name (
				consoleHelperComponentName)

			.componentClass (
				consoleHelperClass)

			.factoryClass (
				ConsoleHelperFactory.class)

			.scope (
				"singleton")

			.addReferenceProperty (
				"objectHelper",
				objectHelperComponentName)

			.addValueProperty (
				"consoleHelperClass",
				consoleHelperClass)

		);

	}

	void registerEnumConsoleHelper (
			@NonNull TaskLogger taskLog,
			@NonNull PluginEnumTypeSpec enumType) {

		String enumClassName =
			stringFormat (
				"%s.model.%s",
				enumType.plugin ().packageName (),
				capitalise (
					enumType.name ()));

		Class <?> enumClass;

		try {

			enumClass =
				Class.forName (
					enumClassName);

		} catch (ClassNotFoundException exception) {

			taskLog.errorFormat (
				"No such class %s",
				enumClassName);

			return;

		}

		String enumConsoleHelperComponentName =
			stringFormat (
				"%sConsoleHelper",
				enumType.name ());

		applicationContext.registerComponentDefinition (
			new ComponentDefinition ()

			.name (
				enumConsoleHelperComponentName)

			.componentClass (
				EnumConsoleHelper.class)

			.factoryClass (
				EnumConsoleHelperFactory.class)

			.scope (
				"singleton")

			.addValueProperty (
				"enumClass",
				enumClass)

		);

	}

	void registerCustomConsoleHelper (
			@NonNull TaskLogger taskLog,
			@NonNull PluginCustomTypeSpec customType) {

		String customClassName =
			stringFormat (
				"%s.model.%s",
				customType.plugin ().packageName (),
				capitalise (
					customType.name ()));

		Optional <Class <?>> customClassOptional =
			classForName (
				customClassName);

		if (
			optionalIsNotPresent (
				customClassOptional)

		) {

			taskLog.errorFormat (
				"No such class %s",
				customClassName);

			return;

		}

		if (
			isNotSubclassOf (
				Enum.class,
				customClassOptional.get ())
		) {
			return;
		}

		Class <?> enumClass =
			customClassOptional.get ();

		String enumConsoleHelperComponentName =
			stringFormat (
				"%sConsoleHelper",
				customType.name ());

		if (
			optionalIsPresent (
				applicationContext.getComponentDefinition (
					enumConsoleHelperComponentName))
		) {
			return;
		}

		applicationContext.registerComponentDefinition (
			new ComponentDefinition ()

			.name (
				enumConsoleHelperComponentName)

			.componentClass (
				EnumConsoleHelper.class)

			.factoryClass (
				EnumConsoleHelperFactory.class)

			.scope (
				"singleton")

			.addValueProperty (
				"enumClass",
				enumClass)

		);

	}

	void registerConfigComponents (
			@NonNull TaskLogger taskLog) {

		for (
			String configName
				: configNames
		) {

			log.info (
				stringFormat (
					"Loading configuration %s",
					configName));

			String configPath =
				stringFormat (
					"conf/%s-config-components.xml",
					configName);

			applicationContext.registerXmlFilename (
				configPath);

		}

	}

	void registerSingletonComponents (
			@NonNull TaskLogger taskLog) {

		for (
			Map.Entry <String,Object> entry
				: singletonComponents.entrySet ()
		) {

			applicationContext.registerUnmanagedSingleton (
				entry.getKey (),
				entry.getValue ());

		}

	}

	@SuppressWarnings ("resource")
	void createApplicationContext () {

		applicationContext =
			new ApplicationContextImplementation ()

			.activityManager (
				activityManager)

			.outputPath (
				outputPath);

	}

	public
	ApplicationContextBuilder registerComponentDefinition (
			@NonNull ComponentDefinition componentDefinition) {

		componentDefinitionsToRegister.add (
			componentDefinition);

		return this;

	}

}
