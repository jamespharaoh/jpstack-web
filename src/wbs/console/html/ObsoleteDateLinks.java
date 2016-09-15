package wbs.console.html;

import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import wbs.utils.string.FormatWriter;

/**
 * Useful stuff for making sets of links to different dates in html.
 */
public
class ObsoleteDateLinks {

	/**
	 * Takes a basic url, adds a set of query parameters from a FormData, then
	 * ads a final parameter specified by field and value.
	 */
	public static
	String makeLink (
			String url,
			Map<String,String> formData,
			String field,
			String value) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			stringFormat (
				"%s?",
				url));

		for (
			Map.Entry <String, String> entry
				: formData.entrySet ()
		) {

			if (
				stringEqualSafe (
					entry.getKey (),
					field)
			) {
				continue;
			}

			stringBuilder.append (
				stringFormat (
					"%u=%u&",
					entry.getKey (),
					entry.getValue ()));

		}

		stringBuilder.append (
			stringFormat (
				"%u=%u",
				field,
				value));

		return stringBuilder.toString ();

	}

	/**
	 * Represents a policy of dates to link to.
	 */
	public static
	interface LinkMaker {

		void makeLinks (
				LinkWriter linkWriter,
				LocalDate date);

	}

	/**
	 * Called back by LinkMaker for each link.
	 */
	public static
	interface LinkWriter {

		void writeLink (
				LocalDate date,
				String title);

	}

	/**
	 * A LinkMaker which creates links for last year, last month, next month and
	 * next year.
	 */
	public final static
	LinkMaker monthlyLinkMaker =
		new LinkMaker () {

		@Override
		public
		void makeLinks (
				LinkWriter linkWriter,
				LocalDate date) {

			linkWriter.writeLink (
				date.minusYears (1),
				"Prev year");

			linkWriter.writeLink (
				date.minusMonths (1),
				"Prev month");

			linkWriter.writeLink (
				date.plusMonths (1),
				"Next month");

			linkWriter.writeLink (
				date.plusYears (1),
				"Next year");

		}

	};

	/**
	 * A LinkMaker which creates links for last month, last week, last day, next
	 * day, next week and next month.
	 */
	public final static
	LinkMaker dailyLinkMaker =
		new LinkMaker () {

		@Override
		public
		void makeLinks (
				LinkWriter linkWriter,
				LocalDate date) {

			linkWriter.writeLink (
				date.minusMonths (1),
				"Prev month");

			linkWriter.writeLink (
				date.minusWeeks (1),
				"Prev week");

			linkWriter.writeLink (
				date.minusDays (1),
				"Prev day");

			linkWriter.writeLink (
				date.plusDays (1),
				"Next day");

			linkWriter.writeLink (
				date.plusWeeks (1),
				"Next week");

			linkWriter.writeLink (
				date.plusMonths (1),
				"Next month");

		}

	};

	/**
	 * Uses a LinkMaker and LinkWriter to write a link paragraph in html to a
	 * PrintWriter.
	 */
	public static
	void browser (
			final PrintWriter out,
			final String url,
			final Map<String,String> formData,
			final LocalDate date,
			final String dateFieldName,
			final LinkMaker linkMaker,
			final DateTimeFormatter dateFormatter) {

		LinkWriter linkWriter =
			new LinkWriter () {

			@Override
			public
			void writeLink (
					LocalDate date,
					String title) {

				out.println (
					stringFormat (
						"<a",
						" href=\"%h\"",
						makeLink (
							url,
							formData,
							dateFieldName,
							dateFormatter.print (date)),
						">%h</a>\n",
						title));

			}

		};

		linkMaker.makeLinks (
			linkWriter,
			date);

	}

	public static
	void browserParagraph (
			FormatWriter formatWriter,
			String url,
			Map <String, String> formData,
			LocalDate date,
			String dateFieldName,
			LinkMaker linkMaker,
			DateTimeFormatter dateFormatter) {

		formatWriter.writeLineFormat (
			"<p class=\"links\">");

		formatWriter.increaseIndent ();

		LinkWriter linkWriter =
			new LinkWriter () {

			@Override
			public
			void writeLink (
					LocalDate date,
					String title) {

				formatWriter.writeLineFormat (
					"<a",
					" href=\"%h\"",
					makeLink (
						url,
						formData,
						dateFieldName,
							dateFormatter.print (date)),
					">%h</a>",
					title);

			}

		};

		linkMaker.makeLinks (
			linkWriter,
			date);

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</p>");

	}

	/**
	 * Utility function to call browser (...) with monthlyLinkMaker and
	 * MonthField.dateFormat.
	 */
	public static
	void monthlyBrowserParagraph (
			FormatWriter formatWriter,
			String url,
			Map<String,String> formData,
			LocalDate date) {

		browserParagraph (
			formatWriter,
			url,
			formData,
			date,
			"month",
			monthlyLinkMaker,
			ObsoleteMonthField.dateFormatter);

	}

	/**
	 * Utility function to call browser (...) with dailyLinkMaker and
	 * DateField.dateFormat.
	 */
	public static
	void dailyBrowserParagraph (
			FormatWriter formatWriter,
			String url,
			Map<String,String> formData,
			LocalDate date) {

		browserParagraph (
			formatWriter,
			url,
			formData,
			date,
			"date",
			dailyLinkMaker,
			ObsoleteDateField.dateFormatter);

	}

	public static
	String dailyBrowserLinks (
			String url,
			Map<String,String> formData,
			LocalDate date) {

		StringWriter stringWriter =
			new StringWriter ();

		browser (
			new PrintWriter (
				stringWriter),
			url,
			formData,
			date,
			"date",
			dailyLinkMaker,
			ObsoleteDateField.dateFormatter);

		return stringWriter.toString ();

	}

}
