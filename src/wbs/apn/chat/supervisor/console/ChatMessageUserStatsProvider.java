package wbs.apn.chat.supervisor.console;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import wbs.console.priv.UserPrivChecker;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsDatum;
import wbs.console.reporting.StatsProvider;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.time.TextualInterval;

import wbs.apn.chat.contact.console.ChatMessageConsoleHelper;
import wbs.apn.chat.contact.model.ChatMessageStatsSearch;
import wbs.apn.chat.contact.model.ChatMessageUserStats;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.console.ChatConsoleLogic;

@PrototypeComponent ("chatMessageUserStatsProvider")
public
class ChatMessageUserStatsProvider
	implements StatsProvider {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleLogic chatConsoleLogic;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatMessageConsoleHelper chatMessageHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// state

	Set <Long> searchChatIds;
	Set <Long> searchUserIds;

	Set <Long> filterChatIds;
	Set <Long> filterUserIds;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Set <String>> conditions) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			// get conditions

			searchChatIds =
				chatConsoleLogic.getSupervisorSearchChatIds (
					transaction,
					conditions);

			searchUserIds =
				userConsoleLogic.getSupervisorSearchIds (
					transaction,
					conditions);

			// get filters

			filterChatIds =
				chatConsoleLogic.getSupervisorFilterChatIds (
					transaction);

			filterUserIds =
				userConsoleLogic.getSupervisorFilterIds (
					transaction);

		}

	}

	@Override
	public
	StatsDataSet getStats (
			@NonNull Transaction parentTransaction,
			@NonNull Interval interval) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getStats");

		) {

			// fetch stats

			StatsDataSet statsDataSet =
				new StatsDataSet ();

			Set <Object> indexUserIds =
				new HashSet<> ();

			Set <Object> indexChatIds =
				new HashSet<> ();

			List <ChatMessageUserStats> messageUserStatsList =
				chatMessageHelper.searchUserStats (
					transaction,
					new ChatMessageStatsSearch ()

				.chatIds (
					searchChatIds)

				.senderUserIds (
					searchUserIds)

				.timestamp (
					TextualInterval.forInterval (
						DateTimeZone.UTC,
						interval))

				.filterChatIds (
					filterChatIds)

				.filterSenderUserIds (
					filterUserIds)

			);

			for (
				ChatMessageUserStats messageUserStats
					: messageUserStatsList
			) {

				statsDataSet.data ().add (
					new StatsDatum ()

					.startTime (
						interval.getStart ().toInstant ())

					.addIndex (
						"userId",
						messageUserStats.getUser ().getId ())

					.addIndex (
						"chatId",
						messageUserStats.getChat ().getId ())

					.addValue (
						"numMessages",
						messageUserStats.getNumMessages ())

					.addValue (
						"numCharacters",
						messageUserStats.getNumCharacters ())

					.addValue (
						"numMessagesFinal",
						messageUserStats.getNumMessagesFinal ())

				);

				indexUserIds.add (
					messageUserStats.getUser ().getId ());

				indexChatIds.add (
					messageUserStats.getChat ().getId ());

			}

			statsDataSet.indexValues ().put (
				"userId",
				indexUserIds);

			statsDataSet.indexValues ().put (
				"chatId",
				indexChatIds);

			return statsDataSet;

		}

	}

}
