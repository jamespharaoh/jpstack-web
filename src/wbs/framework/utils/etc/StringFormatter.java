package wbs.framework.utils.etc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public
class StringFormatter {

	private final
	Map<Character,Conversion> conversions;

	public
	StringFormatter (
			Map<Character,Conversion> newConversions) {

		conversions =
			newConversions;

	}

	/**
	 * Finds a series of formats and their args and calls formatReal () with
	 * them, returning the resulting strings in a list.
	 */
	public
	List<String> formatSpecial (
			List<Object> arguments) {

		List<String> stringsToReturn =
			new ArrayList<String> ();

		for (
			int argumentIndex = 0;
			argumentIndex < arguments.size ();
			argumentIndex ++
		) {

			String format =
				(String)
				arguments.get (argumentIndex);

			int numPercents =
				numPercents (format);

			stringsToReturn.add (
				formatReal (
					format,
					arguments.subList (
						argumentIndex + 1,
						argumentIndex + 1 + numPercents)));

			argumentIndex +=
				numPercents;

		}

		return stringsToReturn;

	}

	public
	String formatReal (
			String format,
			List<?> argumenta) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		int searchPosition = 0;

		Iterator<?> iterator =
			argumenta.iterator ();

		for (;;) {

			// find the next %

			int percentPosition =
				format.indexOf (
					'%',
					searchPosition);

			if (percentPosition < 0) {

				stringBuilder.append (
					format,
					searchPosition,
					format.length ());

				return stringBuilder.toString ();

			}

			// append the text leading up to the % bit

			stringBuilder.append (
				format,
				searchPosition,
				percentPosition);

			// get the next character

			if (percentPosition + 2 > format.length ()) {

				throw new RuntimeException (
					"Invalid format string - single % at end");

			}

			char formatCharacter =
				format.charAt (
					percentPosition + 1);

			// a double % inserts a single %

			if (formatCharacter == '%') {

				stringBuilder.append (
					'%');

				searchPosition =
					percentPosition + 2;

				continue;

			}

			// lookup the conversion

			Conversion conversion =
				conversions.get (
					formatCharacter);

			if (conversion == null) {

				throw new RuntimeException (
					"Invalid format char: " + formatCharacter);

			}

			// append the converted string

			stringBuilder.append (
				conversion.convert (
					iterator.next ()));

			searchPosition =
				percentPosition + 2;

		}

	}

	public
	String format (
			Object... args) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		for (String string
				: formatSpecial (
					Arrays.asList (args))) {

			stringBuilder.append (
				string);

		}

		return stringBuilder.toString ();

	}

	public static
	int numPercents (
			String format) {

		int position = 0;
		int count = 0;

		for (;;) {

			position =
				format.indexOf (
					'%',
					position);

			if (position < 0)
				return count;

			if (position + 2 > format.length ()) {

				throw new RuntimeException (
					"Lone % at end of format string");

			}

			if (format.charAt (position + 1) != '%') {

				count ++;

			}

			position += 2;

		}

	}

	static
	interface Conversion {

		String convert (
			Object arg);

	}

	public static
	class StringConversion
		implements Conversion {

		@Override
		public
		String convert (
				Object arg) {

			return arg != null
				? arg.toString ()
				: "";

		}

	}

	public static
	class HtmlConversion
		implements Conversion {

		@Override
		public
		String convert (
				Object source) {

			return source != null
				? Html.encode (source)
				: "";

		}

	}

	public static
	class JavaScriptConversion
		implements Conversion {

		@Override
		public
		String convert (
				Object argument) {

			return argument != null
				? Html.javascriptStringEscape (
					argument.toString ())
					: "";

		}

	}

	public static
	class UrlConversion
		implements Conversion {

		@Override
		public
		String convert (
				Object argument) {

			return argument != null
				? Html.urlEncode (argument.toString ())
				: "";

		}

	}

	public static
	class DecimalConversion
		implements Conversion {

		@Override
		public
		String convert (
				Object argument) {

			return argument != null
				? Html.encode (((Number) argument).toString ())
				: "";

		}

	}

	private final static
	Map<Character,Conversion> standardConversions =
		ImmutableMap.<Character,Conversion>builder ()
			.put ('d', new DecimalConversion ())
			.put ('s', new StringConversion ())
			.put ('h', new HtmlConversion ())
			.put ('j', new JavaScriptConversion ())
			.put ('u', new UrlConversion ())
			.build ();

	public final static
	StringFormatter standardStringFormatter =
		new StringFormatter (
			standardConversions);

	public static
	String standard (
			Object... args) {

		return standardStringFormatter.format (
			args);

	}

	public static
	void printWriterFormat (
			PrintWriter printWriter,
			Object... args) {

		printWriter.print (
			standard (args));

	}

}
