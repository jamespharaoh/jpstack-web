package wbs.console.forms;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("text-field")
@PrototypeComponent ("textFormFieldSpec")
@ConsoleModuleData
public
class TextFormFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@Getter @Setter
	boolean dynamic;

	@DataAttribute
	String label;

	@DataAttribute
	Integer size;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Boolean nullable;

}
