package wbs.platform.currency.logic;

import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.currency.model.CurrencyRec;

@SingletonComponent ("currencyLogic")
public
class CurrencyLogicImpl
	implements CurrencyLogic {

	@Override
	public
	String formatText (
			@NonNull CurrencyRec currency,
			Long amount) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			currency.getPrefix ());

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

		stringBuilder.append (
			currency.getSuffix ());

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
	Long parseText (
			CurrencyRec currency,
			String text) {

		// build pattern

		int decimalPlaces;

		if (currency.getDivisions () == 100) {
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

					// units

					"(\\d+)",

					// subdivisions

					"\\.",
					"(\\d{" + decimalPlaces + "})",

					// suffix

					"\\s*",
					"(?:",
					Pattern.quote (
						currency.getSuffix()),
					")?",
					"\\s*"));

		// perform match

		Matcher matcher =
			pattern.matcher (
				text);

		if (! matcher.matches ())
			throw new RuntimeException ();

		// return the result

		Long units =
			Long.parseLong (
				matcher.group (1));

		Long subDivisions =
			Long.parseLong (
				matcher.group (2));

		return
			+ units * currency.getDivisions ()
			+ subDivisions;

	}

}
