package wbs.console.reporting;

import com.google.common.base.Optional;

public
interface StatsFormatter {

	String format (
			Object group,
			StatsPeriod period,
			Integer step,
			Optional<Object> value);

	String formatTotal (
			Object group,
			Optional<Object> value);

}
