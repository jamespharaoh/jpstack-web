package wbs.apn.chat.graphs.console;

import static wbs.framework.utils.etc.Misc.in;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import wbs.platform.console.html.ObsoleteMonthField;
import wbs.platform.graph.console.GraphScale;

public abstract
class MonthlyHistoGraphImageResponder
	extends GraphImageResponder {

	protected
	List<Integer> values =
		new ArrayList<Integer>();

	protected
	Date minTime;

	protected
	Date maxTime;

	public
	MonthlyHistoGraphImageResponder (
			int newImageWidth,
			int newImageHeight,
			int newBorderSize) {

		super (
			newImageWidth,
			newImageHeight,
			newBorderSize);

	}

	protected abstract
	void prepareData (
			Date minTime,
			Date maxTime);

	@Override
	protected
	void prepareData () {

		ObsoleteMonthField monthField =
			ObsoleteMonthField.parse (requestContext.parameter ("month"));

		if (monthField.date == null)
			throw new RuntimeException ();

		minTime =
			monthField.date;

		Calendar calendar =
			Calendar.getInstance();

		calendar.setTime (
			monthField.date);

		int month =
			calendar.get (Calendar.MONTH);

		while (calendar.get (Calendar.MONTH) == month) {

			values.add (0);

			calendar.add (
				Calendar.DATE,
				1);

		}

		maxTime =
			calendar.getTime ();

		prepareData (
			minTime,
			maxTime);

	}

	@Override
	protected
	void prepareVerticalScale () {

		int maxValue = 0;

		for (Integer value : values)
			if (value > maxValue)
				maxValue = value;

		verticalScale =
			GraphScale.setScale (
				maxValue,
				0);

	}

	@Override
	protected
	void prepareImageShadingVertical () {

		int space =
			(int) (plotWidth / 31 / 10);

		// draw dates

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (minTime);

		for (int i = 0; i < values.size (); i++) {

			graphics.setColor (
				in (calendar.get (Calendar.DAY_OF_WEEK),
						Calendar.SUNDAY,
						Calendar.SATURDAY)
					? Color.red
					: Color.black);

			int x1 =
				+ xBound (i)
				+ space
				+ 1;

			int x2 =
				+ xBound (i + 1)
				- space + 1;

			String string =
				Integer.toString (i + 1);

			int x =
				(
					+ x2
					+ x1
					- fontMetrics.stringWidth (string)
				) / 2;

			graphics.drawString (
				string,
				x,
				yOrigin + fontMetrics.getAscent ());

			calendar.add (
				Calendar.DATE,
				1);

		}

		// draw weekends

		graphics.setColor (
			new Color (1.0F, 0.0F, 0.0F, 0.15F));

		calendar.setTime (
			minTime);

		for (int i = 0; i < values.size (); i++) {

			if (in (
					calendar.get (Calendar.DAY_OF_WEEK),
					Calendar.SUNDAY,
					Calendar.SATURDAY)) {

				int x1 =
					xBound (i);

				int x2 =
					xBound (i + 1);

				Rectangle2D rect =
					new Rectangle2D.Float (
						x1,
						yOrigin - plotHeight,
						x2 - x1,
						plotHeight);

				graphics.fill (
					rect);

			}

			calendar.add (
				Calendar.DATE,
				1);

		}

	}

	private
	int xBound (
			int index) {

		return
			+ xOrigin
			+ (int) (plotWidth * index / values.size ());

	}

	@Override
	protected
	void prepareImageData () {

		int space =
			(int) (plotWidth / 31 / 10);

		graphics.setColor (
			new Color (0.0F, 0.0F, 0.7F, 0.7F));

		for (int i = 0; i < values.size (); i++) {

			int value =
				values.get (i);

			int x1 =
				+ xBound (i)
				+ space + 1;

			int x2 =
				+ xBound (i + 1)
				- space + 1;

			int height = 1
				* plotHeight
				* value
				* verticalScale.getMultiplier ()
				/ verticalScale.getStepSize ()
				/ verticalScale.getNumSteps ();

			Rectangle2D rect =
				new Rectangle2D.Float (
					x1,
					yOrigin - height,
					x2 - x1,
					height);

			graphics.fill (
				rect);

		}

	}

}
