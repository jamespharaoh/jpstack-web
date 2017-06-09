package wbs.smsapps.subscription.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("subscriptionNumberAddRemovePart")
public
class SubscriptionNumberAddRemovePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency ("subscriptionNumberAddRemoveFormType")
	ConsoleFormType <SubscriptionNumberAddRemoveForm> formType;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ConsoleForm <SubscriptionNumberAddRemoveForm> form;

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

			form =
				formType.buildResponse (
					transaction,
					emptyMap (),
					new SubscriptionNumberAddRemoveForm ());

			form.update (
				transaction);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/subscriptionNumber.addRemove"));

			htmlTableOpenDetails (
				formatWriter);

			form.outputFormRows (
				transaction,
				formatWriter);

			htmlTableOpenDetails (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

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

			htmlFormClose (
				formatWriter);

		}

	}

}
