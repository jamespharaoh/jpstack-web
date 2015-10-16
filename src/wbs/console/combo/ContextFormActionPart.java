package wbs.console.combo;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("contextFormActionPart")
public
class ContextFormActionPart<FormState>
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	FormFieldSet formFields;

	@Getter @Setter
	ConsoleFormActionHelper<FormState> formActionHelper;

	@Getter @Setter
	String helpText;

	@Getter @Setter
	String submitLabel;

	@Getter @Setter
	String localFile;

	// state

	FormState formState;

	// implementation

	@Override
	public
	void prepare () {

		formState =
			formActionHelper.constructFormState ();

		formActionHelper.updatePassiveFormState (
			formState);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (helpText != null) {

			printFormat (
				"<p>%h</p>\n",
				helpText);

		}

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				localFile),
			">\n");

		printFormat (
			"<table",
			" class=\"details\"",
			">\n");

		formFieldLogic.outputFormRows (
			formatWriter,
			formFields,
			formState);

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"%h\"",
			submitLabel,
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
