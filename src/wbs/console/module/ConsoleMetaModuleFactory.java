package wbs.console.module;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.context.ConsoleContextMetaBuilderContainer;
import wbs.framework.application.context.UninitializedComponentFactory;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderFactory;

@Accessors (fluent = true)
public
class ConsoleMetaModuleFactory
	implements UninitializedComponentFactory {

	// dependencies

	@Inject
	ConsoleMetaModuleBuilder consoleMetaModuleBuilder;

	// prototype dependencies

	@Inject
	Provider<BuilderFactory> builderFactoryProvider;

	@Inject
	Provider<ConsoleMetaModuleImplementation> consoleMetaModuleProvider;

	// properties

	@Getter @Setter
	ConsoleModuleSpec consoleSpec;

	// implementation

	@Override
	public
	Object makeComponent () {

		ConsoleMetaModuleImplementation consoleMetaModule =
			consoleMetaModuleProvider.get ();

		ConsoleContextMetaBuilderContainer contextMetaBuilderContainer =
			new ConsoleContextMetaBuilderContainer ();

		consoleMetaModuleBuilder.descend (
			contextMetaBuilderContainer,
			consoleSpec.builders (),
			consoleMetaModule,
			MissingBuilderBehaviour.ignore);

		return consoleMetaModule;

	}

}
