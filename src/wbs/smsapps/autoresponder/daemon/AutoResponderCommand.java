package wbs.smsapps.autoresponder.daemon;

import static wbs.framework.utils.etc.Misc.isNotNull;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.email.logic.EmailLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.messageset.logic.MessageSetLogic;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.number.list.logic.NumberListLogic;
import wbs.smsapps.autoresponder.model.AutoResponderObjectHelper;
import wbs.smsapps.autoresponder.model.AutoResponderRec;

@PrototypeComponent ("autoResponderCommand")
public
class AutoResponderCommand
	implements CommandHandler {

	// dependencies

	@Inject
	AutoResponderObjectHelper autoResponderHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	EmailLogic emailLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageSetLogic messageSetLogic;

	@Inject
	NumberListLogic numberListLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"auto_responder.default"
		};

	}

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		CommandRec command =
			commandHelper.find (
				commandId);

		AutoResponderRec autoResponder =
			autoResponderHelper.find (
				command.getParentObjectId ());

		ServiceRec defaultService =
			serviceHelper.findByCode (
				autoResponder,
				"default");

		receivedMessage.setServiceId (
			defaultService.getId ());

		// send message set

		MessageSetRec messageSet =
			messageSetLogic.findMessageSet (
				autoResponder,
				"default");

		messageSetLogic.sendMessageSet (
			messageSet,
			message.getThreadId (),
			message.getNumber (),
			defaultService);

		// send email

		if (autoResponder.getEmailAddress () != null
				&& autoResponder.getEmailAddress ().length () > 0) {

			emailLogic.sendEmail (
				autoResponder.getEmailAddress (),
				"Auto responder " + autoResponder.getDescription (),
				message.getText ().getText ());
		}

		// add to number list

		if (
			isNotNull (
				autoResponder.getAddToNumberList ())
		) {

			numberListLogic.addDueToMessage (
				autoResponder.getAddToNumberList (),
				message.getNumber (),
				message,
				defaultService);

		}

		// finish up

		transaction.commit ();

		return Status.processed;

	}

}
