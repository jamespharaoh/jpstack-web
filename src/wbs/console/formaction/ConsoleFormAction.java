package wbs.console.formaction;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldSet;

@Accessors (fluent = true)
@Data
public
class ConsoleFormAction <FormState, History> {

	String name;

	ConsoleFormActionHelper <FormState, History> helper;

	String heading;
	String helpText;
	FormFieldSet <FormState> formFields;
	String submitLabel;

	String historyHeading;
	FormFieldSet <History> historyFields;

}
