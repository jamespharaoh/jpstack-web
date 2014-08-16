package wbs.platform.console.html;

import static wbs.framework.utils.etc.Misc.urlEncode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import wbs.framework.utils.etc.Html;
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
				Date date);

	}

	/**
	 * Called back by LinkMaker for each link.
	 */
	public static
	interface LinkWriter {

		void writeLink (
				Date date,
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
				Date date) {

			Calendar calendar =
				Calendar.getInstance ();

			calendar.setTime(date);
			calendar.add(Calendar.YEAR, -1);
			linkWriter.writeLink(calendar.getTime(), "Prev year");

			calendar.setTime(date);
			calendar.add(Calendar.MONTH, -1);
			linkWriter.writeLink(calendar.getTime(), "Prev month");

			calendar.setTime(date);
			calendar.add(Calendar.MONTH, 1);
			linkWriter.writeLink(calendar.getTime(), "Next month");

			calendar.setTime(date);
			calendar.add(Calendar.YEAR, 1);
			linkWriter.writeLink(calendar.getTime(), "Next year");

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
				Date date) {

			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.add(Calendar.MONTH, -1);
			linkWriter.writeLink(cal.getTime(), "Prev month");

			cal.setTime(date);
			cal.add(Calendar.DATE, -7);
			linkWriter.writeLink(cal.getTime(), "Prev week");

			cal.setTime(date);
			cal.add(Calendar.DATE, -1);
			linkWriter.writeLink(cal.getTime(), "Prev day");

			cal.setTime(date);
			cal.add(Calendar.DATE, 1);
			linkWriter.writeLink(cal.getTime(), "Next day");

			cal.setTime(date);
			cal.add(Calendar.DATE, 7);
			linkWriter.writeLink(cal.getTime(), "Next week");

			cal.setTime(date);
			cal.add(Calendar.MONTH, 1);
			linkWriter.writeLink(cal.getTime(), "Next month");

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
			final Date date,
			final String dateFieldName,
			final LinkMaker linkMaker,
			final SimpleDateFormat dateFormat) {

		LinkWriter linkWriter =
			new LinkWriter () {

			@Override
			public
			void writeLink (
					Date date,
					String title) {

				out.println("<a href=\""
						+ Html.encode(makeLink(url, formData, dateFieldName,
								dateFormat.format(date))) + "\">"
						+ Html.encode(title) + "</a>");

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
			final Date date,
			final String dateFieldName,
			final LinkMaker linkMaker,
			final SimpleDateFormat dateFormat) {

		out.println (
			"<p class=\"links\">");

		LinkWriter linkWriter =
			new LinkWriter () {

			@Override
			public
			void writeLink (
					Date date,
					String title) {

				out.println("<a href=\""
						+ Html.encode(makeLink(url, formData, dateFieldName,
								dateFormat.format(date))) + "\">"
						+ Html.encode(title) + "</a>");

			}

		};

		linkMaker.makeLinks(linkWriter, date);

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
			Date date) {

		browserParagraph (
			out,
			url,
			formData,
			date,
			"month",
			monthlyLinkMaker,
			ObsoleteMonthField.dateFormat);

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
			Date date) {

		browserParagraph (
			out,
			url,
			formData,
			date,
			"date",
			dailyLinkMaker,
			ObsoleteDateField.dateFormat);

	}

	public static
	String dailyBrowserLinks (
			String url,
			FormData formData,
			Date date) {

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
			ObsoleteDateField.dateFormat);

		return stringWriter.toString ();

	}

}
