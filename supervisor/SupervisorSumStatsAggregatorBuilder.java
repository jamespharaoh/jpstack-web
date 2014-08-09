package wbs.platform.supervisor;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.reporting.console.SumStatsAggregator;

@PrototypeComponent ("supervisorSumStatsAggregatorBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorSumStatsAggregatorBuilder {

	@Inject
	Provider<SumStatsAggregator> sumStatsAggregator;

	// builder

	@BuilderParent
	SupervisorPageSpec supervisorPageSpec;

	@BuilderSource
	SupervisorSumStatsAggregatorSpec supervisorSumStatsAggregatorSpec;

	@BuilderTarget
	SupervisorPageBuilder supervisorPageBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builer) {

		String name =
			supervisorSumStatsAggregatorSpec.name ();

		supervisorPageBuilder.statsAggregatorsByName.put (
			name,
			sumStatsAggregator.get ());

	}

}
