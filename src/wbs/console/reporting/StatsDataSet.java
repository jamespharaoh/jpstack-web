package wbs.console.reporting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
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
			Set <Object> indexValues) {

		this.indexValues.put (
			key,
			indexValues);

		return this;

	}

}
