package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.StringUtils.joinWithoutSeparator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

import com.google.common.base.Optional;

public
class CodeUtils {

	public static
	Optional<String> simplifyToCode (
			@NonNull String string) {

		String code =
			string
				.toLowerCase ()
				.replaceAll ("[^a-z0-9]+", " ")
				.trim ()
				.replaceAll (" ", "_");

		if (
			isValidCode (
				code)
		) {

			return Optional.<String>of (
				code);

		} else {

			return Optional.<String>absent ();

		}

	}

	public static
	String simplifyToCodeRequired (
			@NonNull String string) {

		Optional<String> optionalCode =
			simplifyToCode (
				string);

		if (
			OptionalUtils.isPresent (
				optionalCode)
		) {

			return optionalCode.get ();

		} else {

			throw new IllegalArgumentException (
				"Invalid code");

		}


	}

	public static
	boolean isValidCode (
			@NonNull String string) {

		Matcher matcher =
			codePattern.matcher (
				string);

		return matcher.matches ();

	}

	public static
	boolean isNotValidCode (
			@NonNull String string) {

		return ! isValidCode (
			string);

	}

	public static
	boolean isValidRelaxedCode (
			@NonNull String string) {

		Matcher matcher =
			relaxedCodePattern.matcher (
				string);

		return matcher.matches ();

	}

	public static
	boolean isNotValidRelaxedCode (
			@NonNull String string) {

		return ! isValidRelaxedCode (
			string);

	}

	public static final
	Pattern codePattern =
		Pattern.compile (
			joinWithoutSeparator (
				"^",
				"([a-z][a-z0-9]*)",
				"(_([a-z0-9]+))*",
				"$"));

	public static final
	Pattern relaxedCodePattern =
		Pattern.compile (
			joinWithoutSeparator (
				"^",
				"([a-z0-9]+)",
				"(_([a-z0-9]+))*",
				"$"));

}
