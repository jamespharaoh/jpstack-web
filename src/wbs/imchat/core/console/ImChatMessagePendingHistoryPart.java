package wbs.imchat.core.console;

import javax.inject.Inject;

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
		
		
		for (ImChatMessageRec message : imChatConversation.getImChatMessages()) {			
			
			printFormat (
					"<p>%s</p>\n",
					message.getMessageText());		
		}
		

	}

}
