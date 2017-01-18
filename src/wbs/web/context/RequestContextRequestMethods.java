package wbs.web.context;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOr;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.google.common.base.Optional;

import org.apache.commons.io.IOUtils;

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
	InputStream inputStream () {

		try {

			return request ().getInputStream ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

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

}
