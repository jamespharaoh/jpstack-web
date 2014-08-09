package wbs.platform.supervisor;

import java.util.LinkedHashMap;
import java.util.Map;

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
@DataClass ("integer-stats-formatter")
@PrototypeComponent ("supervisorIntStatsFormtterSpec")
@ConsoleModuleData
public
class SupervisorIntegerStatsFormatterSpec {

	@DataParent
	SupervisorPageSpec supervisorPageSpec;

	@DataAttribute (required = true)
	String name;

	@DataAttribute
	String targetBase;

	@DataAttribute ("target-group-param")
	String targetGroupParamName;

	@DataAttribute ("target-step-param")
	String targetStepParamName;

	@DataChildren (
		direct = true,
		childElement = "target-param",
		keyAttribute = "name",
		valueAttribute = "value")
	Map<String,String> targetParams =
		new LinkedHashMap<String,String> ();

}
