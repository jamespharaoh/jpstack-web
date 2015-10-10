package wbs.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("timestamp-to-field")
@PrototypeComponent ("timestampToFormFieldSpec")
@ConsoleModuleData
public
class TimestampToFormFieldSpec {

	@DataAttribute
	String label;

	@DataAttribute
	String name;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Integer size = FormField.defaultSize;

}
