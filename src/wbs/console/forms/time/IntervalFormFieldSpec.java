package wbs.console.forms.time;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("interval-field")
@PrototypeComponent ("intervalFormFieldSpec")
public
class IntervalFormFieldSpec
	implements ConsoleModuleData {

	@DataAttribute
	String label;

	@DataAttribute
	String name;

	@DataAttribute
	String fieldName;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

}
