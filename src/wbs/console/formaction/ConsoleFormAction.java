package wbs.console.formaction;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldSet;

@Accessors (fluent = true)
@Data
public
class ConsoleFormAction <FormState> {

	String name;

	ConsoleFormActionHelper <FormState> helper;
	FormFieldSet <FormState> formFields;

	String heading;
	String helpText;
	String submitLabel;

}
