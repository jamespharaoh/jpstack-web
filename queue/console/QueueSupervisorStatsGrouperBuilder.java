package wbs.platform.queue.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.supervisor.SupervisorConfigBuilder;
import wbs.console.supervisor.SupervisorConfigSpec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

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
