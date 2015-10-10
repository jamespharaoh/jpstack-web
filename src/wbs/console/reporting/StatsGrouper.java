package wbs.console.reporting;

import java.util.List;
import java.util.Set;

public
interface StatsGrouper {

	Set<Object> getGroups (
			StatsDataSet dataSet);

	List<Object> sortGroups (
			Set<Object> groups);

	String tdForGroup (
			Object group);

}
