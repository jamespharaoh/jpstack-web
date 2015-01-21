package wbs.platform.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("stats-group")
@PrototypeComponent ("supervisorTableStatsGroupSpec")
@ConsoleModuleData
public
class SupervisorTableStatsGroupSpec {

	@DataAncestor
	SupervisorPageSpec supervisorPageSpec;

	@DataAttribute (
		value = "grouper",
		required = true)
	String grouperName;

	@DataAttribute (
		value = "resolver",
		required = true)
	String resolverName;

	@DataAttribute (
		value = "formatter",
		required = true)
	String formatterName;

}
