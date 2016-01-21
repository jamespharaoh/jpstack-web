package wbs.applications.imchat.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.joinWithSpace;
import static wbs.framework.utils.etc.Misc.optionalIf;
import static wbs.framework.utils.etc.Misc.presentInstances;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerDetailTypeRec;
import wbs.applications.imchat.model.ImChatCustomerDetailValueRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageObjectHelper;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("imChatPendingSummaryPart")
public
class ImChatPendingSummaryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject @Named
	ConsoleModule imChatPendingConsoleModule;

	@Inject
	ImChatMessageObjectHelper imChatMessageHelper;

	@Inject
	PrivChecker privChecker;

	// state

	FormFieldSet customerFields;
	FormFieldSet messageFields;

	ImChatMessageRec message;
	ImChatConversationRec conversation;
	ImChatCustomerRec customer;
	ImChatRec imChat;

	boolean canSupervise;

	// implementation

	@Override
	public
	void prepare () {

		// get field sets

		customerFields =
			imChatPendingConsoleModule.formFieldSets ().get (
				"customerFields");

		messageFields =
			imChatPendingConsoleModule.formFieldSets ().get (
				"messageFields");

		// load data

		message =
			imChatMessageHelper.find (
				requestContext.stuffInt (
					"imChatMessageId"));

		conversation =
			message.getImChatConversation ();

		customer =
			conversation.getImChatCustomer ();

		imChat =
			customer.getImChat ();

		// misc

		canSupervise =
			privChecker.can (
				imChat,
				"supervisor");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<div class=\"layout-container\">\n",
			"<table class=\"layout\">\n",
			"<tbody>\n",
			"<tr>\n",
			"<td style=\"width: 50%%\">\n");

		goSummary ();

		printFormat (
			"</td>\n",
			"<td style=\"width: 50%%\">\n");

		goDetails ();

		printFormat (
			"</td>\n",
			"</tr>\n",
			"</tbody>\n",
			"</table>\n",
			"</div>\n");

		goHistory ();

	}

	void goDetails () {

		printFormat (
			"<h3>Customer details</h3>\n");

		printFormat (
			"<table class=\"details\">\n",
			"<tbody>\n");

		for (
			ImChatCustomerDetailTypeRec detailType
				: imChat.getCustomerDetailTypes ()
		) {

			if (

				detailType.getRestricted ()

				&& ! canSupervise

			) {
				continue;
			}

			ImChatCustomerDetailValueRec detailValue =
				customer.getDetails ().get (
					detailType.getId ());

			printFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				detailType.getName (),
				"<td>%h</td>\n",
				detailValue != null
					? detailValue.getValue ()
					: "-",
				"</tr>\n");

		}

		printFormat (
			"</tbody>\n",
			"</table>\n");

	}

	void goSummary () {

		printFormat (
			"<h3>Customer summary</h3>\n");

		formFieldLogic.outputDetailsTable (
			formatWriter,
			customerFields,
			customer);

	}

	void goHistory () {

		printFormat (
			"<h3>Conversation history</h3>\n");

		// retrieve messages

		List<ImChatMessageRec> messages =
			new ArrayList<ImChatMessageRec> (
				conversation.getMessagesIn ());

		List<ImChatMessageRec> historyRequests =
			Lists.reverse (
				messages);

		// create message table

		printFormat (
			"<table class=\"list\">\n");

		// header

		printFormat (
			"<tr>\n");

		formFieldLogic.outputTableHeadings (
			formatWriter,
			messageFields);

		printFormat (
			"</tr>\n");

		// row

		for (
			ImChatMessageRec historyRequest
				: historyRequests
		) {

			if (
				isNotNull (
					historyRequest.getPartnerImChatMessage ())
			) {

				ImChatMessageRec historyReply =
					historyRequest.getPartnerImChatMessage ();

				printFormat (
					"<tr",
					" class=\"%h\"",
					classForMessage (
						historyReply),
					">\n");

				formFieldLogic.outputTableCellsList (
					formatWriter,
					messageFields,
					historyReply,
					true);

				printFormat (
					"</tr>\n");

			}

			printFormat (
				"<tr",
				" class=\"%h\"",
				joinWithSpace (
					presentInstances (
						Optional.of (
							classForMessage (
								historyRequest)),
						optionalIf (
							equal (
								message,
								historyRequest),
							"selected"))),
				">\n");

			formFieldLogic.outputTableCellsList (
				formatWriter,
				messageFields,
				historyRequest,
				true);

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"<tr>\n");

		printFormat (
			"</table>\n");

	}

	String classForMessage (
			@NonNull ImChatMessageRec message) {

		if (
			isNotNull (
				message.getPrice ())
		) {

			return "message-out-charge";

		} else if (
			isNotNull (
				message.getSenderUser ())
		) {

			return "message-out";

		} else {

			return "message-in";

		}

	}

}
