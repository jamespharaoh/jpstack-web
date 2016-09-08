package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.LogicUtils.ifThenElse;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.NullUtils.nullIf;
import static wbs.framework.utils.etc.TypeUtils.isInstanceOf;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import lombok.NonNull;

public
class StringUtils {

	public static
	boolean stringIsEmpty (
			@NonNull String string) {

		return string.isEmpty ();

	}

	public static
	boolean stringIsNotEmpty (
			@NonNull String string) {

		return ! string.isEmpty ();

	}

	public static
	String emptyStringIfNull (
			String string) {

		return ifNull (
			string,
			"");

	}

	public static
	String nullIfEmptyString (
			String string) {

		return nullIf (
			string,
			"");

	}

	public static
	String joinWithSeparator (
			@NonNull String separator,
			@NonNull String prefix,
			@NonNull Iterable<String> parts,
			@NonNull String suffix) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		boolean first = true;

		for (
			String part
				: parts
		) {

			if (first) {

				first = false;

			} else {

				stringBuilder.append (
					separator);

			}

			stringBuilder.append (
				prefix);

			stringBuilder.append (
				part);

			stringBuilder.append (
				suffix);

		}

		return stringBuilder.toString ();

	}

	public static
	String joinWithoutSeparator (
			Iterable<String> parts) {

		return joinWithSeparator (
			"",
			"",
			parts,
			"");

	}

	public static
	String joinWithSeparator (
			String separator,
			Iterable<String> parts) {

		return joinWithSeparator (
			separator,
			"",
			parts,
			"");

	}

	public static
	String joinWithSeparator (
			@NonNull String separator,
			@NonNull String... parts) {

		return joinWithSeparator (
			separator,
			"",
			Arrays.asList (parts),
			"");

	}

	public static
	String joinWithoutSeparator (
			@NonNull String... parts) {

		return joinWithSeparator (
			"",
			"",
			Arrays.asList (parts),
			"");

	}

	public static
	String joinWithNewline (
			@NonNull Iterable<String> parts) {

		return joinWithSeparator (
			"\n",
			"",
			parts,
			"");

	}

	public static
	String joinWithNewline (
			@NonNull String ... parts) {

		return joinWithSeparator (
			"\n",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithSpace (
			@NonNull Iterable <String> parts) {

		return joinWithSeparator (
			" ",
			"",
			parts,
			"");

	}

	public static
	String joinWithSpace (
			String ... parts) {

		return joinWithSeparator (
			" ",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithFullStop (
			@NonNull Iterable <String> parts) {

		return joinWithSeparator (
			".",
			"",
			parts,
			"");

	}

	public static
	String joinWithFullStop (
			@NonNull String ... parts) {

		return joinWithSeparator (
			".",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithSlash (
			@NonNull Iterable <String> parts) {

		return joinWithSeparator (
			"/",
			"",
			parts,
			"");

	}

	public static
	String joinWithSlash (
			@NonNull String ... parts) {

		return joinWithSeparator (
			"/",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithPipe (
			@NonNull Iterable <String> parts) {

		return joinWithSeparator (
			"|",
			"",
			parts,
			"");

	}

	public static
	String joinWithPipe (
			@NonNull String ... parts) {

		return joinWithSeparator (
			"|",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithCommaAndSpace (
			@NonNull Iterable <String> parts) {

		return joinWithSeparator (
			", ",
			"",
			parts,
			"");

	}

	public static
	String joinWithCommaAndSpace (
			@NonNull String ... parts) {

		return joinWithSeparator (
			", ",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithSemicolonAndSpace (
			@NonNull Iterable <String> parts) {

		return joinWithSeparator (
			"; ",
			"",
			parts,
			"");

	}

	public static
	String joinWithSemicolonAndSpace (
			@NonNull String... parts) {

		return joinWithSeparator (
			"; ",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String substring (
			@NonNull String string,
			int start,
			int end) {

		return string.substring (
			start,
			end);

	}

	public static
	String substring (
			@NonNull String string,
			long start,
			long end) {

		return string.substring (
			Math.toIntExact (
				start),
			Math.toIntExact (
				end));

	}

	public static
	String substringFrom (
			@NonNull String string,
			int start) {

		return string.substring (
			start);

	}

	public static
	String substringFrom (
			@NonNull String string,
			long start) {

		return string.substring (
			Math.toIntExact (
				start));

	}

	public static
	String substringTo (
			@NonNull String string,
			int end) {

		return string.substring (
			0,
			end);

	}

	public static
	String substringTo (
			@NonNull String string,
			long end) {

		return string.substring (
			0,
			Math.toIntExact (
				end));

	}

	public static
	String hyphenToCamel (
			@NonNull String string) {

		return delimitedToCamel (
			string,
			"-");

	}

	public static
	String underscoreToCamel (
			@NonNull String string) {

		return delimitedToCamel (
			string,
			"_");

	}

	public static
	String replaceAll (
			@NonNull String source,
			@NonNull String find,
			@NonNull String replaceWith) {

		return source.replaceAll (
			Pattern.quote (
				find),
			replaceWith);

	}

	public static
	String underscoreToHyphen (
			@NonNull String string) {

		return replaceAll (
			string,
			"_",
			"-");

	}

	public static
	String hyphenToUnderscore (
			@NonNull String string) {

		return replaceAll (
			string,
			"-",
			"_");

	}

	public static
	String delimitedToCamel (
			@NonNull String string,
			@NonNull String delimiter) {

		String[] parts =
			string.split (delimiter);

		StringBuilder ret =
			new StringBuilder (parts [0]);

		for (
			int index = 1;
			index < parts.length;
			index ++
		) {

			ret.append (
				Character.toUpperCase (
					parts [index].charAt (0)));

			ret.append (
				parts [index].substring (1));

		}

		return ret.toString ();

	}

	public static
	String camelToUnderscore (
			String string) {

		return camelToDelimited (
			string,
			"_");

	}

	public static
	String camelToHyphen (
			String string) {

		return camelToDelimited (
			string,
			"-");

	}

	public static
	String camelToSpaces (
			String string) {

		return camelToDelimited (
			string,
			" ");

	}

	public static
	String camelToDelimited (
			String string,
			String delimiter) {

		if (string == null)
			return null;

		StringBuilder stringBuilder =
			new StringBuilder (
				string.length () * 2);

		stringBuilder.append (
			Character.toLowerCase (
				string.charAt (0)));

		for (
			int pos = 1;
			pos < string.length ();
			pos ++
		) {

			char ch =
				string.charAt (pos);

			if (Character.isUpperCase (ch)) {

				stringBuilder.append (
					delimiter);

				stringBuilder.append (
					Character.toLowerCase (ch));

			} else {

				stringBuilder.append (ch);

			}

		}

		return stringBuilder.toString ();

	}

	public static
	String spacify (
			String input) {

		return spacify (
			input,
			32);

	}

	private final static
	Pattern nonWhitespaceWordsPattern =
		Pattern.compile ("\\S+");

	/**
	 * Returns a transformed version of a string, with all whitespace replaced
	 * by a single space and all words longer than wordLength split into
	 * wordLength or less.
	 *
	 * @param input
	 *            the input string
	 * @param wordLength
	 *            the maximum word length in the output string
	 * @return the transformed string
	 */
	public static
	String spacify (
			String input,
			int wordLength) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		Matcher matcher =
			nonWhitespaceWordsPattern.matcher (input);

		while (matcher.find ()) {

			String string =
				matcher.group (0);

			int position = 0;

			int length =
				string.length ();

			while (true) {

				if (stringBuilder.length () > 0)
					stringBuilder.append (' ');

				if (position + wordLength > length) {

					stringBuilder.append (
						string,
						position,
						length);

					break;

				}

				stringBuilder.append (
					string,
					position,
					position + wordLength);

				position += wordLength;

			}

		}

		return stringBuilder.toString ();

	}

	public static
	List <String> stringSplitRegexp (
			@NonNull String pattern,
			@NonNull String source) {

		if (source.isEmpty ()) {

			return Collections.emptyList ();

		} else {

			return Arrays.asList (
				source.split (
					pattern));

		}

	}

	public static
	List <String> stringSplitSimple (
			@NonNull String separator,
			@NonNull String source) {

		if (source.isEmpty ()) {

			return Collections.emptyList ();

		} else {

			return Arrays.asList (
				source.split (
					Pattern.quote (
						separator)));

		}

	}

	public static
	List <String> stringSplitComma (
			@NonNull String source) {

		return stringSplitSimple (
			",",
			source);

	}

	public static
	List <String> stringSplitColon (
			@NonNull String source) {

		return stringSplitSimple (
			":",
			source);

	}

	public static
	List <String> stringSplitFullStop (
			@NonNull String source) {

		return stringSplitSimple (
			".",
			source);

	}

	public static
	List <String> stringSplitSlash (
			@NonNull String source) {

		return stringSplitSimple (
			"/",
			source);

	}

	public static
	List <String> stringSplitHyphen (
			@NonNull String source) {

		return stringSplitSimple (
			"-",
			source);

	}

	public static
	boolean doesNotStartWithSimple (
			@NonNull String string,
			@NonNull String prefix) {

		return ! string.startsWith (
			prefix);

	}

	public static
	byte[] stringToUtf8 (
			@NonNull String string) {

		return stringToBytes (
			string,
			"utf-8");

	}


	public static
	byte[] stringToBytes (
			@NonNull String string,
			@NonNull String charset) {

		try {

			return string.getBytes (
				charset);

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (exception);

		}

	}

	public static
	String bytesToString (
			byte[] bytes,
			String charset) {

		try {

			return new String (
				bytes,
				charset);

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (exception);

		}

	}

	public static
	String utf8ToString (
			@NonNull byte[] bytes) {

		return bytesToString (
			bytes,
			"utf-8");

	}

	private final static
	Pattern nonAlphanumericWordsPattern =
		Pattern.compile ("[^a-z0-9]+");

	public static
	String simplify (
			@NonNull String s) {

		return nonAlphanumericWordsPattern.matcher (
			s.toLowerCase()).replaceAll(
				" ").trim();

	}

	public static
	String objectToStringNullSafe (
			Object object) {

		if (object == null)
			return null;

		return object.toString ();

	}

	public static
	String objectToString (
			@NonNull Object object) {

		return object.toString ();

	}

	public static
	boolean equalIgnoreCase (
			@NonNull String... strings) {

		if (strings.length == 0)
			return true;

		String firstString =
			strings [0];

		for (
			String string
				: strings
		) {

			if (string == firstString)
				continue;

			if (! firstString.equalsIgnoreCase (string))
				return false;

		}

		return true;

	}

	public static
	String naivePluralise (
			@NonNull String singular) {

		if (
			stringEndsWith (
				singular,
				"s")
		) {

			return singular + "es";

		} else {

			return singular + "s";

		}

	}

	public static
	boolean stringStartsWithSimple (
			@NonNull String suffix,
			@NonNull String subject) {

		return subject.startsWith (
			suffix);

	}

	public static
	boolean stringEndsWith (
			@NonNull String suffix,
			@NonNull String subject) {

		return subject.endsWith (
			suffix);

	}

	public static
	String pluralise (
			long quantity,
			@NonNull String singularNoun,
			@NonNull String pluralNoun) {

		return quantity == 1L
			? "" + quantity + " " + singularNoun
			: "" + quantity + " " + pluralNoun;

	}

	public static
	String pluralise (
			long quantity,
			@NonNull String singularNoun) {

		return quantity == 1L

			? joinWithSpace (
				Long.toString (
					quantity),
				singularNoun)

			: joinWithSpace (
				Long.toString (
					quantity),
				naivePluralise (
					singularNoun));

	}

	public static
	String uppercase (
			@NonNull String string) {

		return string.toUpperCase ();

	}

	public static
	String lowercase (
			@NonNull String string) {

		return string.toLowerCase ();

	}

	public static
	String capitalise (
			@NonNull String string) {

		if (string.length () == 0)
			return "";

		return (
			Character.toUpperCase (
				string.charAt (0))
			+ string.substring (1)
		);

	}

	public static
	String uncapitalise (
			@NonNull String string) {

		if (string.length () == 0)
			return "";

		return (
			Character.toLowerCase (
				string.charAt (0))
			+ string.substring (1)
		);

	}

	public static
	String stringFormat (
			Object... arguments) {

		return StringFormatter.standardArray (
			arguments);

	}

	public static
	String stringFormatArray (
			Object[] args) {

		return stringFormat (
			args);

	}

	public static
	String stringFormatList (
			List <?> arguments) {

		return stringFormat (
			arguments.toArray (
				new Object [] {}));

	}

	public static
	String fixNewlines (
			@NonNull String input) {

		return input
			.replaceAll ("\r\n", "\n")
			.replaceAll ("\r", "\n");

	}

	public static
	Optional <String> emptyStringToAbsent (
			@NonNull String source) {

		if (source.isEmpty ()) {

			return Optional.absent ();

		} else {

			return Optional.of (
				source);

		}

	}

	public static
	boolean stringEqualSafe (
			@NonNull String string0,
			@NonNull String string1) {

		return string0.equals (
			string1);

	}

	public static
	boolean stringEqualSafe (
			@NonNull CharSequence string0,
			@NonNull CharSequence string1) {

		return string0.equals (
			string1);

	}

	public static
	boolean stringNotEqualSafe (
			@NonNull String string0,
			@NonNull String string1) {

		return ! string0.equals (
			string1);

	}

	public static
	boolean stringNotEqualSafe (
			@NonNull CharSequence string0,
			@NonNull CharSequence string1) {

		return ! string0.equals (
			string1);

	}

	public static
	boolean stringLongerThan (
			@NonNull Long length,
			@NonNull String string) {

		return string.length () > length;

	}

	public static
	boolean stringNotLongerThan (
			@NonNull Long length,
			@NonNull String string) {

		return string.length () <= length;

	}

	public static
	boolean stringShorterThan (
			@NonNull Long length,
			@NonNull String string) {

		return string.length () < length;

	}

	public static
	boolean stringNotShorterThan (
			@NonNull Long length,
			@NonNull String string) {

		return string.length () >= length;

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull Iterable <String> examples) {

		for (
			String example
				: examples
		) {

			if (
				value.equals (
					example)
			) {
				return true;
			}

		}

		return false;

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String... examples) {

		for (
			String example
				: examples
		) {

			if (
				value.equals (
					example)
			) {
				return true;
			}

		}

		return false;

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String example0) {

		return (

			value.equals (
				example0)

		);

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1) {

		return (

			value.equals (
				example0)

			|| value.equals (
				example1)

		);

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2) {

		return (

			value.equals (
				example0)

			|| value.equals (
				example1)

			|| value.equals (
				example2)

		);

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2,
			@NonNull String example3) {

		return (

			value.equals (
				example0)

			|| value.equals (
				example1)

			|| value.equals (
				example2)

			|| value.equals (
				example3)

		);

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2,
			@NonNull String example3,
			@NonNull String example4) {

		return (

			value.equals (
				example0)

			|| value.equals (
				example1)

			|| value.equals (
				example2)

			|| value.equals (
				example3)

			|| value.equals (
				example4)

		);

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String... examples) {

		for (
			String example
				: examples
		) {

			if (
				value.equals (
					example)
			) {
				return false;
			}

		}

		return true;

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String example0) {

		return (

			! value.equals (
				example0)

		);

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1) {

		return (

			! value.equals (
				example0)

			&& ! value.equals (
				example1)

		);

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2) {

		return (

			! value.equals (
				example0)

			&& ! value.equals (
				example1)

			&& ! value.equals (
				example2)

		);

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2,
			@NonNull String example3) {

		return (

			! value.equals (
				example0)

			&& ! value.equals (
				example1)

			&& ! value.equals (
				example2)

			&& ! value.equals (
				example3)

		);

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2,
			@NonNull String example3,
			@NonNull String example4) {

		return (

			! value.equals (
				example0)

			&& ! value.equals (
				example1)

			&& ! value.equals (
				example2)

			&& ! value.equals (
				example3)

			&& ! value.equals (
				example4)

		);

	}

	public static
	String stringReplaceAllSimple (
			@NonNull String from,
			@NonNull String to,
			@NonNull String subject) {

		return subject.replaceAll (
			Pattern.quote (
				from),
			to);

	}

	public static
	String stringReplaceAllRegex (
			@NonNull String fromPattern,
			@NonNull String to,
			@NonNull String subject) {

		return subject.replaceAll (
			fromPattern,
			to);

	}

	public static <Input>
	Function <Input, String> stringFormatLaterArray (
			@Nonnull Object[] arguments) {

		return new Function <Input, String> () {

			@Override
			public
			String apply (
					@NonNull Input input) {

				return stringFormatList (
					Arrays.stream (
						arguments)

					.map (
						argument ->
							ifThenElse (

							() -> isInstanceOf (
								String.class,
								argument),

								() ->
									argument,

							() -> isInstanceOf (
								Function.class,
								argument),

								() -> {

									@SuppressWarnings ("unchecked")
									Function <Input, ?> function =
										(Function <Input, ?>)
										argument;

									return function.apply (
										input);
								},

							() -> {
								throw new RuntimeException ();
							}))

					.collect (
						Collectors.toList ())

				);

			}

		};

	}

}
