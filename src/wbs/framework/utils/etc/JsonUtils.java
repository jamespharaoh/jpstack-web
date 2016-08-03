package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.Misc.stringToBytes;

import java.io.StringWriter;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.json.simple.JSONValue;

import wbs.framework.utils.etc.JsonUtils.EscapeOptions.EscapeOptionsBuilder;

public
class JsonUtils {

	public static
	byte[] jsonToBytes (
			@NonNull Object object) {

		return stringToBytes (
			JSONValue.toJSONString (
				object),
			"utf-8");

	}

	public static
	String escapeJsonString (
			@NonNull EscapeOptions escapeOptions,
			@NonNull String source) {

		StringWriter stringWriter =
			new StringWriter ();

		FormatWriter formatWriter =
			new FormatWriterWriter (
				stringWriter);

		writeEscapedJsonString (
			formatWriter,
			escapeOptions,
			source);

		return stringWriter.toString ();

	}

	public static
	void writeEscapedJsonString (
			@NonNull FormatWriter formatWriter,
			@NonNull EscapeOptions escapeOptions,
			@NonNull String source) {

		source.chars ().forEach (
			character -> {

			switch (character) {

			case '"':

				formatWriter.writeString (
					escapeOptions.doubleQuote);

				break;

			case '\'':

				formatWriter.writeString (
					escapeOptions.singleQuote);

				break;

			case '\n':

				formatWriter.writeString (
					"\\n");

				break;

			case '\r':

				formatWriter.writeString (
					"\\r");

				break;

			case '/':

				formatWriter.writeString (
					escapeOptions.forwardSlash);

				break;

			case '\\':

				formatWriter.writeString (
					"\\\\");

				break;

			case '<':

				formatWriter.writeString (
					escapeOptions.lessThan);

				break;

			case '>':

				formatWriter.writeString (
					escapeOptions.greaterThan);

				break;

			case '&':

				formatWriter.writeString (
					escapeOptions.ampersand);

				break;

			default:

				formatWriter.writeCharacter (
					character);

			}

		});

	}

	@Accessors (fluent = true)
	@Builder
	static
	class EscapeOptions {

		String singleQuote;
		String doubleQuote;

		String forwardSlash;

		String lessThan;
		String greaterThan;
		String ampersand;

	}

	// default options

	public final static
	EscapeOptions defaultOptions =
		new EscapeOptionsBuilder ()
			.singleQuote ("\\'")
			.doubleQuote ("\\\"")
			.forwardSlash ("/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

	public final static
	EscapeOptions defaultDoubleQuotesOptions =
		new EscapeOptionsBuilder ()
			.singleQuote ("'")
			.doubleQuote ("\\\"")
			.forwardSlash ("/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

	public final static
	EscapeOptions defaultSingleQuotesOptions =
		new EscapeOptionsBuilder ()
			.singleQuote ("\\'")
			.doubleQuote ("\"")
			.forwardSlash ("/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

	// html options

	public final static
	EscapeOptions htmlDefaultOptions =
		new EscapeOptionsBuilder ()
			.singleQuote ("\\'")
			.doubleQuote ("\\\"")
			.forwardSlash ("/")
			.lessThan ("&lt;")
			.greaterThan ("&gt;")
			.ampersand ("&amp;")
			.build ();

	public final static
	EscapeOptions htmlDoubleQuotesOptions =
		new EscapeOptionsBuilder ()
			.singleQuote ("'")
			.doubleQuote ("\\\"")
			.forwardSlash ("/")
			.lessThan ("&lt;")
			.greaterThan ("&gt;")
			.ampersand ("&amp;")
			.build ();

	public final static
	EscapeOptions htmlSingleQuotesOptions =
		new EscapeOptionsBuilder ()
			.singleQuote ("\\'")
			.doubleQuote ("\"")
			.forwardSlash ("/")
			.lessThan ("&lt;")
			.greaterThan ("&gt;")
			.ampersand ("&amp;")
			.build ();

	// html script options

	public final static
	EscapeOptions htmlScriptDefaultOptions =
		new EscapeOptionsBuilder ()
			.singleQuote ("\\'")
			.doubleQuote ("\\\"")
			.forwardSlash ("\\/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

	public final static
	EscapeOptions htmlScriptDoubleQuotesOptions =
		new EscapeOptionsBuilder ()
			.singleQuote ("'")
			.doubleQuote ("\\\"")
			.forwardSlash ("\\/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

	public final static
	EscapeOptions htmlScriptSingleQuotesOptions =
		new EscapeOptionsBuilder ()
			.singleQuote ("\\'")
			.doubleQuote ("\"")
			.forwardSlash ("\\/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

}
