package wbs.utils.web;

import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.notMoreThanZero;
import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;
import static wbs.utils.string.StringUtils.joinWithSemicolonAndSpace;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.Arrays;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.utils.string.FormatWriter;
import wbs.utils.web.HtmlStyleUtils.HtmlStyleRuleEntry;

public
class HtmlAttributeUtils {

	// generic

	public static
	void htmlAttributeWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlAttribute attribute) {

		formatWriter.writeFormat (
			" %h=\"%h\"",
			attribute.name (),
			attribute.value ());

	}

	public static
	void htmlAttributeWrite (
			@NonNull HtmlAttribute attribute) {

		htmlAttributeWrite (
			currentFormatWriter (),
			attribute);

	}

	public static
	void htmlAttributesWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull Iterable <ToHtmlAttribute> attributes) {

		for (
			ToHtmlAttribute attribute
				: attributes
		) {

			htmlAttributeWrite (
				formatWriter,
				attribute.htmlAttribute ());

		}

	}

	public static
	void htmlAttributesWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlAttributesWrite (
			formatWriter,
			Arrays.asList (
				attributes));

	}

	public static
	void htmlAttributesWrite (
			@NonNull ToHtmlAttribute ... attributes) {

		htmlAttributesWrite (
			currentFormatWriter (),
			Arrays.asList (
				attributes));

	}

	public static
	HtmlAttribute htmlAttribute (
			@NonNull String name,
			@NonNull String value) {

		return new HtmlAttribute ()
			.name (name)
			.value (value);

	}

	public static
	HtmlAttribute htmlAttributeFormat (
			@NonNull String name,
			@NonNull Object ... arguments) {

		return new HtmlAttribute ()

			.name (
				name)

			.value (
				stringFormatArray (
					arguments));

	}

	// specific constructors

	public static
	HtmlAttribute htmlIdAttribute (
			@NonNull String value) {

		return new HtmlAttribute ()

			.name (
				"id")

			.value (
				value);

	}

	public static
	HtmlAttribute htmlIdAttributeFormat (
			@NonNull Object ... arguments) {

		return new HtmlAttribute ()

			.name (
				"id")

			.value (
				stringFormatArray (
					arguments));

	}

	public static
	HtmlAttribute htmlNameAttribute (
			@NonNull String value) {

		return new HtmlAttribute ()

			.name (
				"name")

			.value (
				value);

	}

	public static
	HtmlAttribute htmlColumnSpanAttribute (
			@NonNull Long value) {

		if (
			notMoreThanZero (
				value)
		) {
			throw new IllegalArgumentException ();
		}

		return new HtmlAttribute ()

			.name (
				"colspan")

			.value (
				integerToDecimalString (
					value));

	}

	public static
	HtmlAttribute htmlRowSpanAttribute (
			@NonNull Long value) {

		if (
			notMoreThanZero (
				value)
		) {
			throw new IllegalArgumentException ();
		}

		return new HtmlAttribute ()

			.name (
				"rowspan")

			.value (
				integerToDecimalString (
					value));

	}

	public static
	HtmlAttribute htmlClassAttribute (
			@NonNull Iterable <String> classNames) {

		return new HtmlAttribute ()

			.name (
				"class")

			.value (
				joinWithSpace (
					classNames));

	}

	public static
	HtmlAttribute htmlClassAttribute (
			@NonNull String ... classNames) {

		return new HtmlAttribute ()

			.name (
				"class")

			.value (
				joinWithSpace (
					classNames));

	}

	public static
	HtmlAttribute htmlStyleAttribute (
			@NonNull Iterable <HtmlStyleRuleEntry> styles) {

		return new HtmlAttribute ()

			.name (
				"style")

			.value (
				joinWithSemicolonAndSpace (
					iterableMap (
						entry ->
							stringFormat (
								"%s: %s",
								entry.name (),
								joinWithSpace (
									entry.values ())),
						styles)));

	}

	public static
	HtmlAttribute htmlStyleAttribute (
			@NonNull HtmlStyleRuleEntry ... styles) {

		return htmlStyleAttribute (
			Arrays.asList (
				styles));

	}

	public static
	HtmlAttribute htmlDataAttribute (
			@NonNull String name,
			@NonNull String value) {

		return new HtmlAttribute ()

			.name (
				stringFormat (
					"data-%s",
					name))

			.value (
				value);

	}

	public static
	HtmlAttribute htmlDataAttributeFormat (
			@NonNull String name,
			@NonNull Object ... value) {

		return new HtmlAttribute ()

			.name (
				stringFormat (
					"data-%s",
					name))

			.value (
				stringFormatArray (
					value));

	}

	@Accessors (fluent = true)
	@Data
	public static
	class HtmlAttribute
		implements ToHtmlAttribute {

		String name;
		String value;

		@Override
		public
		HtmlAttribute htmlAttribute () {
			return this;
		}

	}

	public
	interface ToHtmlAttribute {
		HtmlAttribute htmlAttribute ();
	}

}
