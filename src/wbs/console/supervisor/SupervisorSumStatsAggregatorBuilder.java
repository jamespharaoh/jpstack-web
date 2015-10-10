package wbs.console.supervisor;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.reporting.SumStatsAggregator;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("supervisorSumStatsAggregatorBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorSumStatsAggregatorBuilder {

	@Inject
	Provider<SumStatsAggregator> sumStatsAggregator;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorSumStatsAggregatorSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builer) {

		String name =
			spec.name ();

		supervisorConfigBuilder.statsAggregatorsByName.put (
			name,
			sumStatsAggregator.get ());

	}

}
