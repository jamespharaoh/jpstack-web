package wbs.console.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

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
