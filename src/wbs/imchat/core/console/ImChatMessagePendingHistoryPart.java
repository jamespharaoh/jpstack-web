package wbs.imchat.core.console;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.Lists;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.imchat.core.model.ImChatConversationRec;
import wbs.imchat.core.model.ImChatCustomerRec;
import wbs.imchat.core.model.ImChatMessageObjectHelper;
import wbs.imchat.core.model.ImChatMessageRec;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleModule;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.priv.console.PrivChecker;

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

	ImChatMessageRec imChatMessage;

	ImChatConversationRec imChatConversation;

	// implementation

	@Override
	public
	void prepare () {

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
		
		FormFieldSet tableFieldSet;
		
		tableFieldSet =
			imChatMessagePendingConsoleModule.formFieldSets ().get (
				"messageFields");
		
		printFormat (
				"<h3>Conversation history</h3>\n");
		
		// retrieve messages

		List<ImChatMessageRec> messages =
			new ArrayList<ImChatMessageRec> (
				imChatConversation.getImChatMessages ());
		
		List <ImChatMessageRec> reverseMessages 
			= Lists.reverse(messages);

		// create message table
		
		printFormat (
				"<table class=\"list\">\n");

		// header

		printFormat (
			"<tr>");

		formFieldLogic.outputTableHeadings (
			out,
			tableFieldSet);

		printFormat (
			"</tr>\n");

		// row

		for (
			ImChatMessageRec message
				: reverseMessages
		) {

			printFormat (
				"<tr>\n");

			formFieldLogic.outputTableCells (
				out,
				tableFieldSet,
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
			imChatConversation.getImChatCustomer();
		
		FormFieldSet tableFieldSet;
		
		tableFieldSet =
			imChatMessagePendingConsoleModule.formFieldSets ().get (
				"customerFields");
		
		printFormat (
				"<h3>Customer summary</h3>\n");

			printFormat ("<table class=\"list\">");
		
		formFieldLogic.outputTableRows (
				out,
				tableFieldSet,
				imChatCustomer,
				true);
		
		printFormat ("</table>");

	}
}
