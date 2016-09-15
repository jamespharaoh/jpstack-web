package wbs.utils.web;

import static wbs.utils.collection.CollectionUtils.listSliceAllButLastItemRequired;
import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;

import java.util.Arrays;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;

public
class HtmlStyleUtils {

	public static
	void htmlStyleBlockOpen (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<style type=\"text/css\">");

		formatWriter.increaseIndent ();

	}

	public static
	void htmlStyleBlockOpen () {

		htmlStyleBlockOpen (
			currentFormatWriter ());

	}

	public static
	void htmlStyleBlockClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</style>");

	}

	public static
	void htmlStyleBlockClose () {

		htmlStyleBlockClose (
			currentFormatWriter ());

	}

	public static
	void htmlStyleRuleOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull String ... selectors) {

		for (
			String selector
				: listSliceAllButLastItemRequired (
					Arrays.asList (
						selectors))
		) {

			formatWriter.writeLineFormat (
				"%s,",
				selector);

		}

		formatWriter.writeLineFormat (
			"%s,",
			listSliceAllButLastItemRequired (
				Arrays.asList (
					selectors)));

		formatWriter.increaseIndent ();

	}

	public static
	void htmlStyleRuleOpen (
			@NonNull String ... selectors) {

		htmlStyleRuleOpen (
			currentFormatWriter (),
			selectors);

	}

	public static
	void htmlStyleRuleClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

	}

	public static
	void htmlStyleRuleClose () {

		htmlStyleRuleClose (
			currentFormatWriter ());

	}

	public static
	void htmlStyleRuleEntryWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull String value) {

		formatWriter.writeLineFormat (
			"%h: %h;",
			name,
			value);

	}

	public static
	void htmlStyleRuleEntryWrite (
			@NonNull String name,
			@NonNull String value) {

		htmlStyleRuleEntryWrite (
			currentFormatWriter (),
			name,
			value);

	}

}
