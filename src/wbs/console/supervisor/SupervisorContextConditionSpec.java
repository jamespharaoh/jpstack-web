package wbs.console.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("context-condition")
@PrototypeComponent ("supervisorContextConditionSpec")
public
class SupervisorContextConditionSpec
	implements SupervisorConditionSpec {

	@DataParent
	SupervisorConfigSpec supervisorConfig;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String stuffKey;

}
