package wbs.console.forms.time;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("date-field")
@PrototypeComponent ("dateFormFieldSpec")
public
class DateFormFieldSpec
	implements ConsoleModuleData {

	@DataAttribute
	String name;

	@DataAttribute (
		name = "field")
	String fieldName;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

}
