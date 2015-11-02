
package wbs.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("integer-field")
@PrototypeComponent ("integerFormFieldSpec")
@ConsoleModuleData
public
class IntegerFormFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute
	Boolean dynamic;

	@DataAttribute
	String delegate;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute (
		name = "min")
	Long minimum =
		Long.MIN_VALUE;

	@DataAttribute (
		name = "max")
	Long maximum =
		Long.MAX_VALUE;

	@DataAttribute
	Integer size;

	@DataAttribute
	Boolean blankIfZero;

}
