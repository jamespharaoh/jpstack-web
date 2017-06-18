package wbs.console.module;

import static wbs.utils.etc.Misc.doNothing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleFormsSpec;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.helper.provider.ConsoleHelperProviderSpec;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleModuleBuilder")
public
class ConsoleModuleBuilder
	implements Builder <TaskLogger> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@StrongPrototypeDependency
	Provider <BuilderFactory <?, TaskLogger>> builderFactoryProvider;

	@StrongPrototypeDependency
	Map <Class <?>, ComponentProvider <ConsoleModuleBuilderComponent>> builders;

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

		List <Object> firstPass =
			new ArrayList<> ();

		List <Object> secondPass =
			new ArrayList<> ();

		for (
			Object childObject
				: childObjects
		) {

			if (
				childObject instanceof ConsoleFormsSpec
				|| childObject instanceof ConsoleHelperProviderSpec
			) {

				doNothing ();

			} else {

				secondPass.add (
					childObject);

			}

		}

		builder.descend (
			parentTaskLogger,
			parentObject,
			firstPass,
			targetObject,
			missingBuilderBehaviour);

		builder.descend (
			parentTaskLogger,
			parentObject,
			secondPass,
			targetObject,
			missingBuilderBehaviour);

	}

}
