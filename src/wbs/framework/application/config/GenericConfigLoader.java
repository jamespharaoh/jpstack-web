package wbs.framework.application.config;

import javax.annotation.PostConstruct;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;

@SingletonComponent ("genericConfigLoader")
public
class GenericConfigLoader {

	// state

	DataFromXml dataFromXml;

	// lifecycle

	@PostConstruct
	public
	void setup () {

		dataFromXml =
			new DataFromXml ()

			.registerBuilderClasses (
				GenericConfigSpec.class,
				GenericConfigItemSpec.class);

	}

	// public implementation

	public
	AbstractGenericConfig load (
			@NonNull String configFilePath) {

		return new AbstractGenericConfig ()

			.genericConfigSpec (
				loadSpec (
					configFilePath));

	}

	public
	GenericConfigSpec loadSpec (
			@NonNull String configFilePath) {

		return (GenericConfigSpec)
			dataFromXml.readFilename (
				configFilePath);

	}

}
