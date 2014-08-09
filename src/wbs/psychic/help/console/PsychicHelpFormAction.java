package wbs.psychic.help.console;

import static wbs.framework.utils.etc.Misc.toInteger;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.psychic.help.model.PsychicHelpRequestRec;
import wbs.psychic.send.logic.PsychicSendLogic;
import wbs.psychic.user.core.console.PsychicUserConsoleHelper;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("psyhicHelpFormAction")
public
class PsychicHelpFormAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	PsychicHelpRequestConsoleHelper psychicHelpRequestHelper;

	@Inject
	PsychicSendLogic psychicSendLogic;

	@Inject
	PsychicUserConsoleHelper psychicUserHelper;

	@Inject
	QueueLogic queueLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("psychicHelpFormResponder");
	}

	@Override
	protected
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		PsychicUserRec psychicUser =
			psychicUserHelper.find (
				requestContext.stuffInt ("psychicUserId"));

		PsychicHelpRequestRec helpRequest =
			psychicHelpRequestHelper.find (
				toInteger (requestContext.getForm ("psychicHelpRequestId")));

		// check things look ok

		if (helpRequest.getPsychicUser () != psychicUser)
			throw new RuntimeException ();

		if (helpRequest.getResponseMessage () != null)
			throw new RuntimeException ();

		// send message

		String textParam =
			requestContext.getForm ("text");

		MessageRec responseMessage =
			psychicSendLogic.sendMagic (
				psychicUser,
				"help",
				0,
				helpRequest.getRequestMessage ().getThreadId (),
				"From help: " + textParam,
				"help");

		// update request

		TextRec responseText =
			textHelper.findOrCreate (textParam);

		helpRequest
			.setResponseMessage (responseMessage)
			.setResponseText (responseText)
			.setResponseTime (transaction.now ())
			.setResponseUser (myUser);

		// update user

		psychicUser.setNumHelpResponses (
			psychicUser.getNumHelpResponses () + 1);

		// update queue item

		PsychicHelpRequestRec nextHelpRequest =
			psychicUser.getHelpRequestsByIndex ().get (
				psychicUser.getNumHelpResponses ());

		if (nextHelpRequest != null) {

			psychicUser.getHelpQueueItem ().setDetails (
				nextHelpRequest.getRequestText ().getText ());

		} else {

			queueLogic.processQueueItem (
				psychicUser.getHelpQueueItem (),
				myUser);

			psychicUser.setHelpQueueItem (null);

		}

		// and finish up

		transaction.commit ();

		requestContext.addNotice ("Help message sent");

		if (nextHelpRequest != null) {
			return responder ("psychicHelpFormResponder");
		} else {
			return responder ("queueHomeResponder");
		}

	}

}
