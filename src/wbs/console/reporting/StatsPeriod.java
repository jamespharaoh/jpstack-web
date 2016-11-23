package wbs.console.reporting;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Instant;
import org.joda.time.Interval;

@Accessors (fluent = true)
@Data
public
class StatsPeriod {

	StatsGranularity granularity;

	Instant startTime;
	Instant endTime;

	Long offset;

	List <Instant> steps =
		new ArrayList<> ();

	public
	Interval toInterval () {

		return new Interval (
			startTime,
			endTime);

	}

	public
	Instant step (
			int index) {

		if (index < 0)
			throw new IllegalArgumentException ();

		if (index > steps.size ())
			throw new IllegalArgumentException ();

		if (index == steps.size ())
			return endTime;

		return steps.get (index);

	}

	public
	long size () {
		return steps.size ();
	}

	public
	int assign (
			Instant timestamp) {

		if (timestamp.isBefore (startTime)) {

			throw new IllegalArgumentException (
				stringFormat (
					"Timestamp %s is before start of period %s",
					timestamp.toString (),
					startTime.toString ()));

		}

		if (timestamp.isEqual (endTime)) {

			throw new IllegalArgumentException (
				stringFormat (
					"Timestamp %s is equal to end of period",
					timestamp.toString ()));

		}

		if (timestamp.isAfter (endTime)) {

			throw new IllegalArgumentException (
				stringFormat (
					"Timestamp %s is after end of period %s",
					timestamp.toString (),
					endTime.toString ()));

		}

		for (
			int step = 0;
			step < size ();
			step ++
		) {

			if (timestamp.isBefore (step (step + 1)))
				return step;

		}

		throw new RuntimeException (
			stringFormat (
				"Logic error %s %s %s",
				timestamp.toString (),
				startTime.toString (),
				endTime.toString ()));

	}

}
