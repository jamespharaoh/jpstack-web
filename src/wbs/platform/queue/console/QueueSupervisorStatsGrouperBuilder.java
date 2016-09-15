package wbs.platform.queue.console;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.supervisor.SupervisorConfigBuilder;
import wbs.console.supervisor.SupervisorConfigSpec;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("queueSupervisorStatsGrouperBuilder")
@ConsoleModuleBuilderHandler
public
class QueueSupervisorStatsGrouperBuilder {

	// singleton dependencies

	@SingletonDependency
	QueueStatsGrouper queueStatsGrouper;

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
			queueStatsGrouper);

	}

}
