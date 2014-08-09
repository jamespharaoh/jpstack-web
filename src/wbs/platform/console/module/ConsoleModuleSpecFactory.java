package wbs.platform.console.module;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.BeanFactory;
import wbs.platform.console.spec.ConsoleSpecReader;

@Accessors (fluent = true)
public
class ConsoleModuleSpecFactory
	implements BeanFactory {

	@Inject
	ConsoleSpecReader consoleSpecReader;

	@Getter @Setter
	String xmlResourceName;

	@Override
	public
	Object instantiate () {

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
