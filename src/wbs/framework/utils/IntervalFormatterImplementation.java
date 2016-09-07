package wbs.framework.utils;

import static wbs.framework.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("intervalFormatter")
public
class IntervalFormatterImplementation
	implements IntervalFormatter {

	@Override
	public
	Optional <Long> parseIntervalStringSeconds (
			@NonNull String input) {

		for (
			IntervalMatcher intervalMatcher
				: intervalMatchers
		) {

			Long interval =
				intervalMatcher.match (
					input);

			if (interval == null)
				continue;

			return Optional.of (
				interval);

		}

		return Optional.absent ();

	}

	@Override
	public
	Long parseIntervalStringSecondsRequired (
			@NonNull String input) {

		return optionalGetRequired (
			parseIntervalStringSeconds (
				input));

	}

	@Override
	public
	String createTextualIntervalStringSeconds (
			Long input) {

		for (
			IntervalMatcher intervalMatcher
				: intervalMatchers
		) {

			String intervalString =
				intervalMatcher.textify (
					input);

			if (intervalString == null)
				continue;

			return intervalString;

		}

		throw new RuntimeException ();

	}

	@Override
	public
	String createNumericIntervalStringSeconds (
			@NonNull Long inputObject) {

		long input =
			inputObject;

		if (input < 60) {

			return String.format (
				"%d",
				input);

		} else if (input < 60 * 60) {

			return String.format (
				"%d:%02d",
				input / 60,
				input % 60);

		} else {

			return String.format (
				"%d:%02d:%02d",
				input / 60 / 60,
				input / 60 % 60,
				input % 60);

		}

	}

	private static
	interface IntervalMatcher {

		Long match (
				String input);

		String textify (
				long input);

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
				@NonNull String... otherLabels) {

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
		Long match (
				@NonNull String input) {

			Matcher matcher =
				pattern.matcher (
					input);

			return matcher.matches ()
				? scale * parseIntegerRequired (
					matcher.group (1))
				: null;

		}

		@Override
		public
		String textify (
				long input) {

			if (input % scale != 0)
				return null;

			if (input / scale == 1)
				return "1 " + singularLabel;

			return stringFormat (
				"%s %s",
				input / scale,
				pluralLabel);

		}

	}

	Pattern zeroPattern =
		Pattern.compile ("\\s*0+\\s*");

	IntervalMatcher[] intervalMatchers =
		new IntervalMatcher [] {

		new IntervalMatcher () {

			@Override
			public
			Long match (
					String input) {

				return zeroPattern.matcher (input).matches ()
					? 0l
					: null;

			}

			@Override
			public
			String textify (
					long input) {

				return input == 0 ? "0" : null;

			}

		},

		new SimpleIntervalMatcher (
			31556736l,
			"year",
			"years"),

		new SimpleIntervalMatcher (
			2629728l,
			"month",
			"months"),

		new SimpleIntervalMatcher (
			86400l,
			"day",
			"days",
			"d"),

		new SimpleIntervalMatcher (
			3600l,
			"hour",
			"hours",
			"h"),

		new SimpleIntervalMatcher (
			60l,
			"minute",
			"minutes",
			"mins",
			"min",
			"m"),

		new SimpleIntervalMatcher (
			1l,
			"second",
			"seconds",
			"secs",
			"sec",
			"s")

	};

}
