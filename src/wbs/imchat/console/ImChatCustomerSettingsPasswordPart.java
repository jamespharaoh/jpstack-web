package wbs.imchat.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.string.StringUtils.stringInSafe;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;

import java.util.List;
import java.util.stream.Collectors;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.platform.event.console.EventConsoleLogic;
import wbs.platform.event.console.EventLinkConsoleHelper;
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventRec;

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

	// state

	ImChatCustomerRec customer;
	List<EventRec> events;

	// implementation

	@Override
	public
	void prepare () {

		// get customer

		customer =
			imChatCustomerHelper.findRequired (
				requestContext.stuffInteger (
					"imChatCustomerId"));

		// get recent password change events

		GlobalId customerId =
			imChatCustomerHelper.getGlobalId (
				customer);

		events =
			eventLinkHelper.findByTypeAndRef (
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

	@Override
	public
	void renderHtmlBodyContent () {

		requestContext.flushNotices ();

		renderRequestNewPasswordForm ();

		renderRecentPasswordEvents ();

	}

	private
	void renderRequestNewPasswordForm () {

		// heading

		htmlHeadingTwoWrite (
			"Request new password");

		// information

		htmlParagraphWriteFormat (
			"This will generate a new password for the customer, and send it ",
			"it to them via email as usual. It will also display it on the ",
			"screen.");

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/imChatCustomer.settings.password"));

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"generate new password\"",
			">");

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

	}

	private
	void renderRecentPasswordEvents () {

		htmlHeadingTwoWrite (
			"Recent forgotten password events");

		if (
			collectionIsEmpty (
				events)
		) {

			htmlParagraphWriteFormat (
				"No forgotten password events have been logged for this ",
				"customer.");

		} else {

			eventConsoleLogic.writeEventsTable (
				formatWriter,
				events);

		}

	}

}
