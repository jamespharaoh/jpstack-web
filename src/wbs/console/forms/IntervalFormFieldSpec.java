package wbs.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("interval-field")
@PrototypeComponent ("intervalFormFieldSpec")
@ConsoleModuleData
public
class IntervalFormFieldSpec {

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
