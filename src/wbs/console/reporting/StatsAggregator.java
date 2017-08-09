package wbs.console.reporting;

import java.util.List;

public
interface StatsAggregator {

	Object aggregate (
			List<Object> values);

}
