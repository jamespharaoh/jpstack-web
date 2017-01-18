package wbs.web.context;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.genericCastUncheckedNullSafe;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import com.google.common.base.Optional;

import lombok.NonNull;

public
interface RequestContextSessionMethods
	extends RequestContextCoreMethods {

	default
	HttpSession session () {
		return request ().getSession ();
	}

	default
	Optional <Serializable> session (
			@NonNull String key) {

		return optionalFromNullable (
			genericCastUncheckedNullSafe (
				session ().getAttribute (
					key)));

	}

	default
	String sessionOrEmptyString (
			@NonNull String key) {

		return optionalOrEmptyString (
			genericCastUnchecked (
				session (
					key)));

	}

	default
	void session (
			@NonNull String key,
			Serializable object) {

		session ().setAttribute (
			key,
			object);

	}

	default
	String sessionId () {

		return session ().getId ();

	}

}
