package wbs.platform.console.html;

import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.urlEncode;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import wbs.platform.console.request.FormData;

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
			FormData formData,
			String field,
			String value) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append(url);
		stringBuilder.append("?");
		for (FormData.Entry entry : formData) {
			if (entry.getName().equals(field))
				continue;
			stringBuilder.append(urlEncode(entry.getName()));
			stringBuilder.append('=');
			stringBuilder.append(urlEncode(entry.getValue()));
			stringBuilder.append('&');
		}
		stringBuilder.append(urlEncode(field));
		stringBuilder.append('=');
		stringBuilder.append(urlEncode(value));

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
			final FormData formData,
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
			final PrintWriter out,
			final String url,
			final FormData formData,
			final LocalDate date,
			final String dateFieldName,
			final LinkMaker linkMaker,
			final DateTimeFormatter dateFormatter) {

		out.println (
			"<p class=\"links\">");

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

		out.println("</p>");
	}

	/**
	 * Utility function to call browser (...) with monthlyLinkMaker and
	 * MonthField.dateFormat.
	 */
	public static
	void monthlyBrowserParagraph (
			PrintWriter out,
			String url,
			FormData formData,
			LocalDate date) {

		browserParagraph (
			out,
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
			PrintWriter out,
			String url,
			FormData formData,
			LocalDate date) {

		browserParagraph (
			out,
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
			FormData formData,
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
