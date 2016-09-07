package wbs.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("timestamp-timezone-field")
@PrototypeComponent ("timestampTimezoneFormFieldSpec")
@ConsoleModuleData
public
class TimestampTimezoneFormFieldSpec {

	@DataAttribute
	String label;

	@DataAttribute
	String name;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Integer size;

}
