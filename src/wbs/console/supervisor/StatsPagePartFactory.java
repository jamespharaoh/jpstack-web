package wbs.console.supervisor;

import java.util.Map;

import wbs.console.part.PagePart;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsPeriod;

import wbs.framework.database.Transaction;

public
interface StatsPagePartFactory {

	PagePart buildPagePart (
			Transaction parentTransaction,
			StatsPeriod period,
			Map <String, StatsDataSet> statsDataSets);

}
