package wbs.console.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("stats-total")
@PrototypeComponent ("supervisorTableStatsTotalSpec")
public
class SupervisorTableStatsTotalSpec
	implements ConsoleSpec {

	@DataAncestor
	SupervisorConfigSpec supervisorConfigSpec;

	@DataAttribute (
		required = true)
	String label;

	@DataAttribute (
		name = "resolver",
		required = true)
	String resolverName;

	@DataAttribute (
		name = "formatter",
		required = true)
	String formatterName;

}
