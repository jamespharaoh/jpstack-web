package wbs.console.misc;

import com.google.common.base.Optional;

public
interface IntervalFormatter {

	Optional<Integer> parseIntervalStringSeconds (
			String input);

	Integer parseIntervalStringSecondsRequired (
			String input);

	String createIntervalStringSeconds (
			int input);

}
