package wbs.console.module;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;

@Accessors (fluent = true)
@SingletonComponent ("consoleModuleSpecReader")
public
class ConsoleModuleSpecReader {

	@Inject
	@ConsoleModuleData
	Map <Class <?>, Provider <ConsoleModuleSpec>> consoleModuleSpecProviders;

	DataFromXml dataFromXml;

	@PostConstruct
	public
	void init () {

		DataFromXmlBuilder builder =
			new DataFromXmlBuilder ();

		for (
			Map.Entry <Class <?>, Provider <ConsoleModuleSpec>> entry
				: consoleModuleSpecProviders.entrySet ()
		) {

			builder.registerBuilder (
				entry.getKey (),
				entry.getValue ());

		}

		dataFromXml =
			builder.build ();

	}

	public
	ConsoleModuleSpec readClasspath (
			@NonNull String xmlResourceName) {

		ConsoleModuleSpec consoleSpec =
			(ConsoleModuleSpec)
			dataFromXml.readClasspath (
				xmlResourceName);

		return consoleSpec;

	}

}
