package wbs.platform.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("separator")
@PrototypeComponent ("supervisorTableSeparatorSpec")
@ConsoleModuleData
public
class SupervisorTableSeparatorSpec {

	@DataParent
	SupervisorTablePartSpec supervisorTablePartSpec;

}
