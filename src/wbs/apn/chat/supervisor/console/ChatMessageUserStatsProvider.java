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
import wbs.console.reporting.StatsGranularity;
import wbs.console.reporting.StatsPeriod;
import wbs.console.reporting.StatsProvider;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.time.TextualInterval;

import wbs.apn.chat.contact.console.ChatMessageConsoleHelper;
import wbs.apn.chat.contact.model.ChatMessageSearch;
import wbs.apn.chat.contact.model.ChatMessageUserStats;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.console.ChatConsoleLogic;

@SingletonComponent ("chatMessageUserStatsProvider")
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

	// implementation

	@Override
	public
	StatsDataSet getStats (
			@NonNull Transaction parentTransaction,
			@NonNull StatsPeriod period,
			@NonNull Map <String, Set <String>> conditions) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getStats");

		) {

			if (period.granularity () != StatsGranularity.hour) {
				throw new IllegalArgumentException ();
			}

			// get conditions

			Set <Long> searchChatIds =
				chatConsoleLogic.getSupervisorSearchChatIds (
					transaction,
					conditions);

			Set <Long> searchUserIds =
				userConsoleLogic.getSupervisorSearchUserIds (
					transaction,
					conditions);

			// get filters

			Set <Long> filterChatIds =
				chatConsoleLogic.getSupervisorFilterChatIds (
					transaction);

			Set <Long> filterUserIds =
				userConsoleLogic.getSupervisorFilterUserIds (
					transaction);

			// fetch stats

			StatsDataSet statsDataSet =
				new StatsDataSet ();

			Set <Object> indexUserIds =
				new HashSet<> ();

			Set <Object> indexChatIds =
				new HashSet<> ();

			for (
				Interval interval
					: period
			) {

				List <ChatMessageUserStats> messageUserStatsList =
					chatMessageHelper.searchUserStats (
						transaction,
						new ChatMessageSearch ()

					.chatIds (
						searchChatIds)

					.senderUserIds (
						searchUserIds)

					.timestamp (
						TextualInterval.forInterval (
							DateTimeZone.UTC,
							interval))

					.filter (
						true)

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
							"numFinal",
							messageUserStats.getNumFinalMessages ())

					);

					indexUserIds.add (
						messageUserStats.getUser ().getId ());

					indexChatIds.add (
						messageUserStats.getChat ().getId ());

				}

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
