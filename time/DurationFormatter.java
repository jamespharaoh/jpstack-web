package wbs.utils.time;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;

public
interface DurationFormatter {

	Optional <Duration> stringToDuration (
			String input);

	default
	Duration stringToDurationRequired (
			@NonNull String input) {

		return optionalGetRequired (
			stringToDuration (
				input));

	}

	String durationToStringTextual (
			Duration input);

	String durationToStringNumeric (
			Duration input);

}
