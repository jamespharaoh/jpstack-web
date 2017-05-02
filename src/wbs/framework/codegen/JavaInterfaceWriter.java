package wbs.framework.codegen;

import static wbs.utils.collection.CollectionUtils.collectionHasOneElement;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listLastItemRequired;
import static wbs.utils.collection.CollectionUtils.listSliceAllButLastItemRequired;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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

@Accessors (fluent = true)
@PrototypeComponent ("javaInterfaceWriter")
public
class JavaInterfaceWriter
	implements JavaBlockWriter {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String interfaceName;

	@Getter @Setter
	List <JavaAnnotationWriter> interfaceAnnotations =
		new ArrayList<> ();

	@Getter @Setter
	List <String> interfaceModifiers =
		new ArrayList<> ();

	@Getter @Setter
	List <Function <JavaImportRegistry, String>> extendsInterfaceNames =
		new ArrayList<> ();

	@Getter @Setter
	List <JavaBlockWriter> blocks =
		new ArrayList<> ();

	// setters and getters

	public
	JavaInterfaceWriter interfaceNameFormat (
			@NonNull String ... arguments) {

		return interfaceName (
			stringFormatArray (
				arguments));

	}

	public
	JavaInterfaceWriter addInterfaceModifier (
			@NonNull String modifier) {

		interfaceModifiers.add (
			modifier);

		return this;

	}

	public
	JavaInterfaceWriter addInterface (
			@NonNull Function <JavaImportRegistry, String> interfaceName) {

		extendsInterfaceNames.add (
			interfaceName);

		return this;

	}

	public
	JavaInterfaceWriter addInterfaceFormat (
			@NonNull String ... arguments) {

		extendsInterfaceNames.add (
			imports ->
				imports.register (
					stringFormatArray (
						arguments)));

		return this;

	}

	public
	JavaInterfaceWriter addBlock (
			@NonNull JavaBlockWriter block) {

		blocks.add (
			block);

		return this;

	}

	// implementation

	@Override
	public
	void writeBlock (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"writeBlock");

		) {

			// interface annotations

			for (
				JavaAnnotationWriter annotationWriter
					: interfaceAnnotations
			) {

				annotationWriter.writeAnnotation (
					imports,
					formatWriter);

			}

			// interface modifiers

			if (
				collectionIsNotEmpty (
					interfaceModifiers)
			) {

				formatWriter.writeLineFormat (
					"%s",
					joinWithSpace (
						interfaceModifiers));

			}

			// interface declaration

			if (
				collectionIsEmpty (
					extendsInterfaceNames)
			) {

				formatWriter.writeLineFormat (
					"interface %s {",
					interfaceName);

			} else {

				formatWriter.writeLineFormat (
					"interface %s",
					interfaceName);

			}

			// extends declaration

			if (
				collectionIsNotEmpty (
					extendsInterfaceNames)
			) {

				if (
					collectionHasOneElement (
						extendsInterfaceNames)
				) {

					Function <JavaImportRegistry, String> extendsInterfaceName =
						listFirstElementRequired (
							extendsInterfaceNames);

					formatWriter.writeLineFormat (
						"\textends %s {",
						extendsInterfaceName.apply (
							imports));

				} else {

					formatWriter.writeLineFormat (
						"\textends");

					for (
						Function <JavaImportRegistry, String> extendsInterfaceName
							: listSliceAllButLastItemRequired (
								extendsInterfaceNames)
					) {

						formatWriter.writeLineFormat (
							"\t\t%s,",
							extendsInterfaceName.apply (
								imports));

					}

					Function <JavaImportRegistry, String> extendsInterfaceName =
						listLastItemRequired (
							extendsInterfaceNames);

					formatWriter.writeLineFormat (
						"\t\t%s {",
						extendsInterfaceName.apply (
							imports));

				}

			}

			formatWriter.writeNewline ();

			// interface body

			formatWriter.increaseIndent ();

			blocks.forEach (
				block ->
					block.writeBlock (
						taskLogger,
						imports,
						formatWriter));

			formatWriter.decreaseIndent ();

			// end class

			formatWriter.writeLineFormat (
				"}");

		}

	}

}
