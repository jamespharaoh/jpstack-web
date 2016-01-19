package wbs.smsapps.subscription.console;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Optional;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

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
			requestContext,
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
			requestContext,
			formatWriter,
			addRemoveFormFieldSet,
			Optional.<UpdateResultSet>absent (),
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
