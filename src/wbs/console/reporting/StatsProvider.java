package wbs.console.reporting;

import java.util.Map;

import wbs.framework.logging.TaskLogger;

public
interface StatsProvider {

	StatsDataSet getStats (
			TaskLogger parentTaskLogger,
			StatsPeriod period,
			Map <String, Object> conditions);

}
