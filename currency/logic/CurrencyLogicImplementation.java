package wbs.platform.currency.logic;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.currency.model.CurrencyRec;

@SingletonComponent ("currencyLogic")
public
class CurrencyLogicImplementation
	implements CurrencyLogic {

	@Override
	public
	String formatSimple (
			@NonNull CurrencyRec currency,
			Long amount) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			amount / currency.getDivisions ());

		stringBuilder.append (
			".");

		if (currency.getDivisions () == 100) {

			Long remainder =
				Math.abs (amount % 100);

			if (remainder == 0) {

				stringBuilder.append (
					"00");

			} else if (remainder < 10) {

				stringBuilder.append (
					"0");

				stringBuilder.append (
					remainder);

			} else {

				stringBuilder.append (
					remainder);

			}

		} else {

			throw new RuntimeException ();

		}

		return stringBuilder.toString ();

	}

	@Override
	public
	String formatText (
			@NonNull CurrencyRec currency,
			@NonNull Long amount) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			currency.getPrefix ());

		stringBuilder.append (
			amount / currency.getDivisions ());

		if (currency.getDivisions () == 1) {

			doNothing ();

		} else if (currency.getDivisions () == 10) {

			stringBuilder.append (
				".");

			Long remainder =
				Math.abs (amount % 10);

			if (remainder == 0) {

				stringBuilder.append (
					"0");

			} else {

				stringBuilder.append (
					remainder);

			}

		} else if (currency.getDivisions () == 100) {

			stringBuilder.append (
				".");

			Long remainder =
				Math.abs (amount % 100);

			if (remainder == 0) {

				stringBuilder.append (
					"00");

			} else if (remainder < 10) {

				stringBuilder.append (
					"0");

				stringBuilder.append (
					remainder);

			} else {

				stringBuilder.append (
					remainder);

			}

		} else {

			throw new RuntimeException ();

		}

		if (amount == 1 || amount == -1) {

			stringBuilder.append (
				currency.getSingularSuffix ());

		} else {

			stringBuilder.append (
				currency.getPluralSuffix ());

		}


		return stringBuilder.toString ();

	}

	@Override
	public
	String formatHtml (
			@NonNull CurrencyRec currency,
			Long amountRaw) {

		String amountString =
			formatText (
				currency,
				amountRaw);

		return amountRaw < 0

			? stringFormat (
				"<span style=\"color: red\">%h</span>",
				amountString)

			: stringFormat (
				"%h",
				amountString);

	}

	@Override
	public
	String formatHtmlTd (
			@NonNull CurrencyRec currency,
			Long credit) {

		return stringFormat (
			"<td style=\"text-align: right\">%s</td>",
			formatHtml (
				currency,
				credit));

	}

	@Override
	public
	Optional<Long> parseText (
			@NonNull CurrencyRec currency,
			@NonNull String text) {

		// build pattern

		int decimalPlaces;

		if (currency.getDivisions () == 1) {
			decimalPlaces = 0;
		} else if (currency.getDivisions () == 10) {
			decimalPlaces = 1;
		} else if (currency.getDivisions () == 100) {
			decimalPlaces = 2;
		} else {
			throw new RuntimeException ();
		}

		Pattern pattern =
			Pattern.compile (
				joinWithoutSeparator (

					// prefix

					"\\s*",
					"(?:",
					Pattern.quote (
						currency.getPrefix ()),
					")?",
					"\\s*",

					// sign

					"([-+])?",

					// units

					"(\\d+)",

					// subdivisions

					decimalPlaces > 0
						? joinWithoutSeparator (
							"\\.",
							"(\\d{" + decimalPlaces + "})")
						: "",

					// suffix

					"\\s*",
					"(?:",
					Pattern.quote (
						currency.getSingularSuffix ().trim ()),
					"|",
					Pattern.quote (
						currency.getPluralSuffix ().trim ()),
					")?",
					"\\s*"));

		// perform match

		Matcher matcher =
			pattern.matcher (
				text);

		if (! matcher.matches ()) {

System.out.println ("AA");

			return Optional.<Long>absent ();

		}

System.out.println ("BB");

		// return the result

		boolean positive =
			equal (
				ifNull (
					matcher.group (1),
					"+"),
				"+");

		Long units =
			Long.parseLong (
				matcher.group (2));

		Long subDivisions =
			decimalPlaces > 0
				? Long.parseLong (
					matcher.group (3))
				: 0;

		if (positive) {

			return Optional.<Long>of (
				+ units * currency.getDivisions ()
				+ subDivisions);

		} else {

			return Optional.<Long>of (
				- units * currency.getDivisions ()
				- subDivisions);

		}

	}

	@Override
	public
	Long parseTextRequired (
			@NonNull CurrencyRec currency,
			@NonNull String text) {

		return optionalRequired (
			parseText (
				currency,
				text));

	}

}
