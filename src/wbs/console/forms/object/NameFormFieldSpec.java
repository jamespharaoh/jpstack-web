package wbs.console.forms.object;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("name-field")
@PrototypeComponent ("nameFormFieldSpec")
public
class NameFormFieldSpec
	implements ConsoleSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	String pattern;

	@DataAttribute
	String patternError;

	@DataAttribute
	String codePattern;

	@DataAttribute
	Integer size;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Boolean nullable;

}
