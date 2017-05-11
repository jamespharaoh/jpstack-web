package wbs.console.forms.object;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

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
