package wbs.platform.console.module;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.BeanFactory;
import wbs.framework.builder.BuilderFactory;
import wbs.platform.console.context.ConsoleContextMetaBuilderContainer;

@Accessors (fluent = true)
public
class ConsoleMetaModuleFactory
	implements BeanFactory {

	// dependencies

	@Inject
	ConsoleMetaModuleBuilder consoleMetaModuleBuilder;

	// prototype dependencies

	@Inject
	Provider<BuilderFactory> builderFactoryProvider;

	@Inject
	Provider<ConsoleMetaModuleImpl> consoleMetaModuleProvider;

	// properties

	@Getter @Setter
	ConsoleModuleSpec consoleSpec;

	// implementation

	@Override
	public
	Object instantiate () {

		ConsoleMetaModuleImpl consoleMetaModule =
			consoleMetaModuleProvider.get ();

		ConsoleContextMetaBuilderContainer contextMetaBuilderContainer =
			new ConsoleContextMetaBuilderContainer ();

		consoleMetaModuleBuilder.descend (
			contextMetaBuilderContainer,
			consoleSpec.builders (),
			consoleMetaModule);

		return consoleMetaModule;

	}

}
