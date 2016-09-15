package wbs.utils.web;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalEqualOrNotPresentSafe;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;
import static wbs.utils.string.StringUtils.replaceAll;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttributesWrite;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;
import wbs.utils.web.HtmlAttributeUtils.HtmlAttribute;

public
class HtmlUtils {

	public static
	String htmlEncode (
			@NonNull String source) {

		StringBuilder dest =
			new StringBuilder (
				source.length () * 2);

		for (
			int index = 0;
			index < source.length ();
			index ++
		) {

			char character =
				source.charAt (
					index);

			switch (character) {

			case '<':

				dest.append (
					"&lt;");

				break;

			case '>':

				dest.append (
					"&gt;");

				break;

			case '&':

				dest.append (
					"&amp;");

				break;

			case '"':

				dest.append (
					"&quot;");

				break;

			default:

				if (character < 128) {

					dest.append (
						character);

				} else {

					dest.append (
						"&#");

					dest.append (
						(int) character);

					dest.append (
						';');

				}

			}

		}

		return dest.toString ();

	}

	public static
	String javascriptStringEscape (
			String source) {

		if (source == null)
			return null;

		StringBuilder dest =
			new StringBuilder (
				source.length () * 2);

		for (
			int pos = 0;
			pos < source.length ();
			pos++
		) {

			char ch =
				source.charAt (pos);

			switch (ch) {

			case '"':
				dest.append ("\\\"");
				break;

			case '\'':
				dest.append ("\\'");
				break;

			case '\n':
				dest.append ("\\n");
				break;

			case '\r':
				dest.append ("\\r");
				break;

			case '/':
				dest.append ("\\/");
				break;

			default:
				dest.append (ch);

			}

		}

		return dest.toString ();

	}

	public static
	String htmlJavascriptEncode (
			@NonNull String string) {

		return htmlEncode (
			javascriptStringEscape (
				string));

	}

	public static
	String htmlNonBreakingWhitespace (
			String source) {

		return source.replaceAll (
			" ",
			"&nbsp;");

	}

	public static
	String htmlEncodeNonBreakingWhitespace (
			String source) {

		return htmlNonBreakingWhitespace (
			htmlEncode (
				source));

	}

	public static
	String newlineToBr (
			String source) {

		return source.replaceAll (
			"\r\n|\n|\r",
			"<br>");

	}

	public static
	String encodeNewlineToBr (
			@NonNull String source) {

		return newlineToBr (
			htmlEncode (
				source));

	}

	public static
	String urlQueryParameterEncode (
			@NonNull String source) {

		try {

			return URLEncoder.encode (
				source,
				"utf-8");

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	String urlPathElementEncode (
			@NonNull String source) {

		try {

			return replaceAll (
				URLEncoder.encode (
					source,
					"utf-8"),
				"+",
				"%20");

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (
				exception);

		}
	}

	public static
	String htmlColourFromInteger (
			@NonNull Long number) {

		return String.format (
			"#%06x",
			number & 0x0000000000ffffffl);

	}

	public static
	String htmlColourFromObject (
			@NonNull Object object) {

		return htmlColourFromInteger (
			(long) (int)
			object.hashCode ());

	}

	public static
	void htmlWriteAttributesFromMap (
			@NonNull FormatWriter formatWriter,
			@NonNull Map <String, String> map) {

		for (
			Map.Entry <String, String> entry
				: map.entrySet ()
		) {

			formatWriter.writeFormat (
				"%s=\"%h\"",
				entry.getKey (),
				entry.getValue ());

		}

	}

	public static
	void htmlWriteAttributesFromMap (
			@NonNull Map <String, String> map) {

		htmlWriteAttributesFromMap (
			currentFormatWriter (),
			map);

	}

	// html div

	public static
	void htmlDivOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<div");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlDivOpen (
			@NonNull HtmlAttribute ... attributes) {

		htmlDivOpen (
			currentFormatWriter (),
			attributes);

	}

	public static
	void htmlDivClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</div>");

	}

	public static
	void htmlDivClose () {

		htmlDivClose (
			currentFormatWriter ());

	}

