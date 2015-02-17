package wbs.imchat.core.console;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.imchat.core.model.ImChatConversationRec;
import wbs.imchat.core.model.ImChatMessageObjectHelper;
import wbs.imchat.core.model.ImChatMessageRec;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.priv.console.PrivChecker;

import com.google.common.collect.Lists;

@PrototypeComponent ("imChatMessagePendingHistoryPart")
public
class ImChatMessagePendingHistoryPart
	extends AbstractPagePart {

	// dependencies

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

		// retrieve messages

		List<ImChatMessageRec> messages =
			new ArrayList<ImChatMessageRec> (
				imChatConversation.getImChatMessages ());

		Lists.reverse (
			messages);

		printFormat ("<table class=\"list\" style=\"width:100\">");
		printFormat (
				"<tr>",
				"	<th>Timestamp</th>",
				"	<th>Sender</th>",
				"	<th>Message</th>",
				"</tr>");
		
		for (
			ImChatMessageRec message
				: messages
		) {

			String sender;
			
			switch (message.getType()) {
	            case 0:  sender = "Operator";
	                     break;
	            case 1:  sender = imChatConversation.getImChatCustomer().getEmail();
	                     break;
	            default: sender = "Invalid sender";
	                     break;
			}
			
			
			printFormat (
					"<tr>",
					"	<td>"+message.getTime().toString()+"</td>",
					"	<td>"+sender+"</td>",
					"	<td>"+message.getMessageText ()+"</td>",
					"</tr>");

		}
		
		printFormat ("</table>");

	}
}
