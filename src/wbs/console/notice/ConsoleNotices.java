package wbs.console.notice;

import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;

public
class ConsoleNotices {

	List <ConsoleNotice> notices =
		new ArrayList<> ();

	public
	List <ConsoleNotice> notices () {

		return ImmutableList.copyOf (
			notices);

	}

	public
	void add (
			@NonNull ConsoleNoticeType type,
			@NonNull String notice) {

		notices.add (
			new ConsoleNotice (
				type,
				notice));

	}

	public
	void flush (
			@NonNull FormatWriter formatWriter) {

		for (
			ConsoleNotice notice
				: notices
		) {

			formatWriter.writeLineFormat (
				"%s",
				notice.toString ());

		}

		notices.clear ();

	}

	public
	void noticeFormat (
			@NonNull String ... arguments) {

		notices.add (
			new ConsoleNotice (
				ConsoleNoticeType.notice,
				stringFormatArray (
					arguments)));

	}

	@Override
	public
	String toString () {

		StringBuilder stringBuilder =
			new StringBuilder ();

		for (
			ConsoleNotice notice
				: notices
		) {

			stringBuilder.append (
				notice.toString ());

		}

		return stringBuilder.toString ();

	}

	public static
	void add (
			ServletRequest request,
			String notice,
			ConsoleNoticeType type) {

		ConsoleNotices notices =
			(ConsoleNotices)
			request.getAttribute (
				"wbs.notices");

		if (notices == null) {

			notices =
				new ConsoleNotices ();

			request.setAttribute (
				"wbs.notices",
				notices);

		}

		notices.add (
			type,
			notice);

	}

	public static
	void addNotice (
			@NonNull ServletRequest request,
			@NonNull String notice) {

		add (
			request,
			notice,
			ConsoleNoticeType.notice);

	}

	public static
	void addWarning (
			@NonNull ServletRequest request,
			@NonNull String notice) {

		add (
			request,
			notice,
			ConsoleNoticeType.warning);

	}

	public static
	void addError (
			@NonNull ServletRequest request,
			@NonNull String notice) {

		add (
			request,
			notice,
			ConsoleNoticeType.error);

	}

}
