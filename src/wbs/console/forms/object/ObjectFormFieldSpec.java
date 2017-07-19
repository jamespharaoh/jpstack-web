package wbs.console.forms.object;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.ConsoleFormFieldSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("object-field")
@PrototypeComponent ("objectFormFieldSpec")
public
class ObjectFormFieldSpec
	implements ConsoleFormFieldSpec {

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

	@DataAttribute (
		name = "view-priv")
	String viewPrivCode;

	@DataAttribute (
		name = "manage-priv")
	String managePrivCode;

	@DataAttribute
	String optionLabel;

	@DataAttribute (
		name = "feature")
	String featureCode;

}
