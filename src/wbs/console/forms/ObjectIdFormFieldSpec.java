package wbs.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("object-id-field")
@PrototypeComponent ("objectIdFormFieldSpec")
@ConsoleModuleData
public
class ObjectIdFormFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute (
		value = "object-type",
		required = true)
	String objectTypeName;

	@DataAttribute (
		value = "root-field")
	String rootFieldName;

}
