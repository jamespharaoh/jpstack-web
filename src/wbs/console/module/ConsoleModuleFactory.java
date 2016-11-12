package wbs.console.module;

import static wbs.utils.string.StringUtils.hyphenToCamel;

import javax.inject.Named;
import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ConsoleModuleFactory
	implements ComponentFactory {

	// singleton dependencies

	@SingletonDependency
	@Named
	Builder consoleModuleBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleModuleImplementation> consoleModuleProvider;

	// properties

	@Getter @Setter
	ConsoleModuleSpec consoleSpec;

	// implementation

	@Override
	public
	Object makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"makeComponent");

		ConsoleModuleImplementation consoleModule =
			consoleModuleProvider.get ()

			.name (
				consoleSpec.name ());

		SimpleConsoleBuilderContainer container =
			new SimpleConsoleBuilderContainerImplementation ()

			.taskLogger (
				taskLogger)

			.newBeanNamePrefix (
				hyphenToCamel (
					consoleSpec.name ()))

			.existingBeanNamePrefix (
				hyphenToCamel (
					consoleSpec.name ()));

		consoleModuleBuilder.descend (
			taskLogger,
			container,
			consoleSpec.builders (),
			consoleModule,
			MissingBuilderBehaviour.error);

		return consoleModule;

	}

}
