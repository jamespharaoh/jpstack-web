package wbs.utils.web;

import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttributesWrite;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;
import wbs.utils.web.HtmlAttributeUtils.HtmlAttribute;

public
class HtmlFormUtils {

	// form open

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

	// form open method action

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
	void htmlFormOpenGetAction (
			@NonNull FormatWriter formatWriter,
			@NonNull String action,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodAction (
			formatWriter,
			"get",
			action,
			attributes);

	}

	public static
	void htmlFormOpenGetAction (
			@NonNull String action,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodAction (
			currentFormatWriter (),
			"get",
			action,
			attributes);

	}

	public static
	void htmlFormOpenPostAction (
			@NonNull FormatWriter formatWriter,
			@NonNull String action,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodAction (
			formatWriter,
			"post",
			action,
			attributes);

	}

	public static
	void htmlFormOpenPostAction (
			@NonNull String action,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodAction (
			currentFormatWriter (),
			"post",
			action,
			attributes);

	}

	// form open method

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
	void htmlFormOpenPost (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethod (
			formatWriter,
			"post",
			attributes);

	}

	public static
	void htmlFormOpenPost (
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethod (
			currentFormatWriter (),
			"post",
			attributes);

	}

	// form open method action encoding

	public static
	void htmlFormOpenMethodActionEncoding (
			@NonNull FormatWriter formatWriter,
			@NonNull String method,
			@NonNull String action,
			@NonNull String encoding,
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

		formatWriter.writeFormat (
			" enctype=\"%h\"",
			encoding);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlFormOpenMethodActionEncoding (
			@NonNull String method,
			@NonNull String action,
			@NonNull String encoding,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodActionEncoding (
			currentFormatWriter (),
			method,
			action,
			encoding,
			attributes);

	}

	public static
	void htmlFormOpenPostActionEncoding (
			@NonNull FormatWriter formatWriter,
			@NonNull String action,
			@NonNull String encoding,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodActionEncoding (
			formatWriter,
			"post",
			action,
			encoding,
			attributes);

	}

	public static
	void htmlFormOpenPostActionEncoding (
			@NonNull String action,
			@NonNull String encoding,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodActionEncoding (
			currentFormatWriter (),
			"post",
			action,
			encoding,
			attributes);

	}

	public static
	void htmlFormOpenMethodActionMultipart (
			@NonNull FormatWriter formatWriter,
			@NonNull String method,
			@NonNull String action,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodActionEncoding (
			formatWriter,
			method,
			action,
			"multipart/form-data",
			attributes);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlFormOpenMethodActionMultipart (
			@NonNull String method,
			@NonNull String action,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodActionMultipart (
			currentFormatWriter (),
			method,
			action,
			attributes);

	}

	public static
	void htmlFormOpenPostActionMultipart (
			@NonNull FormatWriter formatWriter,
			@NonNull String action,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodActionMultipart (
			formatWriter,
			"post",
			action,
			attributes);

	}

	public static
	void htmlFormOpenPostActionMultipart (
			@NonNull String action,
			@NonNull HtmlAttribute ... attributes) {

		htmlFormOpenMethodActionMultipart (
			currentFormatWriter (),
			"post",
			action,
			attributes);

	}

	// form close

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
