package wbs.console.reporting;

import java.util.Map;

public
interface StatsProvider {

	StatsDataSet getStats (
			StatsPeriod period,
			Map<String,Object> conditions);

}
