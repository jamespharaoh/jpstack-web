package wbs.console.reporting;

import java.util.Map;
import java.util.Set;

import org.joda.time.Interval;

import wbs.framework.database.Transaction;

public
interface StatsProvider {

	void prepare (
			Transaction parentTransaction,
			Map <String, Set <String>> conditions);

	StatsDataSet getStats (
			Transaction parentTransaction,
			Interval interval);

}
