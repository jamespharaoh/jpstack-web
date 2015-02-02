package wbs.imchat.core.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.imchat.core.model.ImChatMessageObjectHelper;
import wbs.imchat.core.model.ImChatMessageRec;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("imChatMessagePendingFormAction")
public
class ImChatMessagePendingFormAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	ImChatMessageObjectHelper imChatMessageHelper;
	
	@Inject
	QueueLogic queueLogic;
	
	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"imChatMessagePendingFormResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		int imChatMessageId =
				Integer.parseInt (
					requestContext.parameter ("message_id"));
		
		String messageText =
				requestContext.parameter ("reply");

		return goSend (
				imChatMessageId,
				messageText);


	}

	Responder goSend (
			int imChatMessageId,
			String messageText) {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();		
		
		// find user
		
		UserRec myUser =
				userHelper.find (
					requestContext.userId ());

		// find message
		
		ImChatMessageRec imChatMessage =
				imChatMessageHelper.find (
						imChatMessageId);
		
		int numMessages = imChatMessage.getImChatConversation().getNumMessages();

		// create reply
		
		imChatMessageHelper.insert (
			new ImChatMessageRec ()

			.setImChatConversation (
				imChatMessage.getImChatConversation())

			.setIndex (
					numMessages) 

			.setMessageText(messageText)
		);
		
		// update conversation
		
		imChatMessage.getImChatConversation().setNumMessages(
				numMessages + 1);
		
		// remove queue item

		queueLogic.processQueueItem (
			imChatMessage.getQueueItem (), myUser);
			
		// done

		transaction.commit ();

		requestContext.addNotice (
			"Reply sent");

		// return

		return responder (
			"queueHomeResponder");

	}


}
