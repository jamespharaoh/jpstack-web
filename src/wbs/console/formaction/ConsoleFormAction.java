package wbs.console.formaction;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.forms.core.ConsoleFormType;

@Accessors (fluent = true)
@Data
public
class ConsoleFormAction <FormState, History> {

	String name;

	ConsoleFormActionHelper <FormState, History> helper;

	String heading;
	String helpText;
	ConsoleFormType <FormState> actionFormContextBuilder;
	String submitLabel;

	String historyHeading;
	ConsoleFormType <History> historyFormContextBuilder;

}
