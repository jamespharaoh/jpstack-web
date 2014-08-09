package wbs.platform.reporting.console;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Instant;

public
interface StatsResolver {

	Set<Object> getGroups (
			Map<String,StatsDataSet> dataSetsByName,
			StatsGrouper grouper);

	Map<Pair<Object,Instant>,Object> resolve (
			Map<String,StatsDataSet> dataSetsByName,
			StatsPeriod period,
			Set<Object> groups);

}
