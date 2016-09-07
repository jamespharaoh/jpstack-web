package wbs.console.supervisor;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("integer-in-condition")
@PrototypeComponent ("supervisorIntegerInConditionSpec")
@ConsoleModuleData
public
class SupervisorIntegerInConditionSpec {

	@DataParent
	SupervisorConfigSpec supervisorConfig;

	@DataAttribute (
		required = true)
	String name;

	@DataChildren (
		direct = true,
		childElement = "item",
		valueAttribute = "value")
	List <Long> values;

}
