package wbs.smsapps.subscription.console;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("subscriptionNumberAddRemovePart")
public
class SubscriptionNumberAddRemovePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	@Named
	ConsoleModule subscriptionNumberConsoleModule;

	// state

	FormFieldSet <SubscriptionNumberAddRemoveForm> addRemoveFormFieldSet;

	SubscriptionNumberAddRemoveForm addRemoveForm;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		addRemoveFormFieldSet =
			subscriptionNumberConsoleModule.formFieldSetRequired (
				"addRemoveForm",
				SubscriptionNumberAddRemoveForm.class);

		addRemoveForm =
			new SubscriptionNumberAddRemoveForm ();

		formFieldLogic.update (
			taskLogger,
			requestContext,
			addRemoveFormFieldSet,
			addRemoveForm,
			ImmutableMap.of (),
			"update");

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/subscriptionNumber.addRemove"));

		htmlTableOpenDetails ();

		formFieldLogic.outputFormRows (
			taskLogger,
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
