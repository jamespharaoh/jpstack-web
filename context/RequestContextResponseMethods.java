package wbs.web.context;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOrElse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import lombok.NonNull;

import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

public
interface RequestContextResponseMethods
	extends RequestContextCoreMethods {

	default
	PrintWriter printWriter () {

		State state =
			requestContextResponseMethodsState ();

		if (
			isNull (
				state.printWriter)
		) {

			response ().setCharacterEncoding (
				"utf-8");

			try {

				state.printWriter =
					response ().getWriter ();

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

		return state.printWriter;

	}

	default
	FormatWriter formatWriter () {

		State state =
			requestContextResponseMethodsState ();

		if (
			isNull (
				state.formatWriter)
		) {

			state.formatWriter =
				new WriterFormatWriter (
					printWriter ());

		}

		return state.formatWriter;

	}

	default
	void sendError (
			@NonNull Long statusCode) {

		try {

			response ().sendError (
				toJavaIntegerRequired (
					statusCode));

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	default
	void sendError (
			@NonNull Long statusCode,
			@NonNull String message) {

		try {

			response ().sendError (
				toJavaIntegerRequired (
					statusCode),
				message);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	default
	void sendRedirect (
			@NonNull String location) {

		try {

			response ().sendRedirect (
				location);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	default
	OutputStream outputStream () {

		try {

			return response ().getOutputStream ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	default
	boolean canGetWriter () {

		try {

			printWriter ();

			return true;

		} catch (IllegalStateException illegalStateException) {

			return false;

		}

	}

	default
	boolean isCommitted () {
		return response ().isCommitted ();
	}

	default
	void status (
			@NonNull Long status) {

		response ().setStatus (
			toJavaIntegerRequired (
				status));

	}

	default
	void reset () {
		response ().reset ();
	}

	// state

	final static
	String STATE_KEY =
		"REQUEST_CONTEXT_RESPONSE_METHODS_STATE";

	default
	State requestContextResponseMethodsState () {

		return optionalOrElse (
			optionalCast (
				State.class,
				optionalFromNullable (
					request ().getAttribute (
						STATE_KEY))),
			() -> {

			State state =
				new State ();

			request ().setAttribute (
				STATE_KEY,
				state);

			return state;

		});

	}

	static
	class State {
		PrintWriter printWriter;
		FormatWriter formatWriter;
	}

}
