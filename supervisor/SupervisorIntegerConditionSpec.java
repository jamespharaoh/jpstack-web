package wbs.platform.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("integer-condition")
@PrototypeComponent ("supervisorIntegerConditionSpec")
@ConsoleModuleData
public
class SupervisorIntegerConditionSpec {

	@DataParent
	SupervisorConfigSpec supervisorConfig;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String value;

}
