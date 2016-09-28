package wbs.utils.web;

import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;
import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttributesWrite;

import java.util.Arrays;
import java.util.function.Consumer;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;
import wbs.utils.web.HtmlAttributeUtils.HtmlAttribute;
import wbs.utils.web.HtmlAttributeUtils.ToHtmlAttribute;

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

				// first column, just store it

				if (headerLabel == null) {
					throw new RuntimeException ();
				}

				currentLabel =
					headerLabel;

				currentColumnSpan = 1;

			} else if (headerLabel == null) { 

				// null value continue previous column

				currentColumnSpan ++;

			} else {

				// new header, output pending

				if (currentColumnSpan > 1) {

					formatWriter.writeLineFormat (
						"<th colspan=\"%h\">%h</th>",
						currentColumnSpan,
						currentLabel);

				} else {
	
					formatWriter.writeLineFormat (
						"<th>%h</th>",
						currentLabel);

				}

				// update current

				currentLabel =
					headerLabel;

				currentColumnSpan = 1;

			}

		}

		// output final header cell

		if (currentColumnSpan > 1) {

			formatWriter.writeLineFormat (
				"<th colspan=\"%h\">%h</th>",
				currentColumnSpan,
				currentLabel);

		} else {

			formatWriter.writeLineFormat (
				"<th>%h</th>",
				currentLabel);

		}

		// close table

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
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<table");

		htmlAttributesWrite (
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableOpen (
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableOpen (
			currentFormatWriter (),
			attributes);

	}

	public static
	void htmlTableOpenDetails (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<table class=\"details\">");

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableOpenDetails () {

		htmlTableOpenDetails (
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
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<table",
			" class=\"list\"");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableOpenList (
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableOpenList (
			currentFormatWriter (),
			attributes);

	}

	public static
	void htmlTableClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</table>");

	}

	public static
	void htmlTableClose () {

		htmlTableClose (
			currentFormatWriter ());

	}

	// table head

	public static
	void htmlTableHeadOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<thead");

		htmlAttributesWrite (
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableHeadOpen (
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableHeadOpen (
			currentFormatWriter (),
			attributes);

	}

	public static
	void htmlTableHeadClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</thead>");

	}

	public static
	void htmlTableHeadClose () {

		htmlTableHeadClose (
			currentFormatWriter ());

	}

	// table body

	public static
	void htmlTableBodyOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<tbody");

		htmlAttributesWrite (
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableBodyOpen (
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableBodyOpen (
			currentFormatWriter (),
			attributes);

	}

	public static
	void htmlTableBodyClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</tbody>");

	}

	public static
	void htmlTableBodyClose () {

		htmlTableBodyClose (
			currentFormatWriter ());

	}

	// table row

	public static
	void htmlTableRowOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull Iterable <ToHtmlAttribute> attributes) {

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
			@NonNull Iterable <ToHtmlAttribute> attributes) {

		htmlTableRowOpen (
			currentFormatWriter (),
			attributes);

	}

	public static
	void htmlTableRowOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableRowOpen (
			formatWriter,
			Arrays.asList (
				attributes));

	}

	public static
	void htmlTableRowOpen (
			@NonNull ToHtmlAttribute ... attributes) {

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
			@NonNull ToHtmlAttribute ... attributes) {

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
			@NonNull ToHtmlAttribute ... attributes) {

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
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%h</td>",
			content);

		formatWriter.writeNewline ();

	}

	public static
	void htmlTableCellWrite (
			@NonNull String content,
			@NonNull ToHtmlAttribute ... attributes) {

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
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableCellOpen (
			formatWriter,
			attributes);

		content.run ();

		htmlTableCellClose ();

	}

	public static
	void htmlTableCellWriteHtml (
			@NonNull Runnable content,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableCellWriteHtml (
			currentFormatWriter (),
			content,
			attributes);

	}

	public static
	void htmlTableHeaderCellWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String content,
			@NonNull ToHtmlAttribute ... attributes) {

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
			@NonNull ToHtmlAttribute ... attributes) {

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
			@NonNull ToHtmlAttribute ... attributes) {

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
			@NonNull ToHtmlAttribute ... attributes) {

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
			@NonNull String value,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableRowOpen (
			formatWriter,
			attributes);

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
			@NonNull String value,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableDetailsRowWriteHtml (
			currentFormatWriter (),
			label,
			value,
			attributes);

	}

	public static
	void htmlTableDetailsRowWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull Runnable value,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableRowOpen (
			formatWriter,
			attributes);

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
			@NonNull Runnable value,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableDetailsRowWriteHtml (
			currentFormatWriter (),
			label,
			value,
			attributes);

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
