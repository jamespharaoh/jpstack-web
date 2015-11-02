package wbs.console.supervisor;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("integer-stats-formatter")
@PrototypeComponent ("supervisorIntStatsFormtterSpec")
@ConsoleModuleData
public
class SupervisorIntegerStatsFormatterSpec {

	@DataParent
	SupervisorConfigSpec supervisorConfigSpec;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String targetBase;

	@DataAttribute (
		name = "target-group-param")
	String targetGroupParamName;

	@DataAttribute (
		name = "target-step-param")
	String targetStepParamName;

	@DataChildren (
		direct = true,
		childElement = "target-param",
		keyAttribute = "name",
		valueAttribute = "value")
	Map<String,String> targetParams =
		new LinkedHashMap<String,String> ();

}
