package wbs.clients.apn.chat.graphs.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.YearMonth;

import wbs.console.html.ObsoleteDateLinks;
import wbs.console.part.AbstractPagePart;

@Accessors (fluent = true)
public abstract
class AbstractMonthlyGraphPart
	extends AbstractPagePart {

	// dependencies

	@Getter @Setter
	String myLocalPart;

	@Getter @Setter
	String imageLocalPart;

	// state

	YearMonth yearMonth;

	// implementation

	@Override
	public
	void prepare () {

		yearMonth =
			YearMonth.parse (
				requestContext.parameter ("month"));
	}

	@Override
	public
	void renderHtmlBodyContent ()  {

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				myLocalPart),
			" method=\"get\"",
			">\n");

		printFormat (
			"<p>Month<br>\n",

			"<input",
			" type=\"text\"",
			" name=\"month\"",
			" value=\"%h\"",
			yearMonth.toString (),
			">",

			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			"></p>");

		printFormat (
			"</form>\n");

		if (yearMonth != null) {

			ObsoleteDateLinks.monthlyBrowserParagraph (
				out,
				requestContext.resolveLocalUrl (
					myLocalPart),
				Collections.<String,String>emptyMap (),
				yearMonth.toLocalDate (1));

			printFormat (
				"<p>");

			printFormat (
				"<img",
				" style=\"graph\"",
				" src=\"%h\"",
				requestContext.resolveLocalUrl (
					stringFormat (
						"%s",
						imageLocalPart,
						"?month=%u",
						yearMonth.toString ())),
				">");

			printFormat (
				"</p>\n");

		}

	}

}
