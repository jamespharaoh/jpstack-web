package wbs.console.forms.object;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("description-field")
@PrototypeComponent ("descriptionFormFieldSpec")
public
class DescriptionFormFieldSpec
	implements ConsoleModuleData {

	@DataAttribute
	String label;

	@DataAttribute
	String name;

	@DataAttribute
	String delegate;

	@DataAttribute
	Integer size;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Boolean nullable;

}
