package wbs.applications.imchat.console;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageObjectHelper;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleModule;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.priv.console.PrivChecker;

import com.google.common.collect.Lists;

@PrototypeComponent ("imChatMessagePendingHistoryPart")
public
class ImChatMessagePendingHistoryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject @Named
	ConsoleModule imChatMessagePendingConsoleModule;

	@Inject
	ImChatMessageObjectHelper imChatMessageHelper;

	@Inject
	PrivChecker privChecker;

	// state

	FormFieldSet customerFields;
	FormFieldSet messageFields;

	ImChatMessageRec imChatMessage;
	ImChatConversationRec imChatConversation;

	// implementation

	@Override
	public
	void prepare () {

		// get field sets

		customerFields =
			imChatMessagePendingConsoleModule.formFieldSets ().get (
				"customerFields");

		messageFields =
			imChatMessagePendingConsoleModule.formFieldSets ().get (
				"messageFields");

		// load data

		imChatMessage =
			imChatMessageHelper.find (
				requestContext.stuffInt ("imChatMessageId"));

		imChatConversation =
			imChatMessage.getImChatConversation ();

	}

	@Override
	public
	void goBodyStuff () {

		goSummary ();

		goHistory ();

	}

	void goHistory () {

		printFormat (
			"<h3>Conversation history</h3>\n");

		// retrieve messages

		List<ImChatMessageRec> messages =
			new ArrayList<ImChatMessageRec> (
				imChatConversation.getImChatMessages ());

		List<ImChatMessageRec> reverseMessages =
			Lists.reverse (
				messages);

		// create message table

		printFormat (
			"<table class=\"list\">\n");

		// header

		printFormat (
			"<tr>\n");

		formFieldLogic.outputTableHeadings (
			out,
			messageFields);

		printFormat (
			"</tr>\n");

		// row

		for (
			ImChatMessageRec message
				: reverseMessages
		) {

			printFormat (
				"<tr>\n");

			formFieldLogic.outputTableCellsList (
				out,
				messageFields,
				message,
				true);

		    printFormat (
			    "</tr>\n");

		}

		printFormat (
			"<tr>\n");

		printFormat (
			"</table>\n");

	}

	void goSummary () {

		ImChatCustomerRec imChatCustomer =
			imChatConversation.getImChatCustomer ();

		printFormat (
			"<h3>Customer summary</h3>\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputTableRows (
			out,
			customerFields,
			imChatCustomer,
			true);

		printFormat (
			"</table>\n");

	}

}
