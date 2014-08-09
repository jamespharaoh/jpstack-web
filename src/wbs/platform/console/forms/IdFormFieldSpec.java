package wbs.platform.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.spec.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("id-field")
@PrototypeComponent ("idFormFieldSpec")
@ConsoleModuleData
public
class IdFormFieldSpec {

	@DataAttribute
	String label;

	@DataAttribute
	String name;

}
