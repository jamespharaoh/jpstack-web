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
	SupervisorPageSpec supervisorPageSpec;

	@BuilderSource
	SupervisorUnaryStatsGrouperSpec supervisorUnaryStatsGrouperSpec;

	@BuilderTarget
	SupervisorPageBuilder supervisorPageBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			supervisorUnaryStatsGrouperSpec.name ();

		String label =
			supervisorUnaryStatsGrouperSpec.label ();

		supervisorPageBuilder.statsGroupersByName ().put (
			name,
			unaryStatsGrouper.get ()
				.label (label));

	}

}
