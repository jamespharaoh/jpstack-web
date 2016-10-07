package wbs.smsapps.subscription.console;

import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("subscriptionNumberAddRemovePart")
public
class SubscriptionNumberAddRemovePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	@Named
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
			addRemoveForm,
			ImmutableMap.of (),
			"update");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/subscriptionNumber.addRemove"));

		htmlTableOpenDetails ();

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			addRemoveFormFieldSet,
			Optional.absent (),
			addRemoveForm,
			ImmutableMap.of (),
			FormType.perform,
			"update");

		htmlTableOpenDetails ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"add\"",
			" value=\"add numbers\"",
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"remove\"",
			" value=\"remove numbers\"",
			">");

		htmlFormClose ();

	}

}
