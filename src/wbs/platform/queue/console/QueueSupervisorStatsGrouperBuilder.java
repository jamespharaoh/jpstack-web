package wbs.platform.queue.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.supervisor.SupervisorConfigBuilder;
import wbs.platform.supervisor.SupervisorConfigSpec;

@PrototypeComponent ("queueSupervisorStatsGrouperBuilder")
@ConsoleModuleBuilderHandler
public
class QueueSupervisorStatsGrouperBuilder {

	// prototype dependencies

	@Inject
	Provider<QueueStatsGrouper> queueStatsGrouper;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	QueueSupervisorStatsGrouperSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		supervisorConfigBuilder.statsGroupersByName ().put (
			name,
			queueStatsGrouper.get ());

	}

}
