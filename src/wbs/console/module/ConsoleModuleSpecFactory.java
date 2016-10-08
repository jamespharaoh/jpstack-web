package wbs.console.module;

import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ConsoleModuleSpecFactory
	implements ComponentFactory {

	// dependencies

	@SingletonDependency
	ConsoleModuleSpecReader consoleSpecReader;

	// properties

	@Getter @Setter
	String xmlResourceName;

	// implementation

	@Override
	public
	Object makeComponent (
			@NonNull TaskLogger taskLogger) {

		try {

			return consoleSpecReader.readClasspath (
				xmlResourceName);

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error reading console module spec %s",
					xmlResourceName),
				exception);

		}

	}

}
