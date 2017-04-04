package wbs.utils.time;

import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;

import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("durationFormatter")
public
class DurationFormatterImplementation
	implements DurationFormatter {

	@Override
	public
	Optional <Duration> stringToDuration (
			@NonNull String input) {

		for (
			IntervalMatcher intervalMatcher
				: intervalMatchers
		) {

			Optional <Duration> durationOptional =
				intervalMatcher.match (
					input);

			if (
				optionalIsNotPresent (
					durationOptional)
			) {
				continue;
			}

			Duration duration =
				optionalGetRequired (
					durationOptional);

			return optionalOf (
				duration);

		}

		return optionalAbsent ();

	}

	@Override
	public
	String durationToStringTextual (
			@NonNull Duration input) {

		for (
			IntervalMatcher intervalMatcher
				: intervalMatchers
		) {

			Optional <String> intervalStringOptional =
				intervalMatcher.textify (
					input);

			if (
				optionalIsNotPresent (
					intervalStringOptional)
			) {
				continue;
			}

			return optionalGetRequired (
				intervalStringOptional);

		}

		throw new RuntimeException ();

	}

	@Override
	public
	String durationToStringNumeric (
			@NonNull Duration inputDuration) {

		long inputMilliseconds =
			inputDuration.getMillis ();

		if (inputMilliseconds < 60l * 1000l) {

			if (inputMilliseconds % 1000l != 0) {

				return String.format (
					"%d.%03ds",
					inputMilliseconds / 1000l,
					inputMilliseconds % 1000l);

			} else {

				return String.format (
					"%d",
					inputMilliseconds / 1000l);

			}

		} else if (inputMilliseconds < 60l * 60l * 1000l) {

			if (inputMilliseconds % 1000l != 0) {

				return String.format (
					"%d.%02d.%03ds",
					inputMilliseconds / 1000l / 60l,
					inputMilliseconds / 1000l % 60l,
					inputMilliseconds % 1000l);

			} else {

				return String.format (
					"%d:%02d",
					inputMilliseconds / 1000l / 60l,
					inputMilliseconds / 1000l % 60l);

			}

		} else {

			if (inputMilliseconds % 1000l != 0) {

				return String.format (
					"%d:%02d:%02d.%03d",
					inputMilliseconds / 1000l / 60l / 60l,
					inputMilliseconds / 1000l / 60l % 60l,
					inputMilliseconds / 1000l % 60l,
					inputMilliseconds % 1000l);

			} else {

				return String.format (
					"%d:%02d:%02d",
					inputMilliseconds / 1000l / 60l / 60l,
					inputMilliseconds / 1000l / 60l % 60l,
					inputMilliseconds / 1000l % 60l);

			}

		}

	}

	private static
	interface IntervalMatcher {

		Optional <Duration> match (
				String input);

		Optional <String> textify (
				Duration input);

	}

	private static
	class SimpleIntervalMatcher
		implements IntervalMatcher {

		String singularLabel;
		String pluralLabel;
		Pattern pattern;
		long scale;

		SimpleIntervalMatcher (
				@NonNull Long newScale,
				@NonNull String newSingularLabel,
				@NonNull String newPluralLabel,
				@NonNull String ... otherLabels) {

			scale =
				newScale;

			singularLabel =
				newSingularLabel;

			pluralLabel =
				newPluralLabel;

			StringBuilder stringBuilder =
				new StringBuilder ();

			stringBuilder.append ("\\s*(\\d+)\\s*(");
			stringBuilder.append (Pattern.quote (singularLabel));

			if (pluralLabel != null && ! singularLabel.equals (pluralLabel)) {
				stringBuilder.append ('|');
				stringBuilder.append (Pattern.quote (pluralLabel));
			}

			for (String otherLabel : otherLabels) {
				stringBuilder.append ('|');
				stringBuilder.append (Pattern.quote (otherLabel));
			}

			stringBuilder.append (")\\s*");

			pattern =
				Pattern.compile (stringBuilder.toString ());

		}

		@Override
		public
		Optional <Duration> match (
				@NonNull String input) {

			Matcher matcher =
				pattern.matcher (
					input);

			if (! matcher.matches ()) {
				return optionalAbsent ();
			}

			return optionalOf (
				Duration.millis (
					scale * parseIntegerRequired (
						matcher.group (1))));

		}

		@Override
		public
		Optional <String> textify (
				@NonNull Duration input) {

			long inputMilliseconds =
				input.getMillis ();

			if (inputMilliseconds % scale != 0) {

				return optionalAbsent ();

			} else if (inputMilliseconds / scale == 1) {

				return optionalOf (
					"1 " + singularLabel);

			} else {

				return optionalOf (
					stringFormat (
						"%s %s",
						integerToDecimalString (
							inputMilliseconds / scale),
						pluralLabel));

			}

		}

	}

	Pattern zeroPattern =
		Pattern.compile ("\\s*0+\\s*");

	IntervalMatcher[] intervalMatchers =
		new IntervalMatcher [] {

		new IntervalMatcher () {

			@Override
			public
			Optional <Duration> match (
					@NonNull String input) {

				return optionalIf (
					zeroPattern.matcher (input).matches (),
					() -> Duration.ZERO);

			}

			@Override
			public
			Optional <String> textify (
					@NonNull Duration input) {

				long inputMilliseconds =
					input.getMillis ();

				return optionalIf (
					equalToZero (
						inputMilliseconds),
					() -> "0");

			}

		},

		new SimpleIntervalMatcher (
			31556736000l,
			"year",
			"years"),

		new SimpleIntervalMatcher (
			2629728000l,
			"month",
			"months"),

		new SimpleIntervalMatcher (
			86400000l,
			"day",
			"days",
			"d"),

		new SimpleIntervalMatcher (
			3600000l,
			"hour",
			"hours",
			"h"),

		new SimpleIntervalMatcher (
			60000l,
			"minute",
			"minutes",
			"mins",
			"min",
			"m"),

		new SimpleIntervalMatcher (
			1000l,
			"second",
			"seconds",
			"secs",
			"sec",
			"s"),

		new SimpleIntervalMatcher (
			1l,
			"millisecond",
			"milliseconds",
			"msecs",
			"msec",
			"ms")

	};

}
