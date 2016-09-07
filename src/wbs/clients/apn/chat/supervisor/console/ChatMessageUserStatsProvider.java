package wbs.clients.apn.chat.supervisor.console;

import static wbs.framework.utils.etc.TimeUtils.earlierThan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.clients.apn.chat.contact.model.ChatContactRec;
import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMessageSearch;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.console.priv.UserPrivChecker;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsDatum;
import wbs.console.reporting.StatsGranularity;
import wbs.console.reporting.StatsPeriod;
import wbs.console.reporting.StatsProvider;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;

@SingletonComponent ("chatMessageUserStatsProvider")
public
class ChatMessageUserStatsProvider
	implements StatsProvider {

	// singleton dependencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// implementation

	@Override
	public
	StatsDataSet getStats (
			@NonNull StatsPeriod period,
			@NonNull Map <String, Object> conditions) {

		if (period.granularity () != StatsGranularity.hour)
			throw new IllegalArgumentException ();

		List <ChatRec> chats =
			new ArrayList<> ();

		if (conditions.containsKey ("chatId")) {

			ChatRec chat =
				chatHelper.findRequired (
					(Long)
					conditions.get (
						"chatId"));

			chats.add (
				chat);

		} else {

			chats =
				chatHelper.findAll ();

		}

		StatsDataSet combinedStatsDataSet =
			new StatsDataSet ();

		Set<Object> userIds =
			new HashSet<Object> ();

		for (ChatRec chat : chats) {

			if (! privChecker.canRecursive (
					chat,
					"supervisor"))
				continue;

			StatsDataSet singleStatsDataSet =
				getStatsForChat (
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

	StatsDataSet getStatsForChat (
			@NonNull StatsPeriod period,
			@NonNull ChatRec chat) {

		// setup data structures

		Map<Long,long[]> countPerUser =
			new TreeMap<> ();

		Map<Long,long[]> charsPerUser =
			new TreeMap<> ();

		Map<Long,long[]> finalCountPerUser =
			new TreeMap<> ();

		Set<Object> userIds =
			new HashSet<> ();

		// retrieve messages

		ChatMessageSearch chatMessageSearch =
			new ChatMessageSearch ();

		chatMessageSearch
			.chatId (chat.getId ())
			.timestampAfter (period.startTime ())
			.timestampBefore (period.endTime ());

		List<ChatMessageRec> chatMessages =
			chatMessageHelper.search (
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
							period.size ()]);

					charsPerUser.put (
						chatMessage.getSender ().getId (),
						new long [
							period.size ()]);

					finalCountPerUser.put (
						chatMessage.getSender ().getId (),
						new long [
							period.size ()]);

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
