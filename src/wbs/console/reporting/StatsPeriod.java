package wbs.console.reporting;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Instant;
import org.joda.time.Interval;

import lombok.Data;
import lombok.experimental.Accessors;

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
	int size () {
		return steps.size ();
	}

	public
	int assign (
			Instant timestamp) {

		if (timestamp.isBefore (startTime)) {

			throw new IllegalArgumentException (
				stringFormat (
					"Timestamp %s is before start of period %s",
					timestamp,
					startTime));

		}

		if (timestamp.isEqual (endTime)) {

			throw new IllegalArgumentException (
				stringFormat (
					"Timestamp %s is equal to end of period",
					timestamp));

		}

		if (timestamp.isAfter (endTime)) {

			throw new IllegalArgumentException (
				stringFormat (
					"Timestamp %s is after end of period %s",
					timestamp,
					endTime));

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
				timestamp,
				startTime,
				endTime));

	}

}
