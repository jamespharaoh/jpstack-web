package wbs.framework.codegen;

import static wbs.framework.utils.etc.ArrayUtils.arrayIsEmpty;
import static wbs.framework.utils.etc.CollectionUtils.collectionHasOneElement;
import static wbs.framework.utils.etc.CollectionUtils.iterableFirstElementRequired;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormatArray;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.utils.formatwriter.FormatWriter;

@Accessors (fluent = true)
public
class JavaAnnotationWriter {

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	Map <String, String> attributes =
		new LinkedHashMap<> ();

	// property accessors

	public
	JavaAnnotationWriter addAttributeFormat (
			@NonNull String name,
			@NonNull String ... valueFormat) {

		if (
			arrayIsEmpty (
				valueFormat)
		) {
			throw new IllegalArgumentException ();
		}

		attributes.put (
			name,
			stringFormatArray (
				valueFormat));

		return this;

	}

	// implementation

	public
	void writeAnnotation (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		// write name

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"@%s",
			imports.register (
				name));

		// write attributes

		if (! attributes.isEmpty ()) {

			formatWriter.writeFormat (
				" (");

			if (

				collectionHasOneElement (
					attributes.entrySet ())

				&& stringEqualSafe (
					iterableFirstElementRequired (
						attributes.keySet ()),
					"value")

			) {

				formatWriter.writeFormat (
					"%s",
					iterableFirstElementRequired (
						attributes.values ()));

			} else {

				boolean first = true;

				for (
					Map.Entry <String, String> attributeEntry
						: attributes.entrySet ()
				) {

					if (! first) {

						formatWriter.writeFormat (
							", ");

					}

					formatWriter.writeNewline ();

					formatWriter.writeIndent ();

					formatWriter.writeFormat (
						"\t%s = %s",
						attributeEntry.getKey (),
						attributeEntry.getValue ());

					first = false;

				}

			}

			formatWriter.writeFormat (
				")");

		}

		formatWriter.writeNewline ();

	}

}
