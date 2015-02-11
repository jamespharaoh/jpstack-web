package wbs.imchat.core.console;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.priv.console.PrivChecker;
import wbs.imchat.core.console.ImChatMessagePendingHistoryPart;
import wbs.imchat.core.model.ImChatConversationRec;
import wbs.imchat.core.model.ImChatMessageObjectHelper;
import wbs.imchat.core.model.ImChatMessageRec;

@PrototypeComponent ("imChatMessagePendingHistoryPart")
public class ImChatMessagePendingHistoryPart
	extends AbstractPagePart {

	@Inject
	PrivChecker privChecker;

	ImChatMessageRec imChatMessage;

	ImChatConversationRec imChatConversation;

	@Inject
	ImChatMessageObjectHelper imChatMessageHelper;

	// implementation

		@Override
		public
		void prepare () {

			imChatMessage =
					imChatMessageHelper.find (
						requestContext.stuffInt ("imChatMessageId"));

			imChatConversation = imChatMessage.getImChatConversation();


		}


	@Override
	public
	void goBodyStuff () {


		// retrieve messages

		List<ImChatMessageRec> messages =
			new ArrayList<ImChatMessageRec> (
				imChatConversation.getImChatMessages());

		Lists.reverse (
			messages);

		for (ImChatMessageRec message : messages) {

			printFormat (
					"<p>%s</p>\n",
					message.getMessageText());
		}


	}

}
