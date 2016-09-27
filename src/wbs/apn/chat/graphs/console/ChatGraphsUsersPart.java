package wbs.apn.chat.graphs.console;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenGetAction;
import static wbs.utils.web.HtmlUtils.htmlLinkWrite;

import org.joda.time.LocalDate;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("chatGraphsUsersPart")
public
class ChatGraphsUsersPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	TimeFormatter timeFormatter;

	// implementation

	@Override
	public
	void renderHtmlBodyContent () {

		LocalDate date;
		String dateString;

		if (
			optionalIsPresent (
				requestContext.parameter (
					"date"))
		) {

			dateString =
				requestContext.parameterRequired (
					"date");

			try {

				date =
					timeFormatter.dateStringToLocalDateRequired (
						dateString);

			} catch (IllegalArgumentException exception) {

				date = null;

			}

		} else {

			date =
				LocalDate.now ();

			dateString =
				timeFormatter.dateString (
					date);

		}

		htmlFormOpenGetAction (
			requestContext.resolveLocalUrl (
				"/chat.graphs.users"));

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"Date<br>");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" name=\"date\"",
			" value=\"%h\"",
			dateString,
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			">");

		htmlParagraphClose ();

		htmlFormClose (); 

		if (
			isNotNull (
				date)
		) {

			// write date browser

			htmlParagraphOpen (
				htmlClassAttribute (
					"links"));

			htmlLinkWrite (
				stringFormat (
					"?date=%u",
					timeFormatter.dateString (
						date.minusWeeks (1))),
				"Prev week");
				
			htmlLinkWrite (
				stringFormat (
					"?date=%h",
					timeFormatter.dateString (
						date.minusDays (1))),
				"Prev day");

			htmlLinkWrite (
				stringFormat (
					"?date=%u",
					timeFormatter.dateString (
						date.plusDays (1))),
					"Next day");

			htmlLinkWrite (
				stringFormat (
					"?date=%u",
					timeFormatter.dateString (
						date.plusWeeks (1))),
				"Next week");

			htmlParagraphClose ();

			// write graph image

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<img",
				" style=\"graph\"",
				" src=\"%h\"",
				requestContext.resolveLocalUrl (
					stringFormat (
						"/chat.graphs.usersImage",
						"?date=%u",
						dateString)),
				">");

			htmlParagraphClose ();

		}

	}

}
