package wbs.console.module;

import static wbs.framework.utils.etc.Misc.hyphenToCamel;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.BeanFactory;
import wbs.framework.builder.Builder;

@Accessors (fluent = true)
public
class ConsoleModuleFactory
	implements BeanFactory {

	// dependencies

	@Inject
	@Named
	Builder consoleModuleBuilder;

	// prototype dependencies

	@Inject
	Provider<ConsoleModuleImpl> consoleModuleProvider;

	// properties

	@Getter @Setter
	ConsoleModuleSpec consoleSpec;

	// implementation

	@Override
	public
	Object instantiate () {

		ConsoleModuleImpl consoleModule =
			consoleModuleProvider.get ();

		consoleModuleBuilder.descend (
			simpleContainerSpec,
			consoleSpec.builders (),
			consoleModule);

		return consoleModule;

	}

	// simple container

	public
	SimpleConsoleBuilderContainer simpleContainerSpec =
		new SimpleConsoleBuilderContainer () {

		@Override
		public
		String newBeanNamePrefix () {

			return hyphenToCamel (
				consoleSpec.name ());

		}

		@Override
		public
		String existingBeanNamePrefix () {

			return hyphenToCamel (
				consoleSpec.name ());

		}

	};

}
