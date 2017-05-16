package wbs.console.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("operand")
@PrototypeComponent ("supervisorAdditionOperandSpec")
public
class SupervisorAdditionOperandSpec
	implements ConsoleSpec {

	@DataParent
	SupervisorAdditionStatsResolverSpec supervisorAdditionStatsResolverSpec;

	@DataAttribute
	Long coefficient = 1l;

	@DataAttribute
	Long value = 1l;

	@DataAttribute (
		name = "resolver")
	String resolverName;

}
