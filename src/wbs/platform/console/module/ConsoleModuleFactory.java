package wbs.platform.console.module;

import static wbs.framework.utils.etc.Misc.hyphenToCamel;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.BeanFactory;
import wbs.framework.builder.Builder;
import wbs.platform.console.spec.ConsoleSpec;
import wbs.platform.console.spec.ConsoleSimpleBuilderContainer;

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
	ConsoleSpec consoleSpec;

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
	ConsoleSimpleBuilderContainer simpleContainerSpec =
		new ConsoleSimpleBuilderContainer () {

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
