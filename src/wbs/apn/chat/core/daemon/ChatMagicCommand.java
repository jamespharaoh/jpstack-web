package wbs.apn.chat.core.daemon;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.keyword.model.ChatKeywordObjectHelper;
import wbs.apn.chat.keyword.model.ChatKeywordRec;

@Accessors (fluent = true)
@PrototypeComponent ("chatMagicCommand")
public
class ChatMagicCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	ChatKeywordObjectHelper chatKeywordHelper;

	@SingletonDependency
	ChatMiscLogic chatLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@WeakSingletonDependency
	CommandManager commandManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	KeywordFinder keywordFinder;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

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
			"chat.magic"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"handle");

		) {

			ChatRec chat =
				genericCastUnchecked (
					objectManager.getParentRequired (
						transaction,
						command));

			// look for a single keyword

			for (
				KeywordFinder.Match match
					: keywordFinder.find (rest)
			) {

				if (! rest.isEmpty ())
					continue;

				Optional <ChatKeywordRec> chatKeywordOptional =
					chatKeywordHelper.findByCode (
						transaction,
						chat,
						match.simpleKeyword ());

				if (

					optionalIsPresent (
						chatKeywordOptional)

					&& chatKeywordOptional.get ().getGlobal ()

					&& isNotNull (
						chatKeywordOptional.get ().getCommand ())

				) {

					return commandManager.handle (
						transaction,
						inbox,
						chatKeywordOptional.get ().getCommand (),
						optionalAbsent (),
						"");

				}

			}

			// use the default command

			CommandRec defaultCommand =
				commandHelper.findRequired (
					transaction,
					commandRef.get ());

			return commandManager.handle (
				transaction,
				inbox,
				defaultCommand,
				optionalAbsent (),
				rest);

		}

	}

}