package wbs.console.misc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.FormatWriterWriter;
import wbs.framework.utils.etc.Html;

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
		new FormatWriterWriter (
			headStringWriter);

	FormatWriter footFormatWriter =
		new FormatWriterWriter (
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
			PrintWriter out) {

		String pageHeaderString =
			headStringWriter.toString ();

		String pageFooterString =
			footStringWriter.toString ();

		endPage ();

		for (String pageBodyString
				: pages) {

			out.print (
				"'");

			out.print (
				Html.javascriptStringEscape (
					pageHeaderString));

			out.print (
				Html.javascriptStringEscape (
					pageBodyString));

			out.print (
				Html.javascriptStringEscape (
					pageFooterString));

			out.print (
				"',\n");

		}

		out.print (
			"''");

	}

	public
	int pages () {
		return pages.size ();
	}

	public
	FormatWriter writer () {

		if (! inPage) {

			stringWriter =
				new StringWriter ();

			formatWriter =
				new FormatWriterWriter (
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
