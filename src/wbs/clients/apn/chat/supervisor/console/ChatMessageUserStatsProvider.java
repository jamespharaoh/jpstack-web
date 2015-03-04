package wbs.clients.apn.chat.supervisor.console;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.clients.apn.chat.contact.model.ChatContactRec;
import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMessageSearch;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.reporting.console.StatsDataSet;
import wbs.platform.reporting.console.StatsDatum;
import wbs.platform.reporting.console.StatsGranularity;
import wbs.platform.reporting.console.StatsPeriod;
import wbs.platform.reporting.console.StatsProvider;

@SingletonComponent ("chatMessageUserStatsProvider")
public
class ChatMessageUserStatsProvider
	implements StatsProvider {

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	PrivChecker privChecker;

	@Override
	public
	StatsDataSet getStats (
			@NonNull StatsPeriod period,
			@NonNull Map<String,Object> conditions) {

		if (period.granularity () != StatsGranularity.hour)
			throw new IllegalArgumentException ();

		List<ChatRec> chats =
			new ArrayList<ChatRec> ();

		if (conditions.containsKey ("chatId")) {

			ChatRec chat =
				chatHelper.find (
					(Integer) conditions.get ("chatId"));

			if (chat == null)
				throw new RuntimeException ();

			chats.add (chat);

		} else {

			chats =
				chatHelper.findAll ();

		}

		StatsDataSet combinedStatsDataSet =
			new StatsDataSet ();

		Set<Object> userIds =
			new HashSet<Object> ();

		for (ChatRec chat : chats) {

			if (! privChecker.can (
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

		Map<Integer,int[]> countPerUser =
			new TreeMap<Integer,int[]> ();

		Map<Integer,int[]> charsPerUser =
			new TreeMap<Integer,int[]> ();

		Map<Integer,int[]> finalCountPerUser =
			new TreeMap<Integer,int[]> ();

		Set<Object> userIds =
			new HashSet<Object> ();

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

		for (ChatMessageRec chatMessage
				: chatMessages) {

			Instant chatMessageTimestamp =
				new Instant (chatMessage.getTimestamp ());

			int hour =
				period.assign (chatMessageTimestamp);

			// count outbound messages

			if (chatMessage.getSender () != null) {

				if (! userIds.contains (
						chatMessage.getSender ().getId ())) {

					userIds.add (
						chatMessage.getSender ().getId ());

					countPerUser.put (
						chatMessage.getSender ().getId (),
						new int [period.size ()]);

					charsPerUser.put (
						chatMessage.getSender ().getId (),
						new int [period.size ()]);

					finalCountPerUser.put (
						chatMessage.getSender ().getId (),
						new int [period.size ()]);

				}

				int[] userCounts =
					countPerUser.get (
						chatMessage.getSender ().getId ());

				userCounts [hour] ++;

				int[] userChars =
					charsPerUser.get (
						chatMessage.getSender ().getId ());

				int length =
					chatMessage.getOriginalText ().getText ().length ();

				userChars [hour] += length;

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

						|| inverseChatContact
							.getLastDeliveredMessageTime ()
							.getTime ()
						< chatMessage
							.getTimestamp ()
							.getTime ()

					)

				) {

					int[] finalCounts =
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

			for (Object userIdObject
					: userIds) {

				Integer userId =
					(Integer) userIdObject;

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
