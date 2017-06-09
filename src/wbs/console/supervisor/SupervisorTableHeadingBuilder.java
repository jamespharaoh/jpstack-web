package wbs.console.supervisor;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.module.ConsoleModuleBuilderComponent;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("supervisorTableHeadingBuilder")
public
class SupervisorTableHeadingBuilder
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <SupervisorTableHeadingPart> supervisorTableHeadingPartProvider;

	// builder

	@BuilderParent
	SupervisorTablePartSpec supervisorTablePartSpec;

	@BuilderSource
	SupervisorTableHeadingSpec supervisorTableHeadingSpec;

	@BuilderTarget
	SupervisorTablePartBuilder supervisorTablePartBuilder;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			supervisorTablePartBuilder.pagePartFactories ().add (
				(transaction, statsPeriod, statsData) ->
					supervisorTableHeadingPartProvider.get ()

				.supervisorTableHeadingSpec (
					supervisorTableHeadingSpec)

				.statsPeriod (
					statsPeriod)

			);

		}

	}

}
