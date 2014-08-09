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
import wbs.platform.reporting.console.UserStatsGrouper;

@PrototypeComponent ("supervisorUserStatsGrouperBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorUserStatsGrouperBuilder {

	@Inject
	Provider<UserStatsGrouper> userStatsGrouper;

	// builder

	@BuilderParent
	SupervisorPageSpec supervisorPageSpec;

	@BuilderSource
	SupervisorUserStatsGrouperSpec supervisorUserStatsGrouperSpec;

	@BuilderTarget
	SupervisorPageBuilder supervisorPageBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			supervisorUserStatsGrouperSpec.name ();

		supervisorPageBuilder.statsGroupersByName ().put (
			name,
			userStatsGrouper.get ());

	}

}
