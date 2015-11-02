package wbs.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("floating-point-field")
@PrototypeComponent ("floatingPointFormFieldSpec")
@ConsoleModuleData
public
class FloatingPointFormFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String delegate;

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
