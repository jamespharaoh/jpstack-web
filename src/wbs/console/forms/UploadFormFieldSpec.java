package wbs.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("upload-field")
@PrototypeComponent ("uploadFormFieldSpec")
@ConsoleModuleData
public
class UploadFormFieldSpec {

	@DataAttribute
	String label;

	@DataAttribute
	String name;

	@DataAttribute
	Boolean nullable;

}
