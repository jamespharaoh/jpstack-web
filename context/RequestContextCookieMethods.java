package wbs.web.context;

import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrElse;

import java.util.Map;

import javax.servlet.http.Cookie;

import com.google.common.base.Optional;

import lombok.NonNull;

public
interface RequestContextCookieMethods
	extends RequestContextCoreMethods {

	default
	Map <String, Cookie> cookiesByName () {

		State state =
			requestContextCookieMethodsState ();

		if (
			isNull (
				state.cookiesByName)
		) {

			state.cookiesByName =
				mapWithDerivedKey (
					ifNull (
						request ().getCookies (),
						new Cookie [] {}),
					Cookie::getName);

		}

		return state.cookiesByName;

	}

	default
	Optional <String> cookie (
			@NonNull String key) {

		return optionalMapRequired (
			mapItemForKey (
				cookiesByName (),
				key),
			Cookie::getValue);

	}

	// state

	final static
	String STATE_KEY =
		"REQUEST_CONTEXT_COOKIE_METHODS_STATE";

	default
	State requestContextCookieMethodsState () {

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
		Map <String, Cookie> cookiesByName;
	}

}
