package wbs.platform.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("condition")
@PrototypeComponent ("supervisorConditionSpec")
@ConsoleModuleData
public
class SupervisorConditionSpec {

	@DataAttribute (required = true)
	String name;

	@DataAttribute (required = true)
	String stuffKey;

}
