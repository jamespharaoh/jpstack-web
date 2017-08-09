package wbs.console.module;

import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringReplaceAllSimple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.ComponentInterface;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.scaffold.PluginConsoleModuleSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@SingletonComponent ("consoleModuleSpecManager")
@ComponentInterface (ConsoleModuleSpecManager.class)
public
class ConsoleModuleSpecManagerFactory
	implements ComponentFactory <ConsoleModuleSpecManager> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	// prototype dependencies

	@PrototypeDependency
	Map <Class <?>, ComponentProvider <ConsoleSpec>> consoleModuleSpecProviders;

	@PrototypeDependency
	ComponentProvider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// state

	DataFromXml dataFromXml;

	List <ConsoleModuleSpec> specs =
		new ArrayList<> ();

	Map <String, ConsoleModuleSpec> specsByName =
		new HashMap<> ();

	// public implementation

	@Override
	public
	ConsoleModuleSpecManager makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			dataFromXml =
				dataFromXmlBuilderProvider.provide (
					taskLogger)

				.registerBuilders (
					taskLogger,
					consoleModuleSpecProviders)

				.build (
					taskLogger)

			;

			pluginManager.plugins ().forEach (
				pluginSpec ->
					loadSpecsForPlugin (
						taskLogger,
						pluginSpec));

			taskLogger.makeException ();

			Collections.sort (
				specs,
				Ordering.natural ().onResultOf (
					ConsoleModuleSpec::name));

			return new ConsoleModuleSpecManager ()

				.specs (
					ImmutableList.copyOf (
						specs))

				.specsByName (
					ImmutableMap.copyOf (
						specsByName))

			;

		}

	}

	// private implementation

	private
	void loadSpecsForPlugin (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PluginSpec pluginSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"loadSpecsForPlugin");

		) {

			pluginSpec.consoleModules ().forEach (
				pluginConsoleModuleSpec ->
					loadSpec (
						parentTaskLogger,
						pluginSpec,
						pluginConsoleModuleSpec));

		}

	}

	private
	void loadSpec (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PluginSpec pluginSpec,
			@NonNull PluginConsoleModuleSpec pluginConsoleModuleSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"loadSpec");

		) {

			String consoleModuleResourseName =
				stringFormat (
					"/%s/console/%s-console.xml",
					stringReplaceAllSimple (
						".",
						"/",
						pluginSpec.packageName ()),
					pluginConsoleModuleSpec.name ());

			Optional <ConsoleModuleSpec> consoleModuleSpecOptional =
				loadFromResource (
					taskLogger,
					consoleModuleResourseName);

			if (
				optionalIsNotPresent (
					consoleModuleSpecOptional)
			) {
				return;
			}

			ConsoleModuleSpec consoleModuleSpec =
				optionalGetRequired (
					consoleModuleSpecOptional);

			if (
				stringNotEqualSafe (
					pluginConsoleModuleSpec.name (),
					consoleModuleSpec.name ())
			) {

				taskLogger.errorFormat (
					"Console module %s ",
					consoleModuleResourseName,
					"has name %s ",
					consoleModuleSpec.name (),
					"but expected %s",
					pluginConsoleModuleSpec.name ());

				return;

			}

			if (
				contains (
					specsByName,
					consoleModuleSpec.name ())
			) {

				taskLogger.errorFormat (
					"Duplicated console module name: %s",
					consoleModuleSpec.name ());

				return;

			}

			specs.add (
				consoleModuleSpec);

			specsByName.put (
				consoleModuleSpec.name (),
				consoleModuleSpec);

		}

	}

	private
	Optional <ConsoleModuleSpec> loadFromResource (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String xmlResourceName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"readClasspath");

		) {

			return genericCastUnchecked (
				dataFromXml.readClasspath (
					taskLogger,
					xmlResourceName));

		}

	}

}
