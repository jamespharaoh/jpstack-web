package wbs.framework.entity.meta;

import static wbs.framework.utils.etc.Misc.stringFormatArray;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
public
class AnnotationWriter {

	@Getter @Setter
	String name;

	@Getter @Setter
	Map<String,String> attributes =
		new LinkedHashMap<String,String> ();

	public
	AnnotationWriter addAttributeFormat (
			String name,
			String... valueFormat) {

		attributes.put (
			name,
			stringFormatArray (
				valueFormat));

		return this;

	}

	public
	void write (
			FormatWriter javaWriter,
			String indent) {

		// write name

		javaWriter.writeFormat (
			"%s@%s",
			indent,
			name);

		// write attributes

		if (! attributes.isEmpty ()) {

			javaWriter.writeFormat (
				" (");

			boolean first = true;

			for (
				Map.Entry<String,String> attributeEntry
					: attributes.entrySet ()
			) {

				if (! first) {

					javaWriter.writeFormat (
						",");

				}

				javaWriter.writeFormat (
					"\n%s\t%s = %s",
					indent,
					attributeEntry.getKey (),
					attributeEntry.getValue ());

				first = false;

			}

			javaWriter.writeFormat (
				")");

		}

		javaWriter.writeFormat (
			"\n");

	}

}
