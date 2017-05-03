package wbs.web.context;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOr;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.google.common.base.Optional;

import org.apache.commons.io.IOUtils;

import wbs.utils.io.BorrowedInputStream;
import wbs.utils.io.RuntimeIoException;

public
interface RequestContextRequestMethods
	extends RequestContextCoreMethods {

	default
	String method () {
		return request ().getMethod ();
	}

	default
	boolean post () {

		return stringEqualSafe (
			method (),
			"POST");

	}

	default
	String requestUri () {
		return request ().getRequestURI ();
	}

	default
	String servletPath () {
		return request ().getServletPath ();
	}

	default
	Optional <String> pathInfo () {

		return optionalFromNullable (
			request ().getPathInfo ());

	}

	default
	String requestPath () {

		return stringFormat (
			"%s%s%s",
			request ().getContextPath (),
			servletPath (),
			optionalOr (
				pathInfo (),
				""));

	}

	default
	Optional <String> realIp () {

		return optionalFromNullable (
			request ().getHeader (
				"X-Real-IP"));

	}

	// request body

	default
	BorrowedInputStream inputStream () {

		State state =
			requestContextRequestMethodsState ();

		if (
			isNull (
				state.inputStream)
		) {

			try {

				state.inputStream =
					request ().getInputStream ();

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

		return new BorrowedInputStream (
			state.inputStream);

	}

	default
	Reader reader () {

		try {

			return request ().getReader ();

		} catch (IOException exception) {

			throw new RuntimeIoException (
				exception);

		}

	}

	default
	byte[] requestBodyRaw () {

		try {

			return IOUtils.toByteArray (
				inputStream ());

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	// state

	final static
	String STATE_KEY =
		"REQUEST_CONTEXT_REQUEST_METHODS_STATE";

	default
	State requestContextRequestMethodsState () {

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
		InputStream inputStream;
		Reader reader;
	}

}
