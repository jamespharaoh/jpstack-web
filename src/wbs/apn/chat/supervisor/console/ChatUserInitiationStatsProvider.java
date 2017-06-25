package wbs.apn.chat.supervisor.console;

import static wbs.utils.collection.IterableUtils.iterableChainToList;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.IterableUtils.iterableMapToSet;
import static wbs.utils.collection.MapUtils.mapDoesNotContainKey;
import static wbs.utils.etc.OptionalUtils.presentInstancesSet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.NonNull;

import org.joda.time.Interval;

import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsDatum;
import wbs.console.reporting.StatsProvider;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
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

@PrototypeComponent ("chatUserInitiationStatsProvider")
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

	// state

	Map <String, Set <String>> conditions;

	// public implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Set <String>> conditions) {

		if (
			mapDoesNotContainKey (
				conditions,
				"chat-id")
		) {

			throw new IllegalArgumentException (
				"Must provide \"chat-id\" condition");

		}

		this.conditions =
			conditions;

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

			// setup data structures

			Map <Long, Long> alarmsPerUser =
				new TreeMap<> ();

			Set <Object> userIdObjects =
				new HashSet <> ();

			// retrieve messages

			Set <Long> chatIds =
				iterableMapToSet (
					conditions.get (
						"chat-id"),
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
								interval)));

			// aggregate stats

			for (
				ChatUserInitiationLogRec log
					: logs
			) {

				if (log.getReason () != ChatUserInitiationReason.alarmSet)
					continue;

				if (log.getMonitorUser () == null)
					continue;

				// count alarms per user

				if (! userIdObjects.contains (
						log.getMonitorUser ().getId ())) {

					userIdObjects.add (
						log.getMonitorUser ().getId ());

					alarmsPerUser.put (
						log.getMonitorUser ().getId (),
						0l);

				}

				alarmsPerUser.compute (
					log.getMonitorUser ().getId (),
					(key, value) -> value + 1);

			}

			// create return value

			StatsDataSet statsDataSet =
				new StatsDataSet ();

			statsDataSet.indexValues ().put (
				"userId",
				userIdObjects);

			for (
				Object userIdObject
					: userIdObjects
			) {

				Long userId =
					(Long) userIdObject;

				statsDataSet.data ().add (
					new StatsDatum ()

					.startTime (
						interval.getStart ().toInstant ())

					.addIndex (
						"chatId",
						conditions.get (
							"chatId"))

					.addIndex (
						"userId",
						userId)

					.addValue (
						"alarmsSet",
						alarmsPerUser.get (
							userId)));

			}

			return statsDataSet;

		}

	}

}
