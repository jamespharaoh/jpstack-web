package wbs.console.forms.basic;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("yes-no-field")
@PrototypeComponent ("yesNoField")
public
class YesNoFormFieldSpec
	implements ConsoleSpec {

	@DataAttribute (required = true)
	String name;

	@Getter @Setter
	boolean dynamic;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Boolean hidden;

	@DataAttribute (
		name = "default")
	Optional <Boolean> defaultValue;

	@DataAttribute
	String yesLabel;

	@DataAttribute
	String noLabel;

	@DataAttribute
	String viewPriv;

}
