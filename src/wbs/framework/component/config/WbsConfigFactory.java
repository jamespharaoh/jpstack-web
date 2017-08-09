package wbs.framework.component.config;

import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("wbsConfigLoader")
public
class WbsConfigFactory
	implements ComponentFactory <WbsConfig> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// publc interface

	@Override
	public
	WbsConfig makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			String configFilename =
				System.getenv (
					"WBS_CONFIG_XML");

			if (configFilename == null) {

				throw new RuntimeException (
					stringFormat (
						"Please set WBS_CONFIG_XML"));

			}

			WbsConfig wbsConfig =
				readFilename (
					taskLogger,
					configFilename);

			return wbsConfig;

		}

	}

	// private implementation

	private
	WbsConfig readFilename (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String filename) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"readFilename");

		) {

			DataFromXml dataFromXml =
				dataFromXmlBuilderProvider.provide (
					taskLogger)

				.registerBuilderClasses (
					taskLogger,
					WbsConfig.class,
					WbsConfigConsoleServer.class,
					WbsConfigDatabase.class,
					WbsConfigEmail.class,
					WbsConfigProcessApi.class)

				.build (
					taskLogger);

			WbsConfig wbsConfig =
				(WbsConfig)
				dataFromXml.readFilenameRequired (
					taskLogger,
					filename);

			return wbsConfig;

		}

	}

}
