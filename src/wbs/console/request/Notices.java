package wbs.console.request;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

import wbs.framework.utils.etc.Html;

public
class Notices {

	static
	class Notice {

		String html;
		String type;

		public
		Notice (
				String html,
				String type) {

			this.html = html;
			this.type = type;

		}

		@Override
		public
		String toString () {

			return "<p class=\"" + Html.encode(type) + "\">" + html + "</p>\n";

		}

	}

	List<Notice> notices =
		new ArrayList<Notice> ();

	public
	void add (
			String notice,
			String type) {

		if (notice == null)
			return;

		notices.add (
			new Notice (
				notice,
				type));

	}

	public
	void add (
			String notice) {

		add (
			notice,
			"notice");

	}

	public
	void flush (
			PrintWriter out) {

		for (Notice notice : notices)
			out.println(notice.toString ());

		notices.clear ();

	}

	@Override
	public
	String toString () {

		StringBuffer str =
			new StringBuffer ();

		for (Notice notice : notices)
			str.append (notice.toString ());

		return str.toString ();

	}

	public static
	void add (
			ServletRequest request,
			String notice,
			String type) {

		Notices notices =
			(Notices)
			request.getAttribute (
				"wbs.notices");

		if (notices == null) {

			notices =
				new Notices ();

			request.setAttribute (
				"wbs.notices",
				notices);

		}

		notices.add (
			notice,
			type);

	}

	public static
	void addNotice (
			ServletRequest request,
			String notice) {

		add (
			request,
			notice,
			"notice");

	}

	public static
	void addError (
			ServletRequest request,
			String notice) {

		add (
			request,
			notice,
			"error");

	}

	public static void addWarning(ServletRequest request, String notice) {
		add(request, notice, "warning");
	}
}
