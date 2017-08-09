package wbs.console.request;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrThrow;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import lombok.NonNull;

public
interface ConsoleRequestContextFormMethods
	extends ConsoleRequestContextCoreMethods {

	default
	void formData (
			@NonNull String name,
			@NonNull String value) {

		formData (
			ImmutableMap.copyOf (
				formData ().entrySet ().stream ()

			.map (
				entry ->
					new SimpleEntry<> (
						entry.getKey (),
						stringEqualSafe (entry.getKey (), name)
							? value
							: entry.getValue ()))

			.collect (
				Collectors.toMap (
					Map.Entry::getKey,
					Map.Entry::getValue))

		));

	}

	default
	void hideFormData (
			@NonNull Set <String> keys) {

		formData (
			ImmutableMap.copyOf (
				Maps.filterKeys (
					formData (),
					Predicates.in (keys))));

	}

	default
	void hideFormData (
			@NonNull String ... keys) {

		hideFormData (
			ImmutableSet.copyOf (
				keys));

	}

	default
	Optional <String> form (
			@NonNull String key) {

		return optionalFromNullable (
			formData ().get (
				key));

	}

	default
	boolean formIsPresent (
			@NonNull String key) {

		return optionalIsPresent (
			form (key));

	}

	default
	String formRequired (
			@NonNull String key) {

		return optionalOrThrow (
			form (key),
			() -> new NoSuchElementException (
				stringFormat (
					"Form data does not contain key: %s",
					key)));


	}

	default
	String formRequiredFormat (
			@NonNull String ... keyArguments) {

		return formRequired (
			stringFormatArray (
				keyArguments));

	}

	default
	String formOrElse (
			@NonNull String key,
			@NonNull Supplier <String> defaultSupplier) {

		return optionalOrElseRequired (
			form (key),
			defaultSupplier);

	}

	default
	String formOrDefault (
			@NonNull String key,
			@NonNull String defaultValue) {

		return optionalOrElseRequired (
			form (key),
			() -> defaultValue);

	}

	default
	String formOrEmptyString (
			@NonNull String key) {

		return formOrElse (
			key,
			() -> "");

	}

	default
	void formData (
			@NonNull Map <String, String> newFormData) {

		requestContext ().request (
			"formData",
			newFormData);

	}

	default
	void setEmptyFormData () {

		requestContext ().request (
			"formData",
			Collections.emptyMap ());

	}

	default
	Map <String, String> formData () {

		Optional <Map <String, String>> formData =
			genericCastUnchecked (
				requestContext ().request (
					"formData"));

		if (
			optionalIsPresent (
				formData)
		) {

			return formData.get ();

		} else {

			return requestContext ().parameterMapSimple ();

		}

	}

}
