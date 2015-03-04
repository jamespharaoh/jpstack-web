package wbs.platform.reporting.console;

public
interface StatsFormatter {

	String format (
			Object group,
			String step,
			Object value);

	String formatTotal (
			Object group,
			Object value);

}
