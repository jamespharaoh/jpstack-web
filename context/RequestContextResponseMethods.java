package wbs.web.context;

import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import lombok.NonNull;

import wbs.utils.io.BorrowedOutputStream;
import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.BorrowedFormatWriter;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

public
interface RequestContextResponseMethods
	extends RequestContextCoreMethods {

	default
	FormatWriter formatWriter () {

		State state =
			requestContextResponseMethodsState ();

		if (
			isNull (
				state.formatWriter)
		) {

			try {

				state.formatWriter =
					new WriterFormatWriter (
						new OutputStreamWriter (
							response ().getOutputStream (),
							"utf-8"));

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

		return new BorrowedFormatWriter (
			state.formatWriter);

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
	BorrowedOutputStream outputStream () {

		State state =
			requestContextResponseMethodsState ();

		if (
			isNull (
				state.outputStream)
		) {

			try {

				state.outputStream =
					response ().getOutputStream ();

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

		return new BorrowedOutputStream (
			state.outputStream);

	}

	default
	boolean canGetWriter () {

		try {

			formatWriter ();

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

	default
	void contentType (
			@NonNull String contentType) {

		response ().setContentType (
			contentType);

	}

	default
	void contentType (
			@NonNull String contentType,
			@NonNull String characterEncoding) {

		response ().setContentType (
			contentType);

		response ().setCharacterEncoding (
			characterEncoding);

	}

	default
	void characterEncoding (
			@NonNull String characterEncoding) {

		response ().setCharacterEncoding (
			characterEncoding);

	}

	default
	void contentLength (
			@NonNull Long contentLength) {

		response ().setContentLength (
			toJavaIntegerRequired (
				contentLength));

	}

	// state

	final static
	String STATE_KEY =
		"REQUEST_CONTEXT_RESPONSE_METHODS_STATE";

	default
	State requestContextResponseMethodsState () {

		return optionalOrElseRequired (
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
		OutputStream outputStream;
		FormatWriter formatWriter;
	}

}
