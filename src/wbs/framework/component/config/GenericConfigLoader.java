package wbs.framework.component.config;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("genericConfigLoader")
public
class GenericConfigLoader {

	// singleotn components

	@ClassSingletonDependency
	LogContext logContext;

	// state

	DataFromXml dataFromXml;

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup () {

		dataFromXml =
			new DataFromXmlBuilder ()

			.registerBuilderClasses (
				GenericConfigSpec.class,
				GenericConfigItemSpec.class)

			.build ();

	}

	// public implementation

	public <Config extends AbstractGenericConfig <Config>>
	AbstractGenericConfig <?> load (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String configFilePath) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"load");

		return new AbstractGenericConfig <Config> ()

			.genericConfigSpec (
				loadSpec (
					taskLogger,
					configFilePath));

	}

	public
	GenericConfigSpec loadSpec (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String configFilePath) {

		return (GenericConfigSpec)
			dataFromXml.readFilename (
				parentTaskLogger,
				configFilePath);

	}

}
