package wbs.console.forms.number;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("floating-point-field")
@PrototypeComponent ("floatingPointFormFieldSpec")
public
class FloatingPointFormFieldSpec
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

	@DataAttribute (
		name = "minimum")
	Double minimum =
		Double.NEGATIVE_INFINITY;

	@DataAttribute (
		name = "maximum")
	Double maximum =
		Double.POSITIVE_INFINITY;

	@DataAttribute
	Integer size;

}
