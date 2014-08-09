package wbs.platform.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.spec.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("script-ref")
@PrototypeComponent ("scriptRefFormFieldSpec")
@ConsoleModuleData
public
class ScriptRefFormFieldSpec {

	@DataAttribute (
		required = true)
	String path;

}
