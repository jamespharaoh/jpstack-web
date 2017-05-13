package wbs.console.forms.object;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("deleted-field")
@PrototypeComponent ("deletedFormFieldSpec")
public
class DeletedFormFieldSpec
	implements ConsoleModuleData {

	@DataAttribute
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	String yesLabel;

	@DataAttribute
	String noLabel;

}
