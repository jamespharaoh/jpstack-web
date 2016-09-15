package wbs.framework.component.config;

import lombok.NonNull;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;

@SingletonComponent ("genericConfigLoader")
public
class GenericConfigLoader {

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
