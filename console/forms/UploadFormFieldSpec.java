package wbs.platform.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;

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

}
