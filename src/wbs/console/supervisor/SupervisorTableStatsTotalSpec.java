package wbs.console.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("stats-total")
@PrototypeComponent ("supervisorTableStatsTotalSpec")
@ConsoleModuleData
public
class SupervisorTableStatsTotalSpec {

	@DataAncestor
	SupervisorConfigSpec supervisorConfigSpec;

	@DataAttribute (required = true)
	String label;

	@DataAttribute (value = "resolver", required = true)
	String resolverName;

	@DataAttribute (value = "formatter", required = true)
	String formatterName;

}
