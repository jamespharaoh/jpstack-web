package wbs.smsapps.subscription.console;

import javax.inject.Inject;
import javax.inject.Named;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.module.ConsoleModule;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("subscriptionNumberAddRemovePart")
public
class SubscriptionNumberAddRemovePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject @Named
	ConsoleModule subscriptionNumberConsoleModule;

	// state

	FormFieldSet addRemoveFormFieldSet;

	SubscriptionNumberAddRemoveForm addRemoveForm;

	// implementation

	@Override
	public
	void prepare () {

		addRemoveFormFieldSet =
			subscriptionNumberConsoleModule.formFieldSets ().get (
				"addRemoveForm");

		addRemoveForm =
			new SubscriptionNumberAddRemoveForm ();

		formFieldLogic.update (
			addRemoveFormFieldSet,
			addRemoveForm);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/subscriptionNumber.addRemove"),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			out,
			addRemoveFormFieldSet,
			addRemoveForm);

		printFormat (
			"</table>\n");

		printFormat (

			"<p><input",
			" type=\"submit\"",
			" name=\"add\"",
			" value=\"add numbers\"",
			"/>\n",

			"<input",
			" type=\"submit\"",
			" name=\"remove\"",
			" value=\"remove numbers\"",
			"/></p>\n");

		printFormat (
			"</form>\n");

	}

}
