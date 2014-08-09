package wbs.platform.reporting.console;

public
interface StatsFormatter {

	String format (
			Object group,
			int step,
			Object value);

}
