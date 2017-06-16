package wbs.imchat.console;

import static wbs.utils.collection.IterableUtils.iterableMapToSet;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

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

import wbs.utils.etc.NumberUtils;
import wbs.utils.time.TextualInterval;

import wbs.imchat.model.ImChatMessageSearch;
import wbs.imchat.model.ImChatMessageUserStats;

@SingletonComponent ("imChatMessageUserStatsProvider")
public
class ImChatMessageUserStatsProvider
	implements StatsProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ImChatConsoleHelper imChatHelper;

	@SingletonDependency
	ImChatMessageConsoleHelper imChatMessageHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

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

			Optional <Set <Long>> imChatIds =
				optionalMapRequired (
					mapItemForKey (
						conditions,
						"imChatId"),
					imChatIdStrings ->
						iterableMapToSet (
							imChatIdStrings,
							NumberUtils::parseIntegerRequired));

			Optional <Set <Long>> userIds =
				optionalMapRequired (
					mapItemForKey (
						conditions,
						"userId"),
					userIdStrings ->
						iterableMapToSet (
							userIdStrings,
							NumberUtils::parseIntegerRequired));

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

				// TODO permissions

				List <ImChatMessageUserStats> messageUserStatsList =
					imChatMessageHelper.searchMessageUserStats (
						transaction,
						new ImChatMessageSearch ()

					.imChatIds (
						optionalOrNull (
							imChatIds))

					.senderUserIds (
						optionalOrNull (
							userIds))

					.timestamp (
						TextualInterval.forInterval (
							DateTimeZone.UTC,
							interval))

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
