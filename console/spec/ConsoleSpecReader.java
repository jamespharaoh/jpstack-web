package wbs.platform.console.spec;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;

import com.google.common.collect.ImmutableList;

@Accessors (fluent = true)
@SingletonComponent ("consoleSpecReader")
public
class ConsoleSpecReader {

	@Inject
	@ConsoleModuleData
	Map<Class<?>,Provider<ConsoleSpec>> consoleModuleSpecProviders;

	DataFromXml dataFromXml;

	@PostConstruct
	public
	void init () {

		dataFromXml =
			new DataFromXml ();

		for (Map.Entry<Class<?>,Provider<ConsoleSpec>> entry
				: consoleModuleSpecProviders.entrySet ()) {

			dataFromXml.registerBuilder (
				entry.getKey (),
				entry.getValue ());

		}

	}

	public
	ConsoleSpec readClasspath (
			@NonNull String xmlResourceName) {

		ConsoleSpec consoleSpec =
			(ConsoleSpec)
			dataFromXml.readClasspath (
				ImmutableList.of (),
				xmlResourceName);

		return consoleSpec;

	}

}
