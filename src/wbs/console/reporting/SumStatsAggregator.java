package wbs.console.reporting;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("sumStatsAggregator")
public
class SumStatsAggregator
	implements StatsAggregator {

	@Override
	public
	Object aggregate (
			@NonNull List <Object> valueObjects) {

		long sum = 0;

		for (
			Object valueObject
				: valueObjects
		) {

			sum +=
				(Long)
				valueObject;

		}

		return sum;

	}

}
