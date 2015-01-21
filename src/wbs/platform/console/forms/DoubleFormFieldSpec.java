package wbs.platform.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("double-field")
@PrototypeComponent ("doubleFormFieldSpec")
@ConsoleModuleData
public
class DoubleFormFieldSpec {

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

	@DataAttribute ("minimum")
	Double minimum =
		Double.NEGATIVE_INFINITY;

	@DataAttribute ("maximum")
	Double maximum =
		Double.POSITIVE_INFINITY;

	@DataAttribute
	Integer size;

}
