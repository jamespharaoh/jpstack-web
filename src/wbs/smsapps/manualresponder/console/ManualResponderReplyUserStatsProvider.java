package wbs.smsapps.manualresponder.console;

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

import wbs.smsapps.manualresponder.model.ManualResponderReplySearch;
import wbs.smsapps.manualresponder.model.ManualResponderReplyUserStats;

import wbs.utils.etc.NumberUtils;
import wbs.utils.time.TextualInterval;

@SingletonComponent ("manualResponderReplyUserStatsProvider")
public
class ManualResponderReplyUserStatsProvider
	implements StatsProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderConsoleHelper manualResponderHelper;

	@SingletonDependency
	ManualResponderReplyConsoleHelper manualResponderReplyHelper;

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

			Optional <Set <Long>> manualResponderIds =
				optionalMapRequired (
					mapItemForKey (
						conditions,
						"manualResponderId"),
					manualResponderIdStrings ->
						iterableMapToSet (
							manualResponderIdStrings,
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

			Set <Object> indexUserIds =
				new HashSet<> ();

			Set <Object> indexManualResponderIds =
				new HashSet<> ();

			for (
				Interval interval
					: period
			) {

				// TODO permissions

				List <ManualResponderReplyUserStats> replyUserStatsList =
					manualResponderReplyHelper.searchUserStats (
						transaction,
						new ManualResponderReplySearch ()

					.manualResponderIds (
						optionalOrNull (
							manualResponderIds))

					.userIds (
						optionalOrNull (
							userIds))

					.timestamp (
						TextualInterval.forInterval (
							DateTimeZone.UTC,
							interval))

				);

				for (
					ManualResponderReplyUserStats replyUserStats
						: replyUserStatsList
				) {

					statsDataSet.data ().add (
						new StatsDatum ()

						.startTime (
							interval.getStart ().toInstant ())

						.addIndex (
							"userId",
							replyUserStats.getUser ().getId ())

						.addIndex (
							"manualResponderId",
							replyUserStats.getManualResponder ().getId ())

						.addValue (
							"numReplies",
							replyUserStats.getNumReplies ())

						.addValue (
							"numCharacters",
							replyUserStats.getNumCharacters ())

					);

					indexUserIds.add (
						replyUserStats.getUser ().getId ());

					indexManualResponderIds.add (
						replyUserStats.getManualResponder ().getId ());

				}

			}

			statsDataSet.indexValues ().put (
				"userId",
				indexUserIds);

			statsDataSet.indexValues ().put (
				"manualResponderId",
				indexManualResponderIds);

			return statsDataSet;

		}

	}

}
