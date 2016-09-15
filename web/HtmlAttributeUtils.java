package wbs.utils.web;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.notMoreThanZero;
import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.utils.string.FormatWriter;

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
			@NonNull HtmlAttribute ... attributes) {

		for (
			HtmlAttribute attribute
				: attributes
		) {

			htmlAttributeWrite (
				formatWriter,
				attribute);

		}

	}

	public static
	void htmlAttributesWrite (
			@NonNull HtmlAttribute ... attributes) {

		htmlAttributesWrite (
			currentFormatWriter (),
			attributes);

	}

	public static
	HtmlAttribute htmlAttribute (
			@NonNull String name,
			@NonNull String value) {

		return new HtmlAttribute ()
			.name (name)
			.value (value);

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
			@NonNull String ... classNames) {

		return new HtmlAttribute ()

			.name (
				"class")

			.value (
				joinWithSpace (
					classNames));

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

	@Accessors (fluent = true)
	@Data
	public static
	class HtmlAttribute {
		String name;
		String value;
	}

}
