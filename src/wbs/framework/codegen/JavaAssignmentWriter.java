package wbs.framework.codegen;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.listLastItemRequired;
import static wbs.utils.collection.CollectionUtils.listSliceAllButLastItemRequired;
import static wbs.utils.collection.MapUtils.mapIsEmpty;
import static wbs.utils.collection.MapUtils.mapIsNotEmpty;
import static wbs.utils.etc.Misc.todo;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
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

import wbs.framework.component.annotations.PrototypeComponent;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("javaAssignmentWriter")
public
class JavaAssignmentWriter {

	// properties

	@Getter @Setter
	String variableName;

	@Getter @Setter
	String provider;

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

		} else if (

			isNotNull (
				value)

			&& isNull (
				provider)

		) {

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

				String functionName =
					callEntry.getKey ();

				List <String> arguments =
					callEntry.getValue ();

				if (
					collectionIsEmpty (
						arguments)
				) {

					formatWriter.writeLineFormat (
						"\t.%s ()",
						functionName);

				} else {

					formatWriter.writeLineFormat (
						"\t.%s (",
						functionName);

					for (
						String argument
							: listSliceAllButLastItemRequired (
								arguments)
					) {

						formatWriter.writeLineFormat (
							"\t\t%s,",
							argument);

					}

					formatWriter.writeLineFormat (
						"\t\t%s);",
						listLastItemRequired (
							arguments));

				}

				// TODO arguments!

				formatWriter.writeNewline ();

			}

			formatWriter.writeLineFormat (
				";");

		} else if (

			isNull (
				value)

			&& isNotNull (
				provider)

		) {

			formatWriter.writeLineFormat (
				"\t%s.provide (",
				provider);

			formatWriter.writeLineFormat (
				"\t\ttaskLogger,");

			formatWriter.writeLineFormat (
				"\t\t%s ->",
				variableName);

			formatWriter.writeLineFormat (
				"\t\t\t%s",
				variableName);

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

			if (
				mapIsNotEmpty (
					calls)
			) {
				throw todo ();
			}

			formatWriter.writeLineFormat (
				");");

		}

		formatWriter.writeNewline ();

	}

}
