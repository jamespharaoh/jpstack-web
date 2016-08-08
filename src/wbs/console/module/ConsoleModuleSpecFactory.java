package wbs.console.module;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.application.context.BeanFactory;

@Accessors (fluent = true)
public
class ConsoleModuleSpecFactory
	implements BeanFactory {

	@Inject
	ConsoleModuleSpecReader consoleSpecReader;

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
