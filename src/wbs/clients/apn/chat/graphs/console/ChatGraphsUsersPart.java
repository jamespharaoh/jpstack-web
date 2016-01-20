package wbs.clients.apn.chat.graphs.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import wbs.console.misc.TimeFormatter;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("chatGraphsUsersPart")
public
class ChatGraphsUsersPart
	extends AbstractPagePart {

	@Inject
	TimeFormatter timeFormatter;

	@Override
	public
	void renderHtmlBodyContent () {

		LocalDate date;

		String dateString =
			requestContext.parameter ("date");

		if (dateString == null) {

			date =
				LocalDate.now ();

			dateString =
				timeFormatter.localDateToDateString (
					date);

		} else {

			try {

				date =
					timeFormatter.dateStringToLocalDateRequired (
						dateString);

			} catch (IllegalArgumentException exception) {

				date = null;

			}

		}

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chat.graphs.users"),
			" method=\"get\"",
			">\n");

		printFormat (
			"<p>Date<br>\n",

			"<input",
			" type=\"text\"",
			" name=\"date\"",
			" value=\"%h\"",
			dateString,
			">\n",

			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			"></p>");

		printFormat (
			"</form>\n");

		if (date != null) {

			printFormat (
				"<p class=\"links\">\n",

				"<a href=\"%h\">Prev week</a>\n",
				stringFormat (
					"?date=%u",
					timeFormatter.localDateToDateString (
						date.minusWeeks (1))),

				"<a href=\"%h\">Prev day</a>\n",
				stringFormat (
					"?date=%h",
					timeFormatter.localDateToDateString (
						date.minusDays (1))),

				"<a href=\"%h\">Next day</a>\n",
				stringFormat (
					"?date=%u",
					timeFormatter.localDateToDateString (
						date.plusDays (1))),

				"<a href=\"%h\">Next week</a>",
				stringFormat (
					"?date=%u",
					timeFormatter.localDateToDateString (
						date.plusWeeks (1))),

				"</p>\n");

			printFormat (
				"<p><img",
				" style=\"graph\"",
				" src=\"%h\"",
				requestContext.resolveLocalUrl (
					stringFormat (
						"/chat.graphs.usersImage",
						"?date=%u",
						dateString)),
				"></p>\n");

		}

	}

}
