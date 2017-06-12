package wbs.console.component;

import static wbs.utils.etc.TypeUtils.dynamicCastRequired;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.module.ConsoleModuleSpec;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleComponentBuilder")
public
class ConsoleComponentBuilder
	implements Builder <TaskLogger> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	Provider <BuilderFactory <?, TaskLogger>> builderFactoryProvider;

	@StrongPrototypeDependency
	Map <Class <?>, Provider <ConsoleComponentBuilderComponent>> builders;

	// state

	Builder <TaskLogger> builder;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			builder =
				builderFactoryProvider.get ()

				.contextClass (
					TaskLogger.class)

				.addBuilders (
					taskLogger,
					builders)

				.create (
					taskLogger)

			;

		}

	}

	// builder

	@Override
	public
	void descend (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object parentObject,
			@NonNull List <?> childObjects,
			@NonNull Object targetObject,
			@NonNull MissingBuilderBehaviour missingBuilderBehaviour) {

		ConsoleComponentBuilderContext context =
			new ConsoleComponentBuilderContextImplementation ()

			.consoleModule (
				dynamicCastRequired (
					ConsoleModuleSpec.class,
					parentObject))

			.pathPrefix (
				"")

			.newComponentNamePrefix (
				"")

			.existingComponentNamePrefix (
				"")

			.friendlyName (
				"")

		;

		builder.descend (
			parentTaskLogger,
			context,
			childObjects,
			targetObject,
			missingBuilderBehaviour);

	}

}
