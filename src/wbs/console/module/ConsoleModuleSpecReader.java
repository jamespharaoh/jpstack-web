package wbs.console.module;

import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@SingletonComponent ("consoleModuleSpecReader")
public
class ConsoleModuleSpecReader {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	@ConsoleModuleData
	Map <Class <?>, Provider <ConsoleModuleSpec>> consoleModuleSpecProviders;

	@PrototypeDependency
	Provider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// state

	DataFromXml dataFromXml;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			dataFromXml =
				dataFromXmlBuilderProvider.get ()

				.registerBuilders (
					consoleModuleSpecProviders)

				.build ();

		}

	}

	public
	ConsoleModuleSpec readClasspath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String xmlResourceName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"readClasspath");

		) {

			ConsoleModuleSpec consoleSpec =
				(ConsoleModuleSpec)
				dataFromXml.readClasspath (
					taskLogger,
					xmlResourceName);

			return consoleSpec;

		}

	}

}
