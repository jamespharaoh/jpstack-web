package wbs.console.reporting;

import static wbs.utils.string.StringUtils.stringFormatObsolete;

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
				stringFormatObsolete (
					"Timestamp %s is before start of period %s",
					timestamp,
					startTime));

		}

		if (timestamp.isEqual (endTime)) {

			throw new IllegalArgumentException (
				stringFormatObsolete (
					"Timestamp %s is equal to end of period",
					timestamp));

		}

		if (timestamp.isAfter (endTime)) {

			throw new IllegalArgumentException (
				stringFormatObsolete (
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
			stringFormatObsolete (
				"Logic error %s %s %s",
				timestamp,
				startTime,
				endTime));

	}

}
