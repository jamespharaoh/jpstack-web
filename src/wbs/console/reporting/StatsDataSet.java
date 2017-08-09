package wbs.console.reporting;

import static wbs.utils.collection.MapUtils.mapItemForKeyOrElseSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class StatsDataSet {

	Map <String, Set <Object>> indexValues =
		new LinkedHashMap<> ();

	List <StatsDatum> data =
		new ArrayList<> ();

	public
	StatsDataSet addIndexValues (
			String key,
			Set <Object> newIndexValues) {

		Set <Object> allIndexValues =
			mapItemForKeyOrElseSet (
				indexValues,
				key,
				() -> new HashSet<> ());

		allIndexValues.addAll (
			newIndexValues);

		return this;

	}

	public static
	StatsDataSet combine (
			@NonNull Iterable <StatsDataSet> inputDataSets) {

		StatsDataSet combinedDataSet =
			new StatsDataSet ();


		for (
			StatsDataSet inputDataSet
				: inputDataSets) {

			for (
				Map.Entry <String, Set <Object>> inputIndexEntry
					: inputDataSet.indexValues ().entrySet ()
			) {

				combinedDataSet.addIndexValues (
					inputIndexEntry.getKey (),
					inputIndexEntry.getValue ());

			}

			combinedDataSet.data ().addAll (
				inputDataSet.data ());

		}

		return combinedDataSet;

	}

}
