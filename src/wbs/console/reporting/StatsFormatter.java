package wbs.console.reporting;

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
