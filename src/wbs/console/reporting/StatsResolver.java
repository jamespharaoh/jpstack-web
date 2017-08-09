package wbs.console.reporting;

import java.util.Map;
import java.util.Set;

public
interface StatsResolver {

	Set<Object> getGroups (
			Map<String,StatsDataSet> dataSetsByName,
			StatsGrouper grouper);

	ResolvedStats resolve (
			Map<String,StatsDataSet> dataSetsByName,
			StatsPeriod period,
			Set<Object> groups);

}
