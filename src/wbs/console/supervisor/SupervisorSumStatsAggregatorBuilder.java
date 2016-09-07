package wbs.console.supervisor;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.reporting.SumStatsAggregator;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@PrototypeComponent ("supervisorSumStatsAggregatorBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorSumStatsAggregatorBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <SumStatsAggregator> sumStatsAggregatorProvider;

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
			sumStatsAggregatorProvider.get ());

	}

}
