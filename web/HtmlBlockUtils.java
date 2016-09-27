package wbs.utils.web;

import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;
import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttributesWrite;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;

import java.util.Arrays;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;
import wbs.utils.web.HtmlAttributeUtils.HtmlAttribute;

public
class HtmlBlockUtils {

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
			Arrays.asList (
				attributes));

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

	public static
	void htmlDivWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlDivOpen (
			formatWriter,
			attributes);

		formatWriter.writeLineFormat (
			"%h",
			content);

		htmlDivClose (
			formatWriter);

	}

	public static
	void htmlDivWrite (
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlDivWrite (
			currentFormatWriter (),
			content,
			attributes);

	}

	// html spans

	public static
	void htmlSpanOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<span");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlSpanOpen (
			@NonNull HtmlAttribute ... attributes) {

		htmlSpanOpen (	
			currentFormatWriter (),
			attributes);

	}

	public static
	void htmlSpanClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</span>");

	}

	public static
	void htmlSpanClose () {

		htmlSpanClose (	
			currentFormatWriter ());

	}

	public static
	void htmlSpanWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlSpanOpen (
			formatWriter,
			attributes);

		formatWriter.writeLineFormat (
			"%h",
			content);

		htmlSpanClose (
			formatWriter);

	}

	public static
	void htmlSpanWrite (
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlSpanWrite (
			currentFormatWriter (),
			content,
			attributes);

	}

	public static
	void htmlSpanWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String content) {

		htmlSpanOpen (
			formatWriter);

		formatWriter.writeLineFormat (
			"%s",
			content);

		htmlSpanClose (
			formatWriter);

	}

	public static
	void htmlSpanWriteHtml (
			@NonNull String content) {

		htmlSpanWriteHtml (
			currentFormatWriter (),
			content);

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

	public static
	void htmlParagraphWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlParagraphOpen (
			formatWriter,
			attributes);

		formatWriter.writeLineFormat (
			"%h",
			content);

		htmlParagraphClose (
			formatWriter);

	}

	public static
	void htmlParagraphWrite (
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlParagraphWrite (
			currentFormatWriter (),
			content,
			attributes);

	}

	public static
	void htmlParagraphWriteFormat (
			@NonNull FormatWriter formatWriter,
			@NonNull Object ... arguments) {

		htmlParagraphOpen (
			formatWriter);

		formatWriter.writeLineFormat (
			"%h",
			stringFormatArray (
				arguments));

		htmlParagraphClose (
			formatWriter);

	}

	public static
	void htmlParagraphWriteFormat (
			@NonNull Object ... arguments) {

		htmlParagraphWriteFormat (
			currentFormatWriter (),
			arguments);

	}

	public static
	void htmlParagraphWriteFormatWarning (
			@NonNull FormatWriter formatWriter,
			@NonNull Object ... arguments) {

		htmlParagraphWrite (
			formatWriter,
			stringFormatArray (
				arguments),
			htmlClassAttribute (
				"warning"));

	}

	public static
	void htmlParagraphWriteFormatWarning (
			@NonNull Object ... arguments) {

		htmlParagraphWrite (
			currentFormatWriter (),
			stringFormatArray (
				arguments),
			htmlClassAttribute (
				"warning"));

	}

	public static
	void htmlParagraphWriteFormatError (
			@NonNull FormatWriter formatWriter,
			@NonNull Object ... arguments) {

		htmlParagraphWrite (
			formatWriter,
			stringFormatArray (
				arguments),
			htmlClassAttribute (
				"error"));

	}

	public static
	void htmlParagraphWriteFormatError (
			@NonNull Object ... arguments) {

		htmlParagraphWrite (
			currentFormatWriter (),
			stringFormatArray (
				arguments),
			htmlClassAttribute (
				"error"));

	}

	public static
	void htmlParagraphWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlParagraphOpen (
			formatWriter,
			attributes);

		formatWriter.writeLineFormat (
			"%s",
			content);

		htmlParagraphClose (
			formatWriter);

	}

	public static
	void htmlParagraphWriteHtml (
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlParagraphWriteHtml (
			currentFormatWriter (),
			content,
			attributes);

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

}
