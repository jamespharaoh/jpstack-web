package wbs.apn.chat.supervisor.console;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

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

import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.time.TextualInterval;

import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageStats;
import wbs.apn.chat.contact.model.ChatMessageStatsSearch;
import wbs.apn.chat.core.console.ChatConsoleLogic;

@SingletonComponent ("chatMessageStatsProvider")
public
class ChatMessageStatsProvider
	implements StatsProvider {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleLogic chatConsoleLogic;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

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
				userConsoleLogic.getSupervisorSearchIds (
					transaction,
					conditions);

			// get filters

			Set <Long> filterChatIds =
				chatConsoleLogic.getSupervisorFilterChatIds (
					transaction);

			Set <Long> filterUserIds =
				userConsoleLogic.getSupervisorFilterIds (
					transaction);

			// fetch stats

			StatsDataSet statsDataSet =
				new StatsDataSet ();

			Set <Object> indexChatIds =
				new HashSet<> ();

			for (
				Interval interval
					: period
			) {

				List <ChatMessageStats> messageStatsList =
					chatMessageHelper.searchStats (
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
					ChatMessageStats messageStats
						: messageStatsList
				) {

					statsDataSet.data ().add (
						new StatsDatum ()

						.startTime (
							interval.getStart ().toInstant ())

						.addIndex (
							"chatId",
							messageStats.getChat ().getId ())

						.addValue (
							"numMessages",
							messageStats.getNumMessages ())

						.addValue (
							"numCharacters",
							messageStats.getNumCharacters ())

						.addValue (
							"numFinal",
							messageStats.getNumFinalMessages ())

					);

					indexChatIds.add (
						messageStats.getChat ().getId ());

				}

			}

			statsDataSet.indexValues ().put (
				"chatId",
				indexChatIds);

			return statsDataSet;

		}

	}

}
