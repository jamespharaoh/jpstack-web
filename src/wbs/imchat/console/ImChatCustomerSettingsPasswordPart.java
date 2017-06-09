package wbs.imchat.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.string.StringUtils.stringInSafe;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;

import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;

import wbs.platform.event.console.EventConsoleLogic;
import wbs.platform.event.console.EventLinkConsoleHelper;
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventRec;

import wbs.utils.string.FormatWriter;

import wbs.imchat.model.ImChatCustomerRec;

@PrototypeComponent ("imChatCustomerSettingsPasswordPart")
public
class ImChatCustomerSettingsPasswordPart
	extends AbstractPagePart {

	// dependencies

	@SingletonDependency
	EventConsoleLogic eventConsoleLogic;

	@SingletonDependency
	EventLinkConsoleHelper eventLinkHelper;

	@SingletonDependency
	ImChatCustomerConsoleHelper imChatCustomerHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ImChatCustomerRec customer;
	List <EventRec> events;

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

			// get customer

			customer =
				imChatCustomerHelper.findFromContextRequired (
					transaction);

			// get recent password change events

			GlobalId customerId =
				imChatCustomerHelper.getGlobalId (
					customer);

			events =
				eventLinkHelper.findByTypeAndRef (
					transaction,
					customerId.typeId (),
					customerId.objectId ())

				.stream ()

				.map (
					EventLinkRec::getEvent)

				.filter (
					event ->
						stringInSafe (
							event.getEventType ().getCode (),
							"im_chat_customer_forgotten_password",
							"im_chat_customer_generated_password_from_console"))

				.distinct ()

				.sorted ()

				.collect (
					Collectors.toList ());

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

			requestContext.flushNotices (
				formatWriter);

			renderRequestNewPasswordForm (
				transaction,
				formatWriter);

			renderRecentPasswordEvents (
				transaction,
				formatWriter);

		}

	}

	private
	void renderRequestNewPasswordForm (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderRequestNewPasswordForm");

		) {

			// heading

			htmlHeadingTwoWrite (
				formatWriter,
				"Request new password");

			// information

			htmlParagraphWriteFormat (
				formatWriter,
				"This will generate a new password for the customer, and send ",
				"it them via email as usual. It will also display it on the ",
				"screen.");

			// form open

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/imChatCustomer.settings.password"));

			// form controls

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"generate new password\"",
				">");

			htmlParagraphClose (
				formatWriter);

			// form close

			htmlFormClose (
				formatWriter);

		}

	}

	private
	void renderRecentPasswordEvents (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderRecentPasswordEvents");

		) {

			htmlHeadingTwoWrite (
				formatWriter,
				"Recent forgotten password events");

			if (
				collectionIsEmpty (
					events)
			) {

				htmlParagraphWriteFormat (
					formatWriter,
					"No forgotten password events have been logged for this ",
					"customer.");

			} else {

				eventConsoleLogic.writeEventsTable (
					transaction,
					formatWriter,
					events);

			}

		}

	}

}
