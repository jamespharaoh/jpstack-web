package wbs.platform.supervisor;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.platform.console.spec.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("addition-stats-resolver")
@PrototypeComponent ("supervisorAdditionStatsResponderSpec")
@ConsoleModuleData
public
class SupervisorAdditionStatsResolverSpec {

	@DataParent
	SupervisorPageSpec supervisorPage;

	@DataAttribute
	String name;

	@DataChildren (direct = true)
	List<SupervisorAdditionOperandSpec> operandSpecs =
		new ArrayList<SupervisorAdditionOperandSpec> ();

}
