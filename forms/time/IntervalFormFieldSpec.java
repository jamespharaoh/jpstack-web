package wbs.console.forms.time;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("interval-field")
@PrototypeComponent ("intervalFormFieldSpec")
public
class IntervalFormFieldSpec
	implements ConsoleSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String fieldName;

	@DataAttribute
	String label;

	@DataAttribute
	TimestampFormFieldFormat format;

	@DataAttribute (
		name = "formatter")
	String formatterName;

	@DataAttribute (
		name = "timezone")
	String timezonePath;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

}
