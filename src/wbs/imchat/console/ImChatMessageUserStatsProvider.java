package wbs.imchat.console;

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

import wbs.imchat.model.ImChatMessageStatsSearch;
import wbs.imchat.model.ImChatMessageUserStats;

@SingletonComponent ("imChatMessageUserStatsProvider")
public
class ImChatMessageUserStatsProvider
	implements StatsProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ImChatConsoleLogic imChatConsoleLogic;

	@SingletonDependency
	ImChatConsoleHelper imChatHelper;

	@SingletonDependency
	ImChatMessageConsoleHelper imChatMessageHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

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

			// get conditions

			Set <Long> searchImChatIds =
				imChatConsoleLogic.getSupervisorSearchIds (
					transaction,
					conditions);

			Set <Long> searchUserIds =
				userConsoleLogic.getSupervisorSearchIds (
					transaction,
					conditions);

			// get filters

			Set <Long> filterImChatIds =
				imChatConsoleLogic.getSupervisorFilterIds (
					transaction);

			Set <Long> filterUserIds =
				userConsoleLogic.getSupervisorFilterIds (
					transaction);

			// fetch stats

			StatsDataSet statsDataSet =
				new StatsDataSet ();

			Set <Object> indexImChatIds =
				new HashSet<> ();

			Set <Object> indexUserIds =
				new HashSet<> ();

			for (
				Interval interval
					: period
			) {

				List <ImChatMessageUserStats> messageUserStatsList =
					imChatMessageHelper.searchMessageUserStats (
						transaction,
						new ImChatMessageStatsSearch ()

					.imChatIds (
						searchImChatIds)

					.senderUserIds (
						searchUserIds)

					.timestamp (
						TextualInterval.forInterval (
							DateTimeZone.UTC,
							interval))

					.filterImChatIds (
						filterImChatIds)

					.filterSenderUserIds (
						filterUserIds)

				);

				for (
					ImChatMessageUserStats messageUserStats
						: messageUserStatsList
				) {

					statsDataSet.data ().add (
						new StatsDatum ()

						.startTime (
							interval.getStart ().toInstant ())

						.addIndex (
							"userId",
							messageUserStats.getSenderUser ().getId ())

						.addIndex (
							"manualResponderId",
							messageUserStats.getImChat ().getId ())

						.addValue (
							"numMessages",
							messageUserStats.getNumMessages ())

						.addValue (
							"numCharacters",
							messageUserStats.getNumCharacters ())

					);

					indexUserIds.add (
						messageUserStats.getSenderUser ().getId ());

					indexImChatIds.add (
						messageUserStats.getImChat ().getId ());

				}

			}

			statsDataSet.indexValues ().put (
				"userId",
				indexUserIds);

			statsDataSet.indexValues ().put (
				"imChatId",
				indexImChatIds);

			return statsDataSet;

		}

	}

}
