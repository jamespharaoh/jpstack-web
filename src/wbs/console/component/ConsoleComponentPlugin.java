package wbs.console.component;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.classForName;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isNotSubclassOf;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.helper.enums.EnumConsoleHelper;
import wbs.console.helper.enums.EnumConsoleHelperFactory;
import wbs.console.module.ConsoleModuleSpecManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.component.scaffold.PluginCustomTypeSpec;
import wbs.framework.component.scaffold.PluginEnumTypeSpec;
import wbs.framework.component.scaffold.PluginModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.ComponentPlugin;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleComponentPlugin")
public
class ConsoleComponentPlugin
	implements ComponentPlugin {

	// singleton dependencies

	@SingletonDependency
	ConsoleModuleSpecManager consoleModuleSpecManager;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	void registerComponents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginSpec plugin) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerComponents");

		) {

			plugin.models ().models ().forEach (
				pluginModelSpec ->
					registerConsoleHelper (
						taskLogger,
						componentRegistry,
						pluginModelSpec));

			plugin.models ().enumTypes ().forEach (
				pluginEnumTypeSpec ->
					registerEnumConsoleHelper (
						taskLogger,
						componentRegistry,
						pluginEnumTypeSpec));

			plugin.models ().customTypes ().forEach (
				pluginCustomTypeSpec ->
					registerCustomConsoleHelper (
						taskLogger,
						componentRegistry,
						pluginCustomTypeSpec));

		}

	}

	// private implementation

	private
	void registerConsoleHelper (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginModelSpec model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerConsoleHelper");

		) {

			String consoleHelperComponentName =
				stringFormat (
					"%sConsoleHelper",
					model.name ());

			// console helper

			String consoleHelperClassName =
				stringFormat (
					"%s.console.%sConsoleHelper",
					model.plugin ().packageName (),
					capitalise (
						model.name ()));

			Optional <Class <?>> consoleHelperClassOptional =
				classForName (
					consoleHelperClassName);

			if (
				optionalIsNotPresent (
					consoleHelperClassOptional)
			) {

				taskLogger.errorFormat (
					"No such class %s",
					consoleHelperClassName);

				return;

			}

			// console helper implemenation

			String consoleHelperImplementationClassName =
				stringFormat (
					"%s.console.%sConsoleHelperImplementation",
					model.plugin ().packageName (),
					capitalise (
						model.name ()));

			Optional <Class <?>> consoleHelperImplementationClassOptional =
				classForName (
					consoleHelperImplementationClassName);

			if (
				optionalIsNotPresent (
					consoleHelperImplementationClassOptional)
			) {

				taskLogger.errorFormat (
					"No such class %s",
					consoleHelperImplementationClassName);

				return;

			}

			Class <?> consoleHelperImplementationClass =
				consoleHelperImplementationClassOptional.get ();

			// component definition

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					consoleHelperComponentName)

				.componentClass (
					consoleHelperImplementationClass)

				.scope (
					"singleton")

			);

		}

	}

	private
	void registerEnumConsoleHelper (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginEnumTypeSpec enumType) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerEnumConsoleHelper");

		) {

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

				taskLogger.errorFormat (
					"No such class %s",
					enumClassName);

				return;

			}

			String enumConsoleHelperComponentName =
				stringFormat (
					"%sConsoleHelper",
					enumType.name ());

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					enumConsoleHelperComponentName)

				.componentClass (
					EnumConsoleHelper.class)

				.factoryClass (
					genericCastUnchecked (
						EnumConsoleHelperFactory.class))

				.scope (
					"singleton")

				.addValueProperty (
					"enumClass",
					enumClass)

			);

		}

	}

	private
	void registerCustomConsoleHelper (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginCustomTypeSpec customType) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerCustomConsoleHelper");

		) {

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

				taskLogger.errorFormat (
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
				componentRegistry.hasName (
					enumConsoleHelperComponentName)
			) {
				return;
			}

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					enumConsoleHelperComponentName)

				.componentClass (
					EnumConsoleHelper.class)

				.factoryClass (
					genericCastUnchecked (
						EnumConsoleHelperFactory.class))

				.scope (
					"singleton")

				.addValueProperty (
					"enumClass",
					enumClass)

			);

		}

	}

}
