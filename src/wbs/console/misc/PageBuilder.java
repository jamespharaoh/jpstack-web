package wbs.console.misc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import wbs.framework.utils.etc.Html;

public
class PageBuilder {

	List<String> pages =
		new ArrayList<String> ();

	boolean inPage =
		false;

	StringWriter stringWriter;

	PrintWriter printWriter;

	StringWriter headStringWriter =
		new StringWriter ();

	StringWriter footStringWriter =
		new StringWriter ();

	PrintWriter headPrintWriter =
		new PrintWriter (
			headStringWriter);

	PrintWriter footPrintWriter =
		new PrintWriter (
			footStringWriter);

	public
	void endPage () {

		if (! inPage)
			return;

		printWriter.flush ();

		pages.add (
			stringWriter.toString ());

		inPage = false;

	}

	public
	void goPages (
			PrintWriter out) {

		headPrintWriter.flush ();

		String pageHeaderString =
			headStringWriter.toString ();

		footPrintWriter.flush ();

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
	PrintWriter writer () {

		if (! inPage) {

			stringWriter =
				new StringWriter ();

			printWriter =
				new PrintWriter (
					stringWriter);

			inPage =
				true;

		}

		return printWriter;

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
	PrintWriter headWriter () {
		return headPrintWriter;
	}

	public
	PrintWriter footWriter () {
		return footPrintWriter;
	}

}
