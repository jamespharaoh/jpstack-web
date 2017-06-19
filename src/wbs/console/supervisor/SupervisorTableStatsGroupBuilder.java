package wbs.console.supervisor;

import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.reporting.StatsFormatter;
import wbs.console.reporting.StatsGrouper;
import wbs.console.reporting.StatsResolver;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("supervisorTableStatsGroupBuilder")
public
class SupervisorTableStatsGroupBuilder
	implements ConsoleModuleBuilderComponent {

	// singleton depdedencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <SupervisorTableStatsGroupPart>
		supervisorTableStatsGroupPartProvider;

	// builder

	@BuilderParent
	SupervisorTablePartSpec container;

	@BuilderSource
	SupervisorTableStatsGroupSpec spec;

	@BuilderTarget
	SupervisorTablePartBuilder supervisorTablePartBuilder;

	// state

	StatsGrouper statsGrouper;
	StatsResolver statsResolver;
	StatsFormatter statsFormatter;

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

			SupervisorConfigBuilder supervisorConfigBuilder =
				supervisorTablePartBuilder.supervisorConfigBuilder;

			statsGrouper =
				supervisorConfigBuilder.statsGroupersByName ().get (
					spec.grouperName ());

			if (statsGrouper == null) {

				throw new RuntimeException (
					stringFormat (
						"Stats grouper %s does not exist",
						spec.grouperName ()));

			}

			statsResolver =
				supervisorConfigBuilder.statsResolversByName ().get (
					spec.resolverName ());

			if (statsResolver == null) {

				throw new RuntimeException (
					stringFormat (
						"Stats resolver %s does not exist",
						spec.resolverName ()));

			}

			statsFormatter =
				supervisorConfigBuilder.statsFormattersByName ().get (
					spec.formatterName ());

			if (statsFormatter == null) {

				throw new RuntimeException (
					stringFormat (
						"Stats formatter %s does not exist",
						spec.formatterName ()));

			}

			supervisorTablePartBuilder.pagePartFactories ().add (
				(transaction, statsPeriod, statsData) ->
					supervisorTableStatsGroupPartProvider.provide (
						transaction)

				.statsGrouper (
					statsGrouper)

				.statsResolver (
					statsResolver)

				.statsFormatter (
					statsFormatter)

				.statsPeriod (
					statsPeriod)

				.statsDataSetsByName (
					statsData)

			);

		}

	}

}
