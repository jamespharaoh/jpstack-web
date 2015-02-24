package wbs.platform.currency.logic;

import wbs.platform.currency.model.CurrencyRec;

public
interface CurrencyLogic {

	String formatSimple (
			CurrencyRec currency,
			Long amount);

	String formatText (
			CurrencyRec currency,
			Long amount);

	String formatHtml (
			CurrencyRec currency,
			Long amountRaw);

	String formatHtmlTd (
			CurrencyRec currency,
			Long credit);

	Long parseText (
			CurrencyRec currency,
			String text);

}
