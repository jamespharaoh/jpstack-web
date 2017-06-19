package wbs.console.reporting;

import static wbs.utils.collection.MapUtils.mapItemForKeyOrThrow;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import org.joda.time.Instant;

import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("simpleStatsResolver")
public
class SimpleStatsResolver
	implements StatsResolver {

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String dataSetName;

	@Getter @Setter
	String indexName;

	@Getter @Setter
	String valueName;

	@Getter @Setter
	StatsAggregator aggregator;

	// public implementation

	@Override
	public
	Set <Object> getGroups (
			@NonNull Map <String, StatsDataSet> dataSetsByName,
			@NonNull StatsGrouper grouper) {

		StatsDataSet dataSet =
			mapItemForKeyOrThrow (
				dataSetsByName,
				dataSetName,
				() -> new RuntimeException (
					stringFormat (
						"Data set \"%s\" not provided ",
						dataSetName,
						"in simple stats resolver \"%s\"",
						name)));

		return grouper.getGroups (
			dataSet);

	}

	@Override
	public
	ResolvedStats resolve (
			Map <String, StatsDataSet> dataSetsByName,
			StatsPeriod period,
			Set <Object> groups) {

		StatsDataSet dataSet =
			mapItemForKeyRequired (
				dataSetsByName,
				dataSetName);

		Map <Pair <Object, Instant>, List <Object>> unaggregatedSteps =
			new HashMap<> ();

		Map <Object, List <Object>> unaggregatedTotals =
			new HashMap<> ();

		for (Object group : groups) {

			for (Instant step : period.steps ()) {

				unaggregatedSteps.put (
					Pair.of (
						group,
						step),
					new ArrayList<> ());

			}

			unaggregatedTotals.put (
				group,
				new ArrayList<> ());

		}

		for (
			StatsDatum datum
				: dataSet.data ()
		) {

			Object indexValue =
				indexName != null
					? datum.indexes ().get (indexName)
					: StatsDatum.UNARY;

			Pair <Object, Instant> key =
				Pair.of (
					indexValue,
					datum.startTime ());

			List <Object> stepValues =
				unaggregatedSteps.get (key);

			if (stepValues == null) {

				throw new RuntimeException (
					stringFormat (
						"Unexpected data for index \"%s\" ",
						indexValue.toString (),
						"start time \"%s\" ",
						datum.startTime ().toString (),
						"in simple stats resolver \"%s\"",
						name));

			}

			List <Object> totalValues =
				unaggregatedTotals.get (
					indexValue);

			if (totalValues == null) {

				throw new RuntimeException (
					stringFormat (
						"Unexpected data for index \"%s\" ",
						indexValue.toString (),
						"in simple stats resolver \"%s\"",
						name));

			}

			Object value =
				datum.values ().get (valueName);

			if (value == null) {

				throw new RuntimeException (
					stringFormat (
						"Value \"%s\" not found ",
						valueName,
						"in simple stats resolver \"%s\"",
						name));

			}

			stepValues.add (
				value);

			totalValues.add (
				value);

		}

		// perform aggregation

		ResolvedStats ret =
			new ResolvedStats ();

		for (
			Map.Entry <Pair <Object, Instant>, List <Object>> entry
				: unaggregatedSteps.entrySet ()
		) {

			ret.steps ().put (
				entry.getKey (),
				aggregator.aggregate (
					entry.getValue ()));

		}

		for (
			Map.Entry <Object, List <Object>> entry
				: unaggregatedTotals.entrySet ()
		) {

			ret.totals ().put (
				entry.getKey (),
				aggregator.aggregate (
					entry.getValue ()));

		}

		// return

		return ret;

	}

}
