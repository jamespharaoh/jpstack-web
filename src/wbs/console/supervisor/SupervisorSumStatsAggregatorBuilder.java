package wbs.console.supervisor;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.reporting.SumStatsAggregator;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("supervisorSumStatsAggregatorBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorSumStatsAggregatorBuilder {

	// singleton dependencies

	@SingletonDependency
	SumStatsAggregator sumStatsAggregator;

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
			sumStatsAggregator);

	}

}
