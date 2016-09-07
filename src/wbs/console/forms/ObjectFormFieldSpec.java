package wbs.console.forms;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("object-field")
@PrototypeComponent ("objectFormFieldSpec")
@ConsoleModuleData
public
class ObjectFormFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@Getter @Setter
	boolean dynamic;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute (
		name = "object-type")
	String objectTypeName;

	@DataAttribute (
		name = "field")
	String fieldName;

	@DataAttribute (
		name = "root-field")
	String rootFieldName;

}
