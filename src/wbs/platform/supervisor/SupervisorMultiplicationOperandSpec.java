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
@DataClass ("operand")
@PrototypeComponent ("supervisorMultiplicationOperandSpec")
@ConsoleModuleData
public
class SupervisorMultiplicationOperandSpec {

	@DataParent
	SupervisorMultiplicationStatsResolverSpec
		supervisorMultiplicationStatsResolverSpec;

	@DataAttribute
	Integer power = 1;

	@DataAttribute
	Integer value = 1;

	@DataAttribute ("resolver")
	String resolverName;

}
