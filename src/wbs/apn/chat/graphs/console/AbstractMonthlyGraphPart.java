package wbs.apn.chat.graphs.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenGetAction;

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
				requestContext.parameterRequired (
					"month"));

	}

	@Override
	public
	void renderHtmlBodyContent ()  {

		htmlFormOpenGetAction (
			requestContext.resolveLocalUrl (
				myLocalPart));

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"Month<br>");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" name=\"month\"",
			" value=\"%h\"",
			yearMonth.toString (),
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			">");

		htmlParagraphClose ();

		htmlFormClose ();

		if (yearMonth != null) {

			ObsoleteDateLinks.monthlyBrowserParagraph (
				formatWriter,
				requestContext.resolveLocalUrl (
					myLocalPart),
				Collections.<String,String>emptyMap (),
				yearMonth.toLocalDate (1));

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
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

			htmlParagraphClose ();

		}

	}

}
