package wbs.console.notice;

import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

public
class ConsoleNotice {

	String html;
	ConsoleNoticeType type;

	public
	ConsoleNotice (
			@NonNull ConsoleNoticeType type,
			@NonNull String html) {

		this.type =
			type;

		this.html =
			html;

	}

	public
	String html () {
		return html;
	}

	public
	ConsoleNoticeType type () {
		return type;
	}

	@Override
	public
	String toString () {

		return stringFormat (
			"<p class=\"%h\">%s</p>",
			type.toString (),
			html);

	}

}
