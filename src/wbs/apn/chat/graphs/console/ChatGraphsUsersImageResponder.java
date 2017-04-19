package wbs.apn.chat.graphs.console;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.platform.graph.console.GraphScale;

import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.console.ChatStatsConsoleHelper;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.core.model.ChatStatsRec;

@PrototypeComponent ("chatGraphsUsersImageResponder")
public
class ChatGraphsUsersImageResponder
	extends GraphImageResponder {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatStatsConsoleHelper chatStatsHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatRec chat;

	List <ChatStatsRec> allChatStats;

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
	void prepareData (
			@NonNull TaskLogger parentTaskLogger) {

		chat =
			chatHelper.findFromContextRequired ();

		timezone =
			chatMiscLogic.timezone (
				chat);

		try {

			date =
				timeFormatter

				.dateStringToLocalDateRequired (
					requestContext.parameterRequired (
						"date"));

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

		long realMax = 0;

		for (
			ChatStatsRec chatStats
				: allChatStats
		) {

			if (chatStats.getNumUsers() > realMax) {

				realMax =
					chatStats.getNumUsers ();

			}

		}

		verticalScale =
			GraphScale.setScale (
				realMax,
				0);

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
				+ toJavaIntegerRequired (1l
					* plotWidth
					* (instant.getMillis () - minTime.getMillis ())
					/ (maxTime.getMillis () - minTime.getMillis ()));

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
				+ toJavaIntegerRequired (1l
					* plotWidth
					* (
						+ chatStats.getTimestamp ().getMillis ()
						- minTime.getMillis ())
					/ (
						+ maxTime.getMillis ()
						- minTime.getMillis ()));

			yPoints [index] =
				+ yOrigin
				- toJavaIntegerRequired (1l
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
