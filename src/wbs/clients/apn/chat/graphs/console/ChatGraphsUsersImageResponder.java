package wbs.clients.apn.chat.graphs.console;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.core.model.ChatStatsObjectHelper;
import wbs.clients.apn.chat.core.model.ChatStatsRec;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.TimeFormatter;
import wbs.platform.graph.console.GraphScale;

@PrototypeComponent ("chatGraphsUsersImageResponder")
public
class ChatGraphsUsersImageResponder
	extends GraphImageResponder {

	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatStatsObjectHelper chatStatsHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

	// state

	ChatRec chat;

	List<ChatStatsRec> allChatStats;

	LocalDate date;
	DateTimeZone timezone;
	Instant minTime;
	Instant maxTime;

	// implementation

	public
	ChatGraphsUsersImageResponder () {

		super (
			640,
			320,
			10);

	}

	@Override
	protected
	void prepareData () {

		chat =
			chatHelper.find (
				requestContext.stuffInt (
					"chatId"));

		timezone =
			chatMiscLogic.timezone (
				chat);

		try {

			date =
				timeFormatter
					.dateStringToLocalDateRequired (
						requestContext.parameter ("date"));

			minTime =
				date
					.toDateTimeAtStartOfDay (timezone)
					.toInstant ();

			maxTime =
				date
					.plusDays (1)
					.toDateTimeAtStartOfDay (timezone)
					.toInstant ();

		} catch (Exception exception) {

			throw new RuntimeException (
				"Invalid date");

		}

		allChatStats =
			chatStatsHelper.findByTimestamp (
				chat,
				new Interval (
					minTime,
					maxTime));

		Collections.sort (
			allChatStats);

	}

	@Override
	protected
	void prepareVerticalScale () {

		int realMax = 0;

		for (
			ChatStatsRec cs
				: allChatStats
		) {

			if (cs.getNumUsers() > realMax) {

				realMax =
					(int) (long)
					cs.getNumUsers ();

			}

		}

		verticalScale =
			GraphScale.setScale (realMax, 0);

	}

	@Override
	protected
	void prepareImageShadingVertical () {

		for (
			int index = 1;
			index <= 8;
			index ++
		) {

			LocalTime time =
				new LocalTime (
					index * 3,
					0,
					0);

			Instant instant =
				date
					.toDateTime (
						time,
						timezone)
					.toInstant ();

			int x =
				+ xOrigin
				+ (int) (1
					* (double) plotWidth
					* (double) (instant.getMillis () - minTime.getMillis ())
					/ (double) (maxTime.getMillis () - minTime.getMillis ()));

			graphics.setColor (
				new Color (192, 192, 192));

			graphics.drawLine (
				x, yOrigin - plotHeight,
				x, yOrigin);

			graphics.setColor (Color.black);

			String string;

			switch (index) {

			case 1:
			case 5:
				string = "3";
				break;

			case 2:
			case 6:
				string = "6";
				break;

			case 3:
			case 7:
				string = "9";
				break;

			case 4:
			case 8:
				string = "12";
				break;

			default:
				string = "";

			}

			graphics.drawString (
				string,
				x - fontMetrics.stringWidth (string) / 2,
				yOrigin + fontMetrics.getAscent ());

		}

	}

	@Override
	protected
	void prepareImageData () {

		int[] xPoints =
			new int [
				allChatStats.size ()];


		int[] yPoints =
			new int [
				allChatStats.size ()];

		int index = 0;

		for (
			ChatStatsRec chatStats
				: allChatStats
		) {

			xPoints [index] =
				+ xOrigin
				+ (int) (1
					* (double) plotWidth
					* (double) (
						+ chatStats.getTimestamp ().getMillis ()
						- minTime.getMillis ())
					/ (double) (
						+ maxTime.getMillis ()
						- minTime.getMillis ()));

			yPoints [index] =
				+ yOrigin
				- (int) (1
					* chatStats.getNumUsers ()
					* plotHeight
					* verticalScale.getMultiplier ()
					/ verticalScale.getStepSize ()
					/ verticalScale.getNumSteps ()
				);

			index ++;

		}

		graphics.setColor (
			Color.blue);

		graphics.drawPolyline (
			xPoints,
			yPoints,
			allChatStats.size ());

	}

}
