package wbs.console.forms.object;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("parent-field")
@PrototypeComponent ("parentFormFieldSpec")
public
class ParentFormFieldSpec
	implements ConsoleSpec {

	// attributes

	@DataAttribute
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute (
		name = "root")
	String rootPath;

	@DataAttribute
	String createPrivDelegate;

	@DataAttribute
	String createPrivCode;

}
