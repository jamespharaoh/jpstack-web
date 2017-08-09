package wbs.console.reporting;

import java.util.List;
import java.util.Set;

import wbs.framework.database.Transaction;

import wbs.utils.string.FormatWriter;

public
interface StatsGrouper {

	Set <Object> getGroups (
			StatsDataSet dataSet);

	List <Object> sortGroups (
			Transaction parentTransaction,
			Set <Object> groups);

	void writeTdForGroup (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Object group);

}
