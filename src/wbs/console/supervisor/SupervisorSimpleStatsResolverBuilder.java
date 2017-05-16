package wbs.console.supervisor;

import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.reporting.SimpleStatsResolver;
import wbs.console.reporting.StatsAggregator;

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

@PrototypeComponent ("supervisorSimpleStatsResolverBuilder")
public
class SupervisorSimpleStatsResolverBuilder
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <SimpleStatsResolver> simpleStatsResolverProvider;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorSimpleStatsResolverSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

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

			String name =
				spec.name ();

			String aggregatorName =
				spec.aggregatorName ();

			String indexName =
				spec.indexName ();

			String valueName =
				spec.valueName ();

			String dataSetName =
				spec.dataSetName();

			/*
			StatsDataSet dataSet =
				spec.statsDataSetsByName ().get (
					dataSetName);

			if (dataSetName != null && dataSet == null) {

				throw new RuntimeException (sf (
					"Stats data set %s does not exist",
					dataSetName));

			}
			*/

			StatsAggregator statsAggregator =
				supervisorConfigBuilder.statsAggregatorsByName.get (
					aggregatorName);

			if (statsAggregator == null) {

				throw new RuntimeException (
					stringFormat (
						"Stats aggregator %s does not exist",
						aggregatorName));

			}

			supervisorConfigBuilder.statsResolversByName.put (

				name,

				simpleStatsResolverProvider.get ()

					.indexName (
						indexName)

					.valueName (
						valueName)

					.dataSetName (
						dataSetName)

					.aggregator (
						statsAggregator));

		}

	}

}
