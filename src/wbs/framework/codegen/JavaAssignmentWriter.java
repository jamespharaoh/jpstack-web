package wbs.framework.codegen;

import static wbs.utils.collection.MapUtils.mapIsEmpty;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
public
class JavaAssignmentWriter {

	// properties

	@Getter @Setter
	String variableName;

	@Getter @Setter
	String value;

	@Getter @Setter
	Map <String, String> properties =
		new LinkedHashMap<> ();

	@Getter @Setter
	Map <String, List <String>> calls =
		new LinkedHashMap<> ();

	// utility methods

	public
	JavaAssignmentWriter valueFormat (
			@NonNull String ... arguments) {

		return value (
			stringFormatArray (
				arguments));

	}

	public
	JavaAssignmentWriter property (
			@NonNull String name,
			@NonNull String value) {

		properties.put (
			name,
			value);

		return this;

	}

	public
	JavaAssignmentWriter propertyFormat (
			@NonNull String name,
			@NonNull String ... arguments) {

		return property (
			name,
			stringFormatArray (
				arguments));

	}

	public
	JavaAssignmentWriter call (
			@NonNull String name,
			@NonNull List <String> arguments) {

		calls.put (
			name,
			ImmutableList.copyOf (
				arguments));

		return this;

	}

	public
	JavaAssignmentWriter call (
			@NonNull String name,
			@NonNull String ... arguments) {

		return call (
			name,
			Arrays.asList (
				arguments));

	}

	// implementation

	public
	void write (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"%s =",
			variableName);

		if (

			mapIsEmpty (
				properties)

			&& mapIsEmpty (
				calls)

		) {

			formatWriter.writeLineFormat (
				"\t%s;",
				value);

		} else {

			formatWriter.writeLineFormat (
				"\t%s",
				value);

			formatWriter.writeNewline ();

			for (
				Map.Entry <String, String> propertyEntry
					: properties.entrySet ()
			) {

				formatWriter.writeLineFormat (
					"\t.%s (",
					propertyEntry.getKey ());

				formatWriter.writeLineFormat (
					"\t\t%s)",
					propertyEntry.getValue ());

				formatWriter.writeNewline ();

			}

			for (
				Map.Entry <String, List <String>> callEntry
					: calls.entrySet ()
			) {

				formatWriter.writeLineFormat (
					"\t.%s ()",
					callEntry.getKey ());

				// TODO arguments!

				formatWriter.writeNewline ();

			}

			formatWriter.writeLineFormat (
				";");

		}

		formatWriter.writeNewline ();

	}

}
