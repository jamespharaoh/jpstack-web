package wbs.console.misc;


import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;
import wbs.utils.web.HtmlUtils;

public
class PageBuilder {

	List<String> pages =
		new ArrayList<String> ();

	boolean inPage =
		false;

	StringWriter stringWriter;

	FormatWriter formatWriter;

	StringWriter headStringWriter =
		new StringWriter ();

	StringWriter footStringWriter =
		new StringWriter ();

	FormatWriter headFormatWriter =
		new WriterFormatWriter (
			headStringWriter);

	FormatWriter footFormatWriter =
		new WriterFormatWriter (
			footStringWriter);

	public
	void endPage () {

		if (! inPage)
			return;

		pages.add (
			stringWriter.toString ());

		inPage = false;

	}

	public
	void goPages (
			@NonNull FormatWriter formatWriter) {

		String pageHeaderString =
			headStringWriter.toString ();

		String pageFooterString =
			footStringWriter.toString ();

		endPage ();

		for (String pageBodyString
				: pages) {

			formatWriter.writeString (
				"'");

			formatWriter.writeString (
				HtmlUtils.javascriptStringEscape (
					pageHeaderString));

			formatWriter.writeString (
				HtmlUtils.javascriptStringEscape (
					pageBodyString));

			formatWriter.writeString (
				HtmlUtils.javascriptStringEscape (
					pageFooterString));

			formatWriter.writeString (
				"',\n");

		}

		formatWriter.writeString (
			"''");

	}

	public
	long pages () {
		return pages.size ();
	}

	public
	FormatWriter writer () {

		if (! inPage) {

			stringWriter =
				new StringWriter ();

			formatWriter =
				new WriterFormatWriter (
					stringWriter);

			inPage =
				true;

		}

		return formatWriter;

	}

	public
	String page (
			int i) {

		return ""
			+ headStringWriter.toString ()
			+ pages.get (0)
			+ footStringWriter.toString ();

	}

	public
	FormatWriter headWriter () {
		return headFormatWriter;
	}

	public
	FormatWriter footWriter () {
		return footFormatWriter;
	}

}
