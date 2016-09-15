package wbs.apn.chat.graphs.console;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NumberUtils.integerInSafe;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.graph.console.GraphScale;

public abstract
class MonthlyHistoGraphImageResponder
	extends GraphImageResponder {

	// dependencies

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	// state

	protected
	List <Integer> values =
		new ArrayList<> ();

	protected
	DateTimeZone timezone;

	protected
	YearMonth yearMonth;

	protected
	Instant minTime;

	protected
	Instant maxTime;

	// implementation

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
			Instant minTime,
			Instant maxTime);

	protected abstract
	DateTimeZone timezone ();

	@Override
	protected
	void prepareData () {

		timezone =
			timezone ();

		yearMonth =
			YearMonth.parse (
				requestContext.parameterRequired (
					"month"));

		minTime =
			yearMonth
				.toLocalDate (1)
				.toDateTimeAtStartOfDay (timezone)
				.toInstant ();

		maxTime =
			yearMonth
				.plusMonths (1)
				.toLocalDate (1)
				.toDateTimeAtStartOfDay (timezone)
				.toInstant ();

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
			plotWidth
				/ 31
				/ 10;

		// draw dates

		for (
			int day = 0;
			day < values.size ();
			day ++
		) {

			LocalDate date =
				yearMonth.toLocalDate (
					day + 1);

			graphics.setColor (
				ifThenElse (
					integerInSafe (
						date.getDayOfWeek (),
						DateTimeConstants.SATURDAY,
						DateTimeConstants.SUNDAY),
					() -> Color.red,
					() -> Color.black));

			int x1 =
				+ xBound (day)
				+ space
				+ 1;

			int x2 =
				+ xBound (day + 1)
				- space + 1;

			String string =
				Integer.toString (day + 1);

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

		}

		// draw weekends

		graphics.setColor (
			new Color (1.0F, 0.0F, 0.0F, 0.15F));

		for (
			int day = 0;
			day < values.size ();
			day ++
		) {

			LocalDate date =
				yearMonth.toLocalDate (
					day + 1);

			if (
				integerInSafe (
					date.getDayOfWeek (),
					DateTimeConstants.SATURDAY,
					DateTimeConstants.SUNDAY)
			) {

				int x1 =
					xBound (day);

				int x2 =
					xBound (day + 1);

				Rectangle2D rect =
					new Rectangle2D.Float (
						x1,
						yOrigin - plotHeight,
						x2 - x1,
						plotHeight);

				graphics.fill (
					rect);

			}

		}

	}

	private
	int xBound (
			int index) {

		return
			+ xOrigin
			+ plotWidth
				* index
				/ values.size ();

	}

	@Override
	protected
	void prepareImageData () {

		int space =
			plotWidth
				/ 31
				/ 10;

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

			int height =
				toJavaIntegerRequired (1
					* plotHeight
					* value
					* verticalScale.getMultiplier ()
					/ verticalScale.getStepSize ()
					/ verticalScale.getNumSteps ());

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
