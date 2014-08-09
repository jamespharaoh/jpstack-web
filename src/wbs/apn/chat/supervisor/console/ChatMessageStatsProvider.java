package wbs.apn.chat.supervisor.console;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageSearch;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.reporting.console.StatsDataSet;
import wbs.platform.reporting.console.StatsDatum;
import wbs.platform.reporting.console.StatsGranularity;
import wbs.platform.reporting.console.StatsPeriod;
import wbs.platform.reporting.console.StatsProvider;

@SingletonComponent ("chatMessageStatsProvider")
public
class ChatMessageStatsProvider
	implements StatsProvider {

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Override
	public
	StatsDataSet getStats (
			@NonNull StatsPeriod period,
			@NonNull Map<String,Object> conditions) {

		if (period.granularity () != StatsGranularity.hour)
			throw new IllegalArgumentException ();

		if (! conditions.containsKey ("chatId"))
			throw new IllegalArgumentException ();

		// setup data structures

		int[] receivedTotal =
			new int [period.size ()];

		int[] sentTotal =
			new int [period.size ()];

		int[] missedTotal =
			new int [period.size ()];

		int[] charsTotal =
			new int [period.size ()];

		// retrieve messages

		List<ChatMessageRec> chatMessages =
			chatMessageHelper.search (
				new ChatMessageSearch ()
					.chatId ((Integer) conditions.get ("chatId"))
					.timestampAfter (period.startTime ())
					.timestampBefore (period.endTime ()));

		// aggregate stats

		for (ChatMessageRec chatMessage
				: chatMessages) {

			Instant chatMessageTimestamp =
				new Instant (chatMessage.getTimestamp ());

			int hour =
				period.assign (chatMessageTimestamp);

			int length =
				chatMessage.getOriginalText ().getText ().length ();

			if (chatMessage.getSender () != null) {

				sentTotal [hour] ++;
				missedTotal [hour] --;
				charsTotal [hour] += length;

			}

			if (chatMessage.getToUser ().getType () == ChatUserType.monitor) {

				receivedTotal [hour]++;
				missedTotal [hour]++;

			}

		}

		// create return value

		StatsDataSet dataSet =
			new StatsDataSet ();

		for (
			int hour = 0;
			hour < period.size ();
			hour ++
		) {

			dataSet.data ().add (
				new StatsDatum ()

				.startTime (
					period.step (hour))

				.addIndex (
					"chatId",
					conditions.get ("chatId"))

				.addValue (
					"messagesReceived",
					receivedTotal [hour])

				.addValue (
					"messagesSent",
					sentTotal [hour])

				.addValue (
					"messagesMissed",
					missedTotal [hour])

				.addValue (
					"charactersSent",
					charsTotal [hour]));

		}

		return dataSet;

	}

}
