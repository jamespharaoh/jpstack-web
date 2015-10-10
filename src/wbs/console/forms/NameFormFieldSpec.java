package wbs.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("name-field")
@PrototypeComponent ("nameFormFieldSpec")
@ConsoleModuleData
public
class NameFormFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String delegate;

	@DataAttribute
	String label;

	@DataAttribute
	Integer size;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Boolean nullable;

}
