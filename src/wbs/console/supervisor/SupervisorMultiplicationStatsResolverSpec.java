package wbs.console.supervisor;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("multiplication-stats-resolver")
@PrototypeComponent ("supervisorMultiplicationStatsResponderSpec")
public
class SupervisorMultiplicationStatsResolverSpec
	implements ConsoleSpec {

	@DataParent
	SupervisorConfigSpec supervisorConfig;

	@DataAttribute
	String name;

	@DataChildren (direct = true)
	List<SupervisorMultiplicationOperandSpec> operandSpecs =
		new ArrayList<SupervisorMultiplicationOperandSpec> ();


}
