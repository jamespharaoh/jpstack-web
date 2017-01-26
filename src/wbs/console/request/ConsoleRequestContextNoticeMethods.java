package wbs.console.request;

import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOrElse;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.List;

import lombok.NonNull;

import wbs.console.notice.ConsoleNoticeType;
import wbs.console.notice.ConsoleNotices;

import wbs.utils.string.FormatWriter;

public
interface ConsoleRequestContextNoticeMethods
	extends ConsoleRequestContextCoreMethods {

	// methods

	default
	void addNotice (
			@NonNull ConsoleNoticeType type,
			@NonNull String message) {

		State state =
			consoleRequestContextNoticeMethodsState ();

		state.notices.add (
			type,
			message);

	}

	default
	void addNotices (
			@NonNull ConsoleNotices notices) {

		notices.notices ().forEach (
			notice ->
				addNotice (
					notice.type (),
					notice.html ()));

	}

	default
	void addNotices (
			@NonNull List <String> notices) {

		notices.forEach (
			this::addNotice);

	}

	default
	void addNoticeFormat (
			@NonNull ConsoleNoticeType type,
			@NonNull String ... arguments) {

		addNotice (
			type,
			stringFormatArray (
				arguments));

	}

	default
	void addNotice (
			@NonNull String message) {

		addNotice (
			ConsoleNoticeType.notice,
			message);

	}

	default
	void addNoticeFormat (
			@NonNull String ... arguments) {

		addNoticeFormat (
			ConsoleNoticeType.notice,
			arguments);

	}

	default
	void addWarning (
			@NonNull String message) {

		addNotice (
			ConsoleNoticeType.warning,
			message);

	}

	default
	void addWarningFormat (
			@NonNull String ... arguments) {

		addNoticeFormat (
			ConsoleNoticeType.warning,
			arguments);

	}

	default
	void addError (
			@NonNull String message) {

		addNotice (
			ConsoleNoticeType.error,
			message);

	}

	default
	void addErrorFormat (
			@NonNull String ... arguments) {

		addNoticeFormat (
			ConsoleNoticeType.error,
			arguments);

	}

	default
	void flushNotices () {

		flushNotices (
			requestContext ().formatWriter ());

	}

	default
	void flushNotices (
			@NonNull FormatWriter formatWriter) {

		State state =
			consoleRequestContextNoticeMethodsState ();

		state.notices.flush (
			formatWriter);

	}

	// state

	final static
	String STATE_KEY =
		"CONSOLE_REQUEST_CONTEXT_NOTICE_METHODS_STATE";

	default
	State consoleRequestContextNoticeMethodsState () {

		return optionalOrElse (
			optionalCast (
				State.class,
				optionalFromNullable (
					requestContext ().request ().getAttribute (
						STATE_KEY))),
			() -> {

			State state =
				new State ();

			requestContext ().request ().setAttribute (
				STATE_KEY,
				state);

			return state;

		});

	}

	static
	class State {

		ConsoleNotices notices =
			new ConsoleNotices ();

	}

}
