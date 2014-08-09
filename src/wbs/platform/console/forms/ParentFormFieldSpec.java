package wbs.platform.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.spec.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("parent-field")
@PrototypeComponent ("parentFormFieldSpec")
@ConsoleModuleData
public
class ParentFormFieldSpec {

	// attributes

	@DataAttribute
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	String createPrivDelegate;

	@DataAttribute
	String createPrivCode;

}
