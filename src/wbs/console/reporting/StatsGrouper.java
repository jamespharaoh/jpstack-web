package wbs.console.reporting;

import java.util.List;
import java.util.Set;

import wbs.utils.string.FormatWriter;

public
interface StatsGrouper {

	Set <Object> getGroups (
			StatsDataSet dataSet);

	List <Object> sortGroups (
			Set <Object> groups);

	void writeTdForGroup (
			FormatWriter formatWriter,
			Object group);

}
