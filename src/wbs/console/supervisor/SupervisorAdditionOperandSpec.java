package wbs.console.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("operand")
@PrototypeComponent ("supervisorAdditionOperandSpec")
@ConsoleModuleData
public
class SupervisorAdditionOperandSpec {

	@DataParent
	SupervisorAdditionStatsResolverSpec supervisorAdditionStatsResolverSpec;

	@DataAttribute
	Integer coefficient = 1;

	@DataAttribute
	Integer value = 1;

	@DataAttribute ("resolver")
	String resolverName;

}
