
package wbs.console.forms.number;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("integer-field")
@PrototypeComponent ("integerFormFieldSpec")
public
class IntegerFormFieldSpec
	implements ConsoleModuleData {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "field")
	String fieldName;

	@DataAttribute
	Boolean dynamic;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Long minimum =
		Long.MIN_VALUE;

	@DataAttribute
	Long maximum =
		Long.MAX_VALUE;

	@DataAttribute
	Integer size;

	@DataAttribute
	Boolean blankIfZero;

}
