package wbs.framework.codegen;

import static wbs.utils.collection.CollectionUtils.collectionHasOneItem;
import static wbs.utils.collection.CollectionUtils.listLastItemRequired;
import static wbs.utils.collection.MapUtils.mapItemForKeyOrThrow;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringSplitFullStop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;
import wbs.utils.string.NullFormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("javaClassUnitWriter")
public
class JavaClassUnitWriter {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	FormatWriter formatWriter;

	@Getter @Setter
	String packageName;

	@Getter @Setter
	List <JavaBlockWriter> blocks =
		new ArrayList<> ();

	// state

	Map <String, String> importedClassMappings =
		new HashMap<> ();

	Set <String> nonImportedClassNames =
		new HashSet<> ();

	// setters and getters

	public
	JavaClassUnitWriter packageNameFormat (
			@NonNull String ... arguments) {

		return packageName (
			stringFormatArray (
				arguments));

	}

	public
	JavaClassUnitWriter addBlock (
			@NonNull JavaBlockWriter block) {

		blocks.add (
			block);

		return this;

	}

	// implementation

	public
	void write (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"write");

		) {

			formatWriter.writeLineFormat (
				"package %s;",
				packageName);

			formatWriter.writeNewline ();

			blocks.forEach (
				block ->
					block.writeBlock (
						taskLogger,
						new ImportCollector (),
						new NullFormatWriter ()));

			for (
				String importClassName
					: importedClassMappings.values ()
			) {

				formatWriter.writeLineFormat (
					"import %s;",
					importClassName);

			}

			formatWriter.writeNewline ();

			blocks.forEach (
				block ->
					block.writeBlock (
						taskLogger,
						new ImportResolver (),
						formatWriter));

		}

	}

	class ImportCollector
		implements JavaImportRegistry {

		@Override
		public
		String register (
				@NonNull String fullClassName) {

			if (

				contains (
					primitiveTypeNames,
					fullClassName)

				|| contains (
					primitiveArrayTypeNames,
					fullClassName)

			) {
				return fullClassName;
			}

			List <String> fullClassNameParts =
				stringSplitFullStop (
					fullClassName);

			if (
				collectionHasOneItem (
					fullClassNameParts)
			) {

				throw new IllegalArgumentException (
					stringFormat (
						"Not a fully qualifed class name: %s",
						fullClassName));

			}

			String simpleClassName =
				listLastItemRequired (
					fullClassNameParts);

			if (

				contains (
					importedClassMappings.keySet (),
					simpleClassName)

				&& stringEqualSafe (
					importedClassMappings.get (
						simpleClassName),
					fullClassName)

			) {

				// already mapped

			} else if (
				contains (
					nonImportedClassNames,
					fullClassName)
			) {

				// already duplicated

			} else if (
				doesNotContain (
					importedClassMappings.keySet (),
					simpleClassName)
			) {

				// new mapping

				importedClassMappings.put (
					simpleClassName,
					fullClassName);

			} else {

				// new duplicate

				importedClassMappings.remove (
					simpleClassName);

				nonImportedClassNames.add (
					fullClassName);

			}

			return "";

		}

	}

	class ImportResolver
		implements JavaImportRegistry {

		@Override
		public
		String register (
				@NonNull String fullClassName) {

			if (

				contains (
					primitiveTypeNames,
					fullClassName)

				|| contains (
					primitiveArrayTypeNames,
					fullClassName)

				|| contains (
					nonImportedClassNames,
					fullClassName)

			) {

				return fullClassName;

			} else if (
				! fullClassName.contains (
					".")
			) {

				throw new RuntimeException (
					stringFormat (
						"Not a fully qualified class name: %s",
						fullClassName));

			} else if (
				contains (
					nonImportedClassNames,
					fullClassName)
			) {

				return fullClassName;

			} else {

				String simpleClassName =
					listLastItemRequired (
						stringSplitFullStop (
							fullClassName));

				String simpleMemberName =
					mapItemForKeyOrThrow (
						importedClassMappings,
						simpleClassName,
						() -> new NoSuchElementException (
							stringFormat (
								"No such import: %s",
								simpleClassName)));

				if (
					stringNotEqualSafe (
						simpleMemberName,
						fullClassName)
				) {
					shouldNeverHappen ();
				}

				return simpleClassName;

			}

		}

	}

	public final static
	ImmutableSet <String> primitiveTypeNames =
		ImmutableSet.of (
			"boolean",
			"byte",
			"double",
			"float",
			"int",
			"long",
			"void");

	public final static
	ImmutableSet <String> primitiveArrayTypeNames =
		ImmutableSet.of (
			"boolean[]",
			"byte[]",
			"double[]",
			"float[]",
			"int[]",
			"long[]",
			"void[]");

}
