package wbs.console.forms.upload;

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
public
class UploadFormFieldSpec
	implements ConsoleModuleData {

	@DataAttribute
	String label;

	@DataAttribute
	String name;

	@DataAttribute
	Boolean nullable;

}
