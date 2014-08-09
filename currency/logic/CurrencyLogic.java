package wbs.platform.currency.logic;

import wbs.platform.currency.model.CurrencyRec;

public
interface CurrencyLogic {

	String formatText (
			CurrencyRec currency,
			int amount);

	String formatHtml (
			CurrencyRec currency,
			int amountRaw);

	String formatHtmlTd (
			CurrencyRec currency,
			int credit);

	int parseText (
			CurrencyRec currency,
			String text);

}
