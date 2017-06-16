package wbs.apn.chat.supervisor.console;

import static wbs.utils.collection.IterableUtils.iterableChainToList;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.IterableUtils.iterableMapToSet;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.presentInstancesSet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.NonNull;

import org.joda.time.Instant;

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

import wbs.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;

@SingletonComponent ("chatUserInitiationStatsProvider")
public
class ChatUserInitiationStatsProvider
	implements StatsProvider {

	// singleton depenencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatUserInitiationLogObjectHelper chatUserInitiationLogHelper;

	@ClassSingletonDependency
	LogContext logContext;

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

			if (period.granularity () != StatsGranularity.hour)
				throw new IllegalArgumentException ();

			if (! conditions.containsKey ("chatId"))
				throw new IllegalArgumentException ();

			// setup data structures

			Map <Long, long[]> alarmsPerUser =
				new TreeMap<> ();

			Set <Object> userIdObjects =
				new HashSet <> ();

			// retrieve messages

			Set <Long> chatIds =
				iterableMapToSet (
					conditions.get (
						"chatId"),
					NumberUtils::parseIntegerRequired);

			Set <ChatRec> chats =
				presentInstancesSet (
					iterableMap (
						chatIds,
						chatId ->
							chatHelper.find (
								transaction,
								chatId)));

			List <ChatUserInitiationLogRec> logs =
				iterableChainToList (
					iterableMap (
						chats,
						chat ->
							chatUserInitiationLogHelper.findByTimestamp (
								transaction,
								chat,
								period.toInterval ())));

			// aggregate stats

			for (
				ChatUserInitiationLogRec log
					: logs
			) {

				if (log.getReason () != ChatUserInitiationReason.alarmSet)
					continue;

				if (log.getMonitorUser () == null)
					continue;

				// work out which hour

				Instant timestamp =
					log.getTimestamp ();

				int hour =
					period.assign (
						timestamp);

				// count alarms per user

				if (! userIdObjects.contains (
						log.getMonitorUser ().getId ())) {

					userIdObjects.add (
						log.getMonitorUser ().getId ());

					alarmsPerUser.put (
						log.getMonitorUser ().getId (),
						new long [
							toJavaIntegerRequired (
								period.size ())]);

				}

				long[] userAlarms =
					alarmsPerUser.get (
						log.getMonitorUser ().getId ());

				userAlarms [hour] ++;

			}

			// create return value

			StatsDataSet statsDataSet =
				new StatsDataSet ();

			statsDataSet.indexValues ().put (
				"userId",
				userIdObjects);

			for (
				int hour = 0;
				hour < period.size ();
				hour ++
			) {

				for (
					Object userIdObject
						: userIdObjects
				) {

					Long userId =
						(Long) userIdObject;

					statsDataSet.data ().add (
						new StatsDatum ()

						.startTime (
							period.step (hour))

						.addIndex (
							"chatId",
							conditions.get ("chatId"))

						.addIndex (
							"userId",
							userId)

						.addValue (
							"alarmsSet",
							alarmsPerUser.get (userId) [hour]));

				}

			}

			return statsDataSet;

		}

	}

}
