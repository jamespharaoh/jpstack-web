package wbs.console.formaction;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.forms.context.FormContextBuilder;

@Accessors (fluent = true)
@Data
public
class ConsoleFormAction <FormState, History> {

	String name;

	ConsoleFormActionHelper <FormState, History> helper;

	String heading;
	String helpText;
	FormContextBuilder <FormState> actionFormContextBuilder;
	String submitLabel;

	String historyHeading;
	FormContextBuilder <History> historyFormContextBuilder;

}
