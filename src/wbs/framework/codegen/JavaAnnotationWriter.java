package wbs.framework.codegen;

import static wbs.utils.collection.ArrayUtils.arrayIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionHasOneElement;
import static wbs.utils.collection.CollectionUtils.iterableFirstElementRequired;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("javaAnnotationWriter")
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
