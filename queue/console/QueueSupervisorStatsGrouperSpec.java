package wbs.platform.queue.console;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.platform.console.spec.ConsoleModuleData;
import wbs.platform.supervisor.SupervisorPageSpec;

@Accessors (fluent = true)
@Data
@DataClass ("queue-stats-grouper")
@PrototypeComponent ("queueSupervisorStatsGrouperSpec")
@ConsoleModuleData
public
class QueueSupervisorStatsGrouperSpec {

	@DataParent
	SupervisorPageSpec supervisorPageSpec;

	@DataAttribute (required = true)
	String name;

}
