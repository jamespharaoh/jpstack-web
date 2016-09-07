package wbs.console.module;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;

@Accessors (fluent = true)
@SingletonComponent ("consoleModuleSpecReader")
public
class ConsoleModuleSpecReader {

	// prototype dependencies

	@PrototypeDependency
	@ConsoleModuleData
	Map <Class <?>, Provider <ConsoleModuleSpec>> consoleModuleSpecProviders;

	// state

	DataFromXml dataFromXml;

	// life cycle

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
