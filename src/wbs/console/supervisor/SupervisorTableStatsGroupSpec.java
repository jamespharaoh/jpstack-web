package wbs.console.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("stats-group")
@PrototypeComponent ("supervisorTableStatsGroupSpec")
public
class SupervisorTableStatsGroupSpec
	implements ConsoleModuleData {

	@DataAncestor
	SupervisorConfigSpec supervisorConfigSpec;

	@DataAttribute (
		name = "grouper",
		required = true)
	String grouperName;

	@DataAttribute (
		name = "resolver",
		required = true)
	String resolverName;

	@DataAttribute (
		name = "formatter",
		required = true)
	String formatterName;

}
