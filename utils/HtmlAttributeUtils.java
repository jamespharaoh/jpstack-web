package wbs.web.utils;

import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.IterableUtils.iterableStream;
import static wbs.utils.etc.NumberUtils.equalToOne;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.notMoreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.joinWithSemicolonAndSpace;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.Arrays;
import java.util.function.Function;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.utils.etc.OptionalUtils;
import wbs.utils.string.FormatWriter;

import wbs.web.utils.HtmlStyleUtils.HtmlStyleRuleEntry;

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
	void htmlAttributesWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull Iterable <? extends ToHtmlAttribute> attributes) {

		iterableStream (
			attributes)

			.map (
				ToHtmlAttribute::htmlAttribute)

			.filter (
				OptionalUtils::optionalIsPresent)

			.map (
				OptionalUtils::optionalGetRequired)

			.forEach (
				attribute ->
					htmlAttributeWrite (
						formatWriter,
						attribute));

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
	HtmlAttribute htmlAttribute (
			@NonNull String name,
			@NonNull String value) {

		return HtmlAttribute.of (
			name,
			value);

	}

	public static
	HtmlAttribute htmlAttributeFormat (
			@NonNull String name,
			@NonNull String ... arguments) {

		return HtmlAttribute.of (
			name,
			stringFormatArray (
				arguments));

	}

	// specific constructors

	public static
	HtmlAttribute htmlClassAttribute (
			@NonNull Iterable <String> classNames) {

		return HtmlAttribute.of (
			"class",
			joinWithSpace (
				classNames));

	}

	public static
	HtmlAttribute htmlClassAttribute (
			@NonNull String ... classNames) {

		return HtmlAttribute.of (
			"class",
			joinWithSpace (
				classNames));

	}

	public static
	ToHtmlAttribute htmlColumnSpanAttribute (
			@NonNull Long value) {

		if (
			notMoreThanZero (
				value)
		) {

			throw new IllegalArgumentException ();

		} else if (
			equalToOne (
				value)
		) {

			return htmlAttributeAbsent ();

		} else {

			return HtmlAttribute.of (
				"colspan",
				integerToDecimalString (
					value));

		}

	}

	public static
	HtmlAttribute htmlDataAttribute (
			@NonNull String name,
			@NonNull String value) {

		return HtmlAttribute.of (
			stringFormat (
				"data-%s",
				name),
			value);

	}

	public static
	HtmlAttribute htmlDataAttributeFormat (
			@NonNull String name,
			@NonNull String ... value) {

		return HtmlAttribute.of (
			stringFormat (
				"data-%s",
				name),
			stringFormatArray (
				value));

	}

	public static
	HtmlAttribute htmlIdAttribute (
			@NonNull String value) {

		return HtmlAttribute.of (
			"id",
			value);

	}

	public static
	HtmlAttribute htmlIdAttributeFormat (
			@NonNull String ... arguments) {

		return HtmlAttribute.of (
			"id",
			stringFormatArray (
				arguments));

	}

	public static
	HtmlAttribute htmlNameAttribute (
			@NonNull String value) {

		return HtmlAttribute.of (
			"name",
			value);

	}

	public static
	HtmlAttribute htmlNameAttributeFormat (
			@NonNull String ... arguments) {

		return HtmlAttribute.of (
			"name",
			stringFormatArray (
				arguments));

	}

	public static
	ToHtmlAttribute htmlRowSpanAttribute (
			@NonNull Long value) {

		if (
			notMoreThanZero (
				value)
		) {

			throw new IllegalArgumentException ();

		} else if (
			equalToOne (
				value)
		) {

			return htmlAttributeAbsent ();

		} else {

			return HtmlAttribute.of (
				"rowspan",
				integerToDecimalString (
					value));

		}

	}

	public static
	HtmlAttribute htmlStyleAttribute (
			@NonNull Iterable <HtmlStyleRuleEntry> styles) {

		return HtmlAttribute.of (
			"style",
			joinWithSemicolonAndSpace (
				iterableMap (
					styles,
					entry ->
						stringFormat (
							"%s: %s",
							entry.name (),
							joinWithSpace (
								entry.values ())))))

		;

	}

	public static
	HtmlAttribute htmlStyleAttribute (
			@NonNull HtmlStyleRuleEntry ... styles) {

		return htmlStyleAttribute (
			Arrays.asList (
				styles));

	}

	public static
	HtmlAttribute htmlTargetAttribute (
			@NonNull String target) {

		return HtmlAttribute.of (
			"target",
			target);

	}

	public static
	Function <String, HtmlAttribute> htmlTargetAttribute () {

		return HtmlAttributeUtils::htmlTargetAttribute;

	}

	public static
	HtmlAttribute htmlTypeAttribute (
			@NonNull String type) {

		return htmlAttribute (
			"type",
			type);

	}

	public static
	HtmlAttribute htmlValueAttribute (
			@NonNull String value) {

		return HtmlAttribute.of (
			"value",
			value);

	}

	public static
	ToHtmlAttribute htmlAttributeAbsent () {

		return htmlAttributeAbsentInstance;

	}

	public
	interface ToHtmlAttribute {
		Optional <HtmlAttribute> htmlAttribute ();
	}

	private final static
	ToHtmlAttribute htmlAttributeAbsentInstance =
		new ToHtmlAttribute () {

		@Override
		public
		Optional <HtmlAttribute> htmlAttribute () {

			return optionalAbsent ();

		}

	};

	@Accessors (fluent = true)
	@Data
	public static
	class HtmlAttribute
		implements ToHtmlAttribute {

		private final
		String name;

		private final
		String value;

		public
		HtmlAttribute (
				@NonNull String name,
				@NonNull String value) {

			this.name =
				name;

			this.value =
				value;

		}

		public static
		HtmlAttribute of (
				@NonNull String name,
				@NonNull String value) {

			return new HtmlAttribute (
				name,
				value);

		}

		@Override
		public
		Optional <HtmlAttribute> htmlAttribute () {

			return optionalOf (
				this);

		}

	}

}
