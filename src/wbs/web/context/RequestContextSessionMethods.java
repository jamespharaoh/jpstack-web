package wbs.web.context;

import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.genericCastUncheckedNullSafe;

import java.io.Serializable;
import java.util.function.Supplier;

import javax.servlet.http.HttpSession;

import com.google.common.base.Optional;

import lombok.NonNull;

public
interface RequestContextSessionMethods
	extends RequestContextCoreMethods {

	@Deprecated
	default
	HttpSession session () {
		return request ().getSession ();
	}

	@Deprecated
	default
	Optional <Serializable> session (
			@NonNull String key) {

		return optionalFromNullable (
			genericCastUncheckedNullSafe (
				session ().getAttribute (
					key)));

	}

	@Deprecated
	default
	Serializable sessionRequired (
			@NonNull String key) {

		return requiredValue (
			genericCastUncheckedNullSafe (
				session ().getAttribute (
					key)));

	}

	@Deprecated
	default
	Serializable sessionOrElseSetRequired (
			@NonNull String key,
			@NonNull Supplier <? extends Serializable> supplier) {

		Optional <Serializable> valueOptional =
			optionalFromNullable (
				genericCastUncheckedNullSafe (
					session ().getAttribute (
						key)));

		if (
			optionalIsPresent (
				valueOptional)
		) {

			return optionalGetRequired (
				valueOptional);

		} else {

			Serializable value =
				requiredValue (
					supplier.get ());

			session ().setAttribute (
				key,
				value);

			return value;

		}

	}

	@Deprecated
	default
	String sessionOrEmptyString (
			@NonNull String key) {

		return optionalOrEmptyString (
			genericCastUnchecked (
				session (
					key)));

	}

	@Deprecated
	default
	void session (
			@NonNull String key,
			Serializable object) {

		session ().setAttribute (
			key,
			object);

	}

	@Deprecated
	default
	String sessionId () {

		return session ().getId ();

	}

}
