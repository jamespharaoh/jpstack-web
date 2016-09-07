package wbs.console.supervisor;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.reporting.SimpleStatsResolver;
import wbs.console.reporting.StatsAggregator;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("supervisorSimpleStatsResolverBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorSimpleStatsResolverBuilder {

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

	@BuildMethod
	public
	void build (
			Builder builder) {

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
