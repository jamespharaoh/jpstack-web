package wbs.console.reporting;

import java.util.Map;
import java.util.Set;

import wbs.framework.database.Transaction;

public
interface StatsProvider {

	StatsDataSet getStats (
			Transaction parentTransaction,
			StatsPeriod period,
			Map <String, Set <String>> conditions);

}
