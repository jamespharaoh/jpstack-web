package wbs.smsapps.autoresponder.daemon;

import static wbs.framework.utils.etc.Misc.isNotNull;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.utils.EmailLogic;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.messageset.logic.MessageSetLogic;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.number.list.logic.NumberListLogic;
import wbs.smsapps.autoresponder.model.AutoResponderObjectHelper;
import wbs.smsapps.autoresponder.model.AutoResponderRec;

import com.google.common.base.Optional;

@Accessors (fluent = true)
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
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageSetLogic messageSetLogic;

	@Inject
	NumberListLogic numberListLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	WbsConfig wbsConfig;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

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
	InboxAttemptRec handle () {

		MessageRec message =
			inbox.getMessage ();

		AutoResponderRec autoResponder =
			autoResponderHelper.find (
				command.getParentObjectId ());

		ServiceRec defaultService =
			serviceHelper.findByCode (
				autoResponder,
				"default");

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
				wbsConfig.defaultEmailAddress (),
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

		// process inbox

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.<AffiliateRec>absent (),
			command);

	}

}
