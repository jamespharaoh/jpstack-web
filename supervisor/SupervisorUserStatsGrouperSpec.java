package wbs.platform.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.platform.console.spec.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("user-stats-grouper")
@PrototypeComponent ("supervisorUserStatsGrouperSpec")
@ConsoleModuleData
public
class SupervisorUserStatsGrouperSpec {

	@DataParent
	SupervisorPageSpec supervisorPageSpec;

	@DataAttribute (required = true)
	String name;

}
