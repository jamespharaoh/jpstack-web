package wbs.console.request;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;

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

			return stringFormat (
				"<p class=\"%h\">%s</p>",
				type,
				html);

		}

	}

	List <Notice> notices =
		new ArrayList<> ();

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
			@NonNull FormatWriter formatWriter) {

		for (
			Notice notice
				: notices
		) {

			formatWriter.writeString (
				notice.toString ());

		}

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
			@NonNull ServletRequest request,
			@NonNull String notice) {

		add (
			request,
			notice,
			"notice");

	}

	public static
	void addError (
			@NonNull ServletRequest request,
			@NonNull String notice) {

		add (
			request,
			notice,
			"error");

	}

	public static
	void addWarning (
			@NonNull ServletRequest request,
			@NonNull String notice) {

		add (
			request,
			notice,
			"warning");

	}
}
