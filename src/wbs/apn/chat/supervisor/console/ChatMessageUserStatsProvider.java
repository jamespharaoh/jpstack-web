package wbs.apn.chat.supervisor.console;

import static wbs.utils.collection.CollectionUtils.singletonSet;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.presentInstancesList;
import static wbs.utils.time.TimeUtils.earlierThan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.NonNull;

import org.joda.time.Instant;

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

import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageSearch;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;

@SingletonComponent ("chatMessageUserStatsProvider")
public
class ChatMessageUserStatsProvider
	implements StatsProvider {

	// singleton dependencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@ClassSingletonDependency
	LogContext logContext;

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

			if (period.granularity () != StatsGranularity.hour)
				throw new IllegalArgumentException ();

			List <ChatRec> chats =
				new ArrayList<> ();

			if (conditions.containsKey ("chatId")) {

				List <Long> chatIds =
					iterableMapToList (
						conditions.get (
							"chatId"),
						NumberUtils::parseIntegerRequired);

				chats =
					presentInstancesList (
						chatHelper.findMany (
							transaction,
							chatIds));

			} else {

				chats =
					chatHelper.findAll (
						transaction);

			}

			StatsDataSet combinedStatsDataSet =
				new StatsDataSet ();

			Set<Object> userIds =
				new HashSet<Object> ();

			for (ChatRec chat : chats) {

				if (
					! privChecker.canRecursive (
						transaction,
						chat,
						"supervisor")
				) {
					continue;
				}

				StatsDataSet singleStatsDataSet =
					getStatsForChat (
						transaction,
						period,
						chat);

				userIds.addAll (
					singleStatsDataSet.indexValues ().get ("userId"));

				combinedStatsDataSet.data ().addAll (
					singleStatsDataSet.data ());

			}

			combinedStatsDataSet.indexValues ().put (
				"userId",
				userIds);

			return combinedStatsDataSet;

		}

	}

	private
	StatsDataSet getStatsForChat (
			@NonNull Transaction parentTransaction,
			@NonNull StatsPeriod period,
			@NonNull ChatRec chat) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getStatsForChat");

		) {

			// setup data structures

			Map <Long, long[]> countPerUser =
				new TreeMap<> ();

			Map <Long, long[]> charsPerUser =
				new TreeMap<> ();

			Map <Long, long[]> finalCountPerUser =
				new TreeMap<> ();

			Set <Object> userIds =
				new HashSet<> ();

			// retrieve messages

			ChatMessageSearch chatMessageSearch =
				new ChatMessageSearch ();

			chatMessageSearch

				.chatIdIn (
					singletonSet (
						chat.getId ()))

				.timestampAfter (
					period.startTime ())

				.timestampBefore (
					period.endTime ())

			;

			List <ChatMessageRec> chatMessages =
				chatMessageHelper.search (
					transaction,
					chatMessageSearch);

			// aggregate stats

			for (
				ChatMessageRec chatMessage
					: chatMessages
			) {

				Instant chatMessageTimestamp =
					chatMessage.getTimestamp ();

				int hour =
					period.assign (
						chatMessageTimestamp);

				// count outbound messages

				if (chatMessage.getSender () != null) {

					if (! userIds.contains (
							chatMessage.getSender ().getId ())) {

						userIds.add (
							chatMessage.getSender ().getId ());

						countPerUser.put (
							chatMessage.getSender ().getId (),
							new long [
								toJavaIntegerRequired (
									period.size ())]);

						charsPerUser.put (
							chatMessage.getSender ().getId (),
							new long [
								toJavaIntegerRequired (
									period.size ())]);

						finalCountPerUser.put (
							chatMessage.getSender ().getId (),
							new long [
								toJavaIntegerRequired (
									period.size ())]);

					}

					long[] userCounts =
						countPerUser.get (
							chatMessage.getSender ().getId ());

					userCounts [hour] ++;

					long[] userChars =
						charsPerUser.get (
							chatMessage.getSender ().getId ());

					long length =
						chatMessage.getOriginalText ().getText ().length ();

					userChars [
						hour] +=
							length;

					ChatContactRec chatContact =
						chatMessage.getChatContact ();

					ChatContactRec inverseChatContact =
						chatContact.getInverseChatContact ();

					if (

						// is the last message

						chatMessage
							.getIndex ()
						== chatContact
							.getNumChatMessages () - 1

						&& (

							// no reverse contact

							inverseChatContact
							== null

							// no reverse message

							|| inverseChatContact
								.getLastDeliveredMessageTime ()
							== null

							// reverse message is older

							|| earlierThan (
								inverseChatContact.getLastDeliveredMessageTime (),
								chatMessage.getTimestamp ())

						)

					) {

						long[] finalCounts =
							finalCountPerUser.get (
								chatMessage.getSender ().getId ());

						finalCounts [hour] ++;

					}

				}

			}

			// create return value

			StatsDataSet statsDataSet =
				new StatsDataSet ();

			statsDataSet.indexValues ()
				.put ("userId", userIds);

			for (
				int hour = 0;
				hour < period.size ();
				hour ++
			) {

				for (
					Object userIdObject
						: userIds
				) {

					Long userId =
						(Long)
						userIdObject;

					statsDataSet.data ().add (
						new StatsDatum ()

						.startTime (
							period.step (hour))

						.addIndex (
							"chatId",
							chat.getId ())

						.addIndex (
							"userId",
							userId)

						.addValue (
							"messagesSent",
							countPerUser.get (userId) [hour])

						.addValue (
							"charactersSent",
							charsPerUser.get (userId) [hour])

						.addValue (
							"finalMessagesSent",
							finalCountPerUser.get (userId) [hour]));

				}

			}

			return statsDataSet;

		}

	}

}
