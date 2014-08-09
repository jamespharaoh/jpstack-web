package wbs.platform.reporting.console;

import java.util.List;

public
interface StatsAggregator {

	Object aggregate (
			List<Object> values);

}
