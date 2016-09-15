package wbs.utils.web;

import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;
import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttributesWrite;

import java.util.function.Consumer;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;
import wbs.utils.web.HtmlAttributeUtils.HtmlAttribute;

public
class HtmlTableUtils {

	public static
	void htmlTableHeaderRowWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String ... headerLabels) {

		htmlTableRowOpen (
			formatWriter);

		String currentLabel = null;
		long currentColumnSpan = 0;

		for (
			String headerLabel
				: headerLabels
		) {

			if (currentLabel == null) {

				if (headerLabel == null) {
					throw new RuntimeException ();
				}

				currentLabel =
					headerLabel;

				currentColumnSpan = 1;

			} else if (headerLabel == null) { 

				currentColumnSpan ++;

			} else {

				if (currentColumnSpan > 1) {

					formatWriter.writeLineFormat (
						"<th colspan=\"%h\">%h</th>",
						currentColumnSpan,
						currentLabel);

				} else {
	
					formatWriter.writeLineFormat (
						"<td>%h</th>",
						headerLabel);

				}

				currentLabel =
					headerLabel;

				currentColumnSpan = 1;

			}

			if (currentColumnSpan > 1) {

				formatWriter.writeLineFormat (
					"<th colspan=\"%h\">%h</th>",
					currentColumnSpan,
					currentLabel);

			} else {

				formatWriter.writeLineFormat (
					"<td>%h</th>",
					headerLabel);

			}

		}

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableHeaderRowWrite (
			@NonNull String ... headerLabels) {

		htmlTableHeaderRowWrite (
			currentFormatWriter (),
			headerLabels);

	}

	public static
	void htmlTableHeaderRowWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String ... headerLabels) {

		htmlTableRowOpen (
			formatWriter);

		for (
			String headerLabel
				: headerLabels
		) {

			formatWriter.writeLineFormat (
				"<th>%s</th>",
				headerLabel);

		}

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableHeaderRowHtml (
			@NonNull String ... headerLabels) {

		htmlTableHeaderRowWriteHtml (
			currentFormatWriter (),
			headerLabels);

	}

	public static
	void htmlTableOpen (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<table>");

		formatWriter.increaseIndent ();

	}

	public static
	void htmlOpenTable () {

		htmlTableOpen (
			currentFormatWriter ());

	}

	public static
	void htmlOpenTableDetails (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<table class=\"details\">");

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableOpenDetails () {

		htmlOpenTableDetails (
			currentFormatWriter ());

	}

	public static
	void htmlTableOpenLayout (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<table class=\"layout\">");

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableOpenLayout () {

		htmlTableOpenLayout (
			currentFormatWriter ());

	}

	public static
	void htmlTableOpenList (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<table class=\"list\">");

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableOpenList () {

		htmlTableOpenList (
			currentFormatWriter ());

	}

	public static
	void htmlCloseTable (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</table>");

	}

	public static
	void htmlTableClose () {

		htmlCloseTable (
			currentFormatWriter ());

	}

	public static
	void htmlTableRowOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<tr");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableRowOpen (
			@NonNull HtmlAttribute ... attributes) {

		htmlTableRowOpen (
			currentFormatWriter (),
			attributes);

	}

	public static
	void htmlTableRowClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</tr>");

	}

	public static
	void htmlTableRowClose () {

		htmlTableRowClose (
			currentFormatWriter ());

	}

	public static
	void htmlTableCellOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableCellOpen (
			@NonNull HtmlAttribute ... attributes) {

		htmlTableCellOpen (
			currentFormatWriter (),
			attributes);

	}

	public static
	void htmlTableCellClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</td>");

	}

	public static
	void htmlTableCellClose () {

		htmlTableCellClose (
			currentFormatWriter ());

	}

	public static
	void htmlTableCellWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%h</td>\n",
			content);

	}

	public static
	void htmlTableCellWrite (
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlTableCellWrite (
			currentFormatWriter (),
			content,
			attributes);

	}

	public static
	void htmlTableCellWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull Runnable content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		content.run ();

		formatWriter.writeFormat (
			"</td>");

		formatWriter.writeNewline ();

	}

	public static
	void htmlTableCellWrite (
			@NonNull Runnable content,
			@NonNull HtmlAttribute ... attributes) {

		htmlTableCellWrite (
			currentFormatWriter (),
			content,
			attributes);

	}

	public static
	void htmlTableCellWriteFormat (
			@NonNull FormatWriter formatWriter,
			@NonNull Object ... arguments) {

		formatWriter.writeLineFormat (
			"<td>%s</td>",
			stringFormatArray (
				arguments));

	}

	public static
	void htmlTableCellWriteFormat (
			@NonNull Object ... arguments) {

		htmlTableCellWriteFormat (
			currentFormatWriter (),
			arguments);

	}

	public static
	void htmlTableCellWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%s</td>",
			content);

		formatWriter.writeNewline (); 

	}

	public static
	void htmlTableCellWriteHtml (
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlTableCellWriteHtml (
			currentFormatWriter (),
			content,
			attributes);

	}

	public static
	void htmlTableCellWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull Runnable content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		content.run ();

		formatWriter.writeFormat (
			"</td>",
			content);

		formatWriter.writeNewline (); 

	}

	public static
	void htmlTableCellWriteHtml (
			@NonNull Runnable content,
			@NonNull HtmlAttribute ... attributes) {

		htmlTableCellWriteHtml (
			currentFormatWriter (),
			content,
			attributes);

	}

	public static
	void htmlTableHeaderCellWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<th");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%h</th>",
			content);

		formatWriter.writeNewline ();

	}

	public static
	void htmlTableHeaderCellWrite (
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlTableHeaderCellWrite (
			currentFormatWriter (),
			content,
			attributes);

	}

	public static
	void htmlTableDetailsRowWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull String value,
			@NonNull HtmlAttribute ... attributes) {

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			label);

		htmlTableCellWrite (
			formatWriter,
			value,
			attributes);

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableDetailsRowWrite (
			@NonNull String label,
			@NonNull String value,
			@NonNull HtmlAttribute ... attributes) {

		htmlTableDetailsRowWrite (
			currentFormatWriter (),
			label,
			value,
			attributes);

	}

	public static
	void htmlTableDetailsRowWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull String value) {

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			label);

		htmlTableCellWriteHtml (
			formatWriter,
			value);

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableDetailsRowWriteHtml (
			@NonNull String label,
			@NonNull String value) {

		htmlTableDetailsRowWriteHtml (
			currentFormatWriter (),
			label,
			value);

	}

	public static
	void htmlTableDetailsRowWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull Runnable value) {

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			label);

		htmlTableCellWriteHtml (
			formatWriter,
			value);

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableDetailsRowWriteHtml (
			@NonNull String label,
			@NonNull Runnable value) {

		htmlTableDetailsRowWriteHtml (
			currentFormatWriter (),
			label,
			value);

	}

	public static
	void htmlTableDetailsRowWriteRaw (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull String rawValue) {

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			label);

		formatWriter.writeString (
			rawValue);

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableDetailsRowWriteRaw (
			@NonNull String label,
			@NonNull String rawValue) {

		htmlTableDetailsRowWrite (
			currentFormatWriter (),
			label,
			rawValue);

	}

	public static
	void htmlTableDetailsRowWriteRaw (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull Consumer <FormatWriter> valueCellWriter) {

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			label);

		valueCellWriter.accept (
			formatWriter);

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableDetailsRowWriteRaw (
			@NonNull String label,
			@NonNull Consumer <FormatWriter> valueCellWriter) {

		htmlTableDetailsRowWriteRaw (
			currentFormatWriter (),
			label,
			valueCellWriter);

	}

	public static
	void htmlTableDetailsRowWriteRaw (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull Runnable valueCellWriter) {

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			label);

		valueCellWriter.run ();

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableDetailsRowWriteRaw (
			@NonNull String label,
			@NonNull Runnable valueCellWriter) {

		htmlTableDetailsRowWriteRaw (
			currentFormatWriter (),
			label,
			valueCellWriter);

	}

	public static
	void htmlTableRowSeparatorWrite (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<tr class=\"sep\">");

	}

	public static
	void htmlTableRowSeparatorWrite () {

		htmlTableRowSeparatorWrite (
			currentFormatWriter ());

	}

}
