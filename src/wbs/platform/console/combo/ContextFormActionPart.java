package wbs.platform.console.combo;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.clients.apn.chat.user.admin.console.ConsoleFormActionHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.part.AbstractPagePart;

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
	void goBodyStuff () {

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
			out,
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
