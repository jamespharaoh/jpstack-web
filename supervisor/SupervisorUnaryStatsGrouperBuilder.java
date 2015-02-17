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
import wbs.platform.reporting.console.UnaryStatsGrouper;

@PrototypeComponent ("supervisorUnaryStatsGrouperBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorUnaryStatsGrouperBuilder {

	@Inject
	Provider<UnaryStatsGrouper> unaryStatsGrouper;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorUnaryStatsGrouperSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		String label =
			spec.label ();

		supervisorConfigBuilder.statsGroupersByName ().put (
			name,
			unaryStatsGrouper.get ()
				.label (label));

	}

}
