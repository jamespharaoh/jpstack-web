
package wbs.platform.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.spec.ConsoleModuleData;

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
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute ("min")
	Integer minimum =
		Integer.MIN_VALUE;

	@DataAttribute ("max")
	Integer maximum =
		Integer.MAX_VALUE;

	@DataAttribute
	Integer size;

}
