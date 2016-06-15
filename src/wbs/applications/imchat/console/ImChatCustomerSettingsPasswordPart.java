package wbs.applications.imchat.console;

import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isEmpty;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.platform.event.console.EventConsoleLogic;
import wbs.platform.event.console.EventLinkConsoleHelper;
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventRec;

@PrototypeComponent ("imChatCustomerSettingsPasswordPart")
public
class ImChatCustomerSettingsPasswordPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	EventConsoleLogic eventConsoleLogic;

	@Inject
	EventLinkConsoleHelper eventLinkHelper;

	@Inject
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
			imChatCustomerHelper.find (
				requestContext.stuffInt (
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

			.filter (event ->
				in (
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

		printFormat (
			"<h2>Request new password</h2>\n");

		printFormat (
			"<p>This will generate a new password for the customer, and send ",
			"it to them via email as usual. It will also display it on the ",
			"screen.</p>\n");

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/imChatCustomer.settings.password"),
			">\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"generate new password\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<h2>Recent forgotten password events</h2>");

		if (
			isEmpty (
				events)
		) {

			printFormat (
				"<p>No forgotten password events have been logged for this ",
				"customer.</p>");

		} else {

			eventConsoleLogic.renderEventsTable (
				formatWriter,
				events);

		}

	}

}
