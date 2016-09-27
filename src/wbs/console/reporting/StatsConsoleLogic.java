package wbs.console.reporting;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.ImmutablePair;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.utils.string.FormatWriter;

@SingletonComponent ("statsConsoleLogic")
public
class StatsConsoleLogic {

	public
	void writeGroup (
			@NonNull FormatWriter formatWriter,
			@NonNull Map <String, StatsDataSet> dataSetsByName,
			@NonNull StatsPeriod period,
			@NonNull StatsGrouper grouper,
			@NonNull StatsResolver resolver,
			@NonNull StatsFormatter formatter) {

		// aggregate stats via resolver

		Set <Object> groups =
			resolver.getGroups (
				dataSetsByName,
				grouper);

		ResolvedStats resolved =
			resolver.resolve (
				dataSetsByName,
				period,
				groups);

		List <Object> sortedGroups =
			grouper.sortGroups (
				groups);

		// output

		for (
			Object group
				: sortedGroups
		) {

			htmlTableRowOpen ();

			grouper.writeTdForGroup (
				formatWriter,
				group);

			for (
				int step = 0;
				step < period.size ();
				step ++
			) {

				Object combinedValue =
					resolved.steps ().get (
						new ImmutablePair<Object,Instant> (
							group,
							period.step (step)));

				formatter.format (
					formatWriter,
					group,
					period,
					step,
					optionalFromNullable (
						combinedValue));

			}

			Object totalValue =
				resolved.totals ().get (
					group);

			formatter.formatTotal (
				formatWriter,
				group,
				Optional.fromNullable (
					totalValue));

			htmlTableRowClose ();

		}

	}

	public
	StatsPeriod createStatsPeriod (
			@NonNull StatsGranularity granularity,
			@NonNull ReadableInstant startTime,
			@NonNull ReadableInstant endTime,
			@NonNull Long offset) {

		StatsPeriod ret =
			new StatsPeriod ()

			.granularity (
				granularity)

			.startTime (
				startTime.toInstant ())

			.endTime (
				endTime.toInstant ())

			.offset (
				offset);

		for (

			DateTime hour =
				startTime.toInstant ().toDateTime ();

			hour.isBefore (
				endTime);

			hour =
				hour.plusHours (1)

		) {

			ret.steps ().add (
				hour.toInstant ());

		}

		return ret;

	}

}
