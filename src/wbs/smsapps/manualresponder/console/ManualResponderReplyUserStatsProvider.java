package wbs.smsapps.manualresponder.console;

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

import wbs.platform.user.console.UserConsoleLogic;

import wbs.smsapps.manualresponder.model.ManualResponderReplyStatsSearch;
import wbs.smsapps.manualresponder.model.ManualResponderReplyUserStats;

import wbs.utils.time.TextualInterval;

@PrototypeComponent ("manualResponderReplyUserStatsProvider")
public
class ManualResponderReplyUserStatsProvider
	implements StatsProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderConsoleLogic manualResponderConsoleLogic;

	@SingletonDependency
	ManualResponderConsoleHelper manualResponderHelper;

	@SingletonDependency
	ManualResponderReplyConsoleHelper manualResponderReplyHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	Set <Long> searchManualResponderIds;
	Set <Long> searchUserIds;

	Set <Long> filterManualResponderIds;
	Set <Long> filterUserIds;

	// public implementation

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

			searchManualResponderIds =
				manualResponderConsoleLogic.getSupervisorSearchIds (
					transaction,
					conditions);

			searchUserIds =
				userConsoleLogic.getSupervisorSearchIds (
					transaction,
					conditions);

			// get filters

			filterManualResponderIds =
				manualResponderConsoleLogic.getSupervisorFilterIds (
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

			Set <Object> indexManualResponderIds =
				new HashSet<> ();

			List <ManualResponderReplyUserStats> replyUserStatsList =
				manualResponderReplyHelper.searchUserStats (
					transaction,
					new ManualResponderReplyStatsSearch ()

				.manualResponderIds (
					searchManualResponderIds)

				.userIds (
					searchUserIds)

				.timestamp (
					TextualInterval.forInterval (
						DateTimeZone.UTC,
						interval))

				.filterManualResponderIds (
					filterManualResponderIds)

				.filterUserIds (
					filterUserIds)

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