	// html paragraphs

	public static
	void htmlParagraphOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<p");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlParagraphOpen (
			@NonNull HtmlAttribute ... attributes) {

		htmlParagraphOpen (	
			currentFormatWriter (),
			attributes);

	}

	public static
	void htmlParagraphClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</p>");

	}

	public static
	void htmlParagraphClose () {

		htmlParagraphClose (	
			currentFormatWriter ());

	}

	// html heading

	public static
	void htmlHeadingOneWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String label) {

		formatWriter.writeLineFormat (
			"<h1>%h</h1>",
			label);

	}

	public static
	void htmlHeadingOneWrite (
			@NonNull String label) {

		htmlHeadingOneWrite (
			currentFormatWriter (),
			label);

	}

	public static
	void htmlHeadingTwoWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String label) {

		formatWriter.writeLineFormat (
			"<h2>%h</h2>",
			label);

	}

	public static
	void htmlHeadingTwoWrite (
			@NonNull String label) {

		htmlHeadingTwoWrite (
			currentFormatWriter (),
			label);

	}

	public static
	void htmlHeadingThreeWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String label) {

		formatWriter.writeLineFormat (
			"<h3>%h</h3>",
			label);

	}

	public static
	void htmlHeadingThreeWrite (
			@NonNull String label) {

		htmlHeadingThreeWrite (
			currentFormatWriter (),
			label);

	}

	public static
	void htmlHeadingFourWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String label) {

		formatWriter.writeLineFormat (
			"<h4>%h</h4>",
			label);

	}

	public static
	void htmlHeadingFourWrite (
			@NonNull String label) {

		htmlHeadingFourWrite (
			currentFormatWriter (),
			label);

	}

	// html link

	public static
	void htmlLinkWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String href,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<a href=\"%h\"",
			href);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%h</a>",
			content);

		formatWriter.writeNewline ();

	}

	public static
	void htmlLinkWrite (
			@NonNull String href,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlLinkWrite (
			currentFormatWriter (),
			href,
			content,
			attributes);

	}

	public static
	void htmlLinkWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String href,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<a href=\"%h\"",
			href);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%s</a>",
			content);

		formatWriter.writeNewline ();

	}

	public static
	void htmlLinkWriteHtml (
			@NonNull String href,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlLinkWriteHtml (
			currentFormatWriter (),
			href,
			content,
			attributes);

	}

	public static
	void htmlLinkWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String href,
			@NonNull Runnable content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<a href=\"%h\"",
			href);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		content.run ();

		formatWriter.writeFormat (
			"</a>");

		formatWriter.writeNewline ();

	}

	public static
	void htmlLinkWriteHtml (
			@NonNull String href,
			@NonNull Runnable content,
			@NonNull HtmlAttribute ... attributes) {

		htmlLinkWriteHtml (
			currentFormatWriter (),
			href,
			content,
			attributes);

	}

	// html form

	public static
	void htmlFormOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<form");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlFormOpen (
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpen (
			currentFormatWriter (),
			attributes);

	}

	public static
	void htmlFormOpenMethodAction (
			@NonNull FormatWriter formatWriter,
			@NonNull String method,
			@NonNull String action,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<form");

		formatWriter.writeFormat (
			" method=\"%h\"",
			method);

		formatWriter.writeFormat (
			" action=\"%h\"",
			action);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlFormOpenMethodAction (
			@NonNull String method,
			@NonNull String action,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodAction (
			currentFormatWriter (),
			method,
			action,
			attributes);

	}

	public static
	void htmlFormOpenMethod (
			@NonNull FormatWriter formatWriter,
			@NonNull String method,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<form");

		formatWriter.writeFormat (
			" method=\"%h\"",
			method);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlFormOpenMethod (
			@NonNull String method,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethod (
			currentFormatWriter (),
			method,
			attributes);

	}

	public static
	void htmlFormClose (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</form>");

	}

	public static
	void htmlFormClose (
			@NonNull HtmlAttribute ... attributes) {

		htmlFormClose (
			currentFormatWriter (),
			attributes);

	}

}
