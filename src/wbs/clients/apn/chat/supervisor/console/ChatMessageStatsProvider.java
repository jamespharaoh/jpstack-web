package wbs.clients.apn.chat.supervisor.console;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMessageSearch;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsDatum;
import wbs.console.reporting.StatsGranularity;
import wbs.console.reporting.StatsPeriod;
import wbs.console.reporting.StatsProvider;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;

@SingletonComponent ("chatMessageStatsProvider")
public
class ChatMessageStatsProvider
	implements StatsProvider {

	// singleton dependencies

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	// implementation

	@Override
	public
	StatsDataSet getStats (
			@NonNull StatsPeriod period,
			@NonNull Map <String, Object> conditions) {

		if (period.granularity () != StatsGranularity.hour)
			throw new IllegalArgumentException ();

		if (! conditions.containsKey ("chatId"))
			throw new IllegalArgumentException ();

		// setup data structures

		long[] receivedTotal =
			new long [
				period.size ()];

		long[] sentTotal =
			new long [
				period.size ()];

		long[] missedTotal =
			new long [
				period.size ()];

		long[] charsTotal =
			new long [
				period.size ()];

		// retrieve messages

		List<ChatMessageRec> chatMessages =
			chatMessageHelper.search (
				new ChatMessageSearch ()

			.chatId (
				(Long)
				conditions.get (
					"chatId"))

			.timestampAfter (
				period.startTime ())

			.timestampBefore (
				period.endTime ())

		);

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
					conditions.get (
						"chatId"))

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
