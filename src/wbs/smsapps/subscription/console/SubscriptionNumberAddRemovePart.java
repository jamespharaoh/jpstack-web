package wbs.smsapps.subscription.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.forms.context.FormContext;
import wbs.console.forms.context.FormContextBuilder;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@PrototypeComponent ("subscriptionNumberAddRemovePart")
public
class SubscriptionNumberAddRemovePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency ("subscriptionNumberAddRemoveFormContextBuilder")
	FormContextBuilder <SubscriptionNumberAddRemoveForm> formContextBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	FormContext <SubscriptionNumberAddRemoveForm> formContext;

	SubscriptionNumberAddRemoveForm addRemoveForm;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			formContext =
				formContextBuilder.build (
					transaction,
					emptyMap ());

			addRemoveForm =
				formContext.object ();

			formContext.update (
				transaction);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlFormOpenPostAction (
				requestContext.resolveLocalUrl (
					"/subscriptionNumber.addRemove"));

			htmlTableOpenDetails ();

			formContext.outputFormRows (
				transaction);

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

}
