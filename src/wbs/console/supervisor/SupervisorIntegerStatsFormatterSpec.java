package wbs.console.supervisor;

import java.util.LinkedHashMap;
import java.util.Map;

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
@DataClass ("integer-stats-formatter")
@PrototypeComponent ("supervisorIntStatsFormtterSpec")
public
class SupervisorIntegerStatsFormatterSpec
	implements ConsoleSpec {

	@DataParent
	SupervisorConfigSpec supervisorConfigSpec;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String targetBase;

	@DataChildren (
		direct = true,
		childElement = "target-param",
		keyAttribute = "name",
		valueAttribute = "value")
	Map<String,String> targetParams =
		new LinkedHashMap<String,String> ();

}
