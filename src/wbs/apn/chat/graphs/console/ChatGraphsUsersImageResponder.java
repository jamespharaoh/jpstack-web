package wbs.apn.chat.graphs.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.Interval;

import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.core.model.ChatStatsObjectHelper;
import wbs.apn.chat.core.model.ChatStatsRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.graph.console.GraphScale;

@PrototypeComponent ("chatGraphsUsersImageResponder")
public
class ChatGraphsUsersImageResponder
	extends GraphImageResponder {

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatStatsObjectHelper chatStatsHelper;

	@Inject
	ConsoleRequestContext requestContext;

	List<ChatStatsRec> allChatStats;

	long minTime, maxTime;

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

		SimpleDateFormat dateFormat =
			new SimpleDateFormat ("yyyy-MM-dd");

		Date date1;

		try {

			date1 =
				dateFormat.parse (
					requestContext.parameter ("date"));

		} catch (ParseException e) {

			throw new RuntimeException ("Invalid date");

		}

		minTime = date1.getTime ();
		Calendar cal = Calendar.getInstance ();
		cal.setTime (date1);
		cal.add (Calendar.DATE, 1);
		Date date2 = cal.getTime ();
		maxTime = date2.getTime ();

		ChatRec chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		allChatStats =
			chatStatsHelper.findByTimestamp (
				chat,
				new Interval (
					dateToInstant (date1),
					dateToInstant (date2)));

		Collections.sort (allChatStats);

	}

	@Override
	protected
	void prepareVerticalScale () {

		int realMax = 0;

		for (ChatStatsRec cs : allChatStats) {

			if (cs.getNumUsers() > realMax)
				realMax = cs.getNumUsers ();

		}

		verticalScale =
			GraphScale.setScale (realMax, 0);

	}

	@Override
	protected
	void prepareImageShadingVertical () {

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			new Date (minTime));

		for (int i = 1; i <= 8; i ++) {

			calendar.set (
				Calendar.HOUR_OF_DAY,
				i * 3);

			Date date = calendar.getTime ();

			int x =
				+ xOrigin
				+ (int) (1
					* (double) plotWidth
					* (double) (date.getTime() - minTime)
					/ (double) (maxTime - minTime));

			graphics.setColor (
				new Color (192, 192, 192));

			graphics.drawLine (
				x, yOrigin - plotHeight,
				x, yOrigin);

			graphics.setColor (Color.black);

			String string;

			switch (i) {

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
			new int [allChatStats.size ()];

		int[] yPoints =
			new int [allChatStats.size ()];

		int index = 0;

		for (ChatStatsRec chatStats
				: allChatStats) {

			xPoints [index] =
				+ xOrigin
				+ (int) (1
					* (double) plotWidth
					* (double) (chatStats.getTimestamp ().getTime () - minTime)
					/ (double) (maxTime - minTime)
				);

			yPoints [index] =
				+ yOrigin
				- (1
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
