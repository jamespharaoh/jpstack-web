package wbs.platform.queue.console;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.supervisor.SupervisorConfigSpec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("queue-stats-grouper")
@PrototypeComponent ("queueSupervisorStatsGrouperSpec")
@ConsoleModuleData
public
class QueueSupervisorStatsGrouperSpec {

	@DataParent
	SupervisorConfigSpec supervisorConfigSpec;

	@DataAttribute (required = true)
	String name;

}
