package wbs.smsapps.forwarder.daemon;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.messageset.logic.MessageSetLogic;
import wbs.sms.messageset.model.MessageSetObjectHelper;

import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderRec;

@Accessors (fluent = true)
@SingletonComponent ("forwarderCommand")
public
class ForwarderCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@SingletonDependency
	ForwarderObjectHelper forwarderHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	MessageSetObjectHelper messageSetHelper;

	@SingletonDependency
	MessageSetLogic messageSetLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Long> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"forwarder.forwarder"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			ForwarderRec forwarder =
				forwarderHelper.findRequired (
					command.getParentId ());

			MessageRec message =
				inbox.getMessage ();

			ServiceRec defaultService =
				serviceHelper.findByCodeRequired (
					forwarder,
					"default");

			forwarderMessageInHelper.insert (
				taskLogger,
				forwarderMessageInHelper.createInstance ()

				.setForwarder (
					forwarder)

				.setNumber (
					message.getNumber ())

				.setMessage (
					message)

				.setSendQueue (
					forwarder.getUrl ().length () > 0)

				.setRetryTime (
					transaction.now ())

			);

			messageSetLogic.sendMessageSet (
				taskLogger,
				messageSetHelper.findByCodeRequired (
					forwarder,
					"forwarder"),
				message.getThreadId (),
				message.getNumber (),
				defaultService);

			// process inbox

			return smsInboxLogic.inboxProcessed (
				taskLogger,
				inbox,
				optionalOf (
					defaultService),
				optionalAbsent (),
				command);

		}

	}

}
