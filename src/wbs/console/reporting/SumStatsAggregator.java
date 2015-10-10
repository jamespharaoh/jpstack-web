package wbs.console.reporting;

import java.util.List;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("sumStatsAggregator")
public
class SumStatsAggregator
	implements StatsAggregator {

	@Override
	public
	Object aggregate (
			List<Object> values) {

		int sum = 0;

		for (Object value
				: values) {

			sum +=
				(Integer) value;

		}

		return sum;

	}

}
