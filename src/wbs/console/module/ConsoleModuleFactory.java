package wbs.console.module;

import static wbs.utils.string.StringUtils.hyphenToSpaces;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ConsoleModuleFactory <ObjectType extends Record <ObjectType>>
	implements ComponentFactory <ConsoleModule> {

	// singleton dependencies

	@SingletonDependency
	ConsoleModuleBuilder consoleModuleBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleModuleImplementation> consoleModuleProvider;

	// properties

	@Getter @Setter
	ConsoleModuleSpec consoleModuleSpec;

	// implementation

	@Override
	public
	ConsoleModule makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			ConsoleModuleImplementation consoleModule =
				consoleModuleProvider.get ()

				.name (
					consoleModuleSpec.name ())

			;

			SimpleConsoleBuilderContainer container =
				new SimpleConsoleBuilderContainerImplementation ()

				.consoleModule (
					consoleModuleSpec)

				.newBeanNamePrefix (
					hyphenToSpaces (
						consoleModuleSpec.name ()))

				.existingBeanNamePrefix (
					hyphenToSpaces (
						consoleModuleSpec.name ()))

			;

			consoleModuleBuilder.descend (
				taskLogger,
				container,
				consoleModuleSpec.builders (),
				consoleModule,
				MissingBuilderBehaviour.error);

			return consoleModule;

		}

	}

}
