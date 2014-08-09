package wbs.platform.reporting.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("simpleStatsResolver")
public
class SimpleStatsResolver
	implements StatsResolver {

	@Getter @Setter
	String dataSetName;

	@Getter @Setter
	String indexName;

	@Getter @Setter
	String valueName;

	@Getter @Setter
	StatsAggregator aggregator;

	@Override
	public
	Set<Object> getGroups (
			Map<String,StatsDataSet> dataSetsByName,
			StatsGrouper grouper) {

		StatsDataSet dataSet =
			dataSetsByName.get (dataSetName);

		if (dataSet == null) {

			throw new RuntimeException (
				stringFormat (
					"Data set %s not provided",
					dataSetName));

		}

		return grouper.getGroups (
			dataSet);

	}

	@Override
	public
	Map<Pair<Object,Instant>,Object> resolve (
			Map<String,StatsDataSet> dataSetsByName,
			StatsPeriod period,
			Set<Object> groups) {

		StatsDataSet dataSet =
			dataSetsByName.get (dataSetName);

		if (dataSet == null)
			throw new RuntimeException ();

		Map<Pair<Object,Instant>,List<Object>> unaggregatedValues =
			new HashMap<Pair<Object,Instant>,List<Object>> ();

		for (Object group : groups) {

			for (Instant step : period.steps ()) {

				unaggregatedValues.put (
					new ImmutablePair<Object,Instant> (
						group,
						step),
					new ArrayList<Object> ());

			}

		}

		for (StatsDatum datum
				: dataSet.data ()) {

			Object indexValue =
				indexName != null
					? datum.indexes ().get (indexName)
					: StatsDatum.UNARY;

			Pair<Object,Instant> key =
				Pair.<Object,Instant>of (
					indexValue,
					datum.startTime ());

			List<Object> values =
				unaggregatedValues.get (key);

			if (values == null) {

				throw new RuntimeException (
					stringFormat (
						"Unexpected data for %s/%s",
						indexValue,
						datum.startTime ()));

			}

			Object value =
				datum.values ().get (valueName);

			if (value == null) {

				throw new RuntimeException (
					stringFormat (
						"Value %s not found",
						valueName));

			}

			values.add (
				value);

		}

		// perform aggregation

		Map<Pair<Object,Instant>,Object> aggregatedValues =
			new HashMap<Pair<Object,Instant>,Object> ();

		for (Map.Entry<Pair<Object,Instant>,List<Object>> entry
				: unaggregatedValues.entrySet ()) {

			aggregatedValues.put (
				entry.getKey (),
				aggregator.aggregate (entry.getValue ()));

		}

		// return

		return aggregatedValues;

	}

}
