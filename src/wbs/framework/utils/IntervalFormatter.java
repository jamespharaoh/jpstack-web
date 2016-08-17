package wbs.framework.utils;

import com.google.common.base.Optional;

public
interface IntervalFormatter {

	Optional <Long> parseIntervalStringSeconds (
			String input);

	Long parseIntervalStringSecondsRequired (
			String input);

	String createTextualIntervalStringSeconds (
			Long input);

	String createNumericIntervalStringSeconds (
			Long input);

}
