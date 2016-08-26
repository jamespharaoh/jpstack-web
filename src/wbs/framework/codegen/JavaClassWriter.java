package wbs.framework.codegen;

import static wbs.framework.utils.etc.CollectionUtils.collectionHasOneElement;
import static wbs.framework.utils.etc.CollectionUtils.collectionIsEmpty;
import static wbs.framework.utils.etc.CollectionUtils.collectionIsNotEmpty;
import static wbs.framework.utils.etc.CollectionUtils.listFirstElementRequired;
import static wbs.framework.utils.etc.CollectionUtils.listLastElementRequired;
import static wbs.framework.utils.etc.CollectionUtils.listSliceAllButLastItemRequired;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.joinWithFullStop;
import static wbs.framework.utils.etc.StringUtils.joinWithSpace;
import static wbs.framework.utils.etc.StringUtils.stringFormatArray;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.utils.formatwriter.FormatWriter;

@Accessors (fluent = true)
public 
class JavaClassWriter
	implements JavaBlockWriter {

	// properties

	@Getter @Setter
	String className;

	@Getter @Setter
	List <JavaAnnotationWriter> classAnnotations =
		new ArrayList<> ();

	@Getter @Setter
	List <String> classModifiers =
		new ArrayList<> ();

	@Getter @Setter
	String extendsClassName;

	@Getter @Setter
	List <String> implementsInterfaceNames =
		new ArrayList<> ();

	@Getter @Setter
	List <JavaBlockWriter> blocks =
		new ArrayList<> ();

	// setters and getters

	public
	JavaClassWriter classNameFormat (
			@NonNull String ... arguments) {

		return className (
			stringFormatArray (
				arguments));

	}

	public
	JavaClassWriter addClassAnnotation (
			@NonNull JavaAnnotationWriter annotation) {

		classAnnotations.add (
			annotation);

		return this;

	}

	public
	JavaClassWriter addClassModifier (
			@NonNull String modifier) {

		classModifiers.add (
			modifier);

		return this;

	}

	public
	JavaClassWriter addImplements (
			@NonNull String interfaceName) {

		implementsInterfaceNames.add (
			interfaceName);

		return this;

	}

	public
	JavaClassWriter addImplements (
			@NonNull String packageName,
			@NonNull String interfaceName) {

		implementsInterfaceNames.add (
			joinWithFullStop (
				packageName,
				interfaceName));

		return this;

	}

	public
	JavaClassWriter addImplements (
			@NonNull Class <?> interfaceClass) {

		implementsInterfaceNames.add (
			interfaceClass.getName ());

		return this;

	}

	public
	JavaClassWriter addBlock (
			@NonNull JavaBlockWriter block) {

		blocks.add (
			block);

		return this;

	}

	// implementation

	@Override
	public
	void writeBlock (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		// class annotations

		for (
			JavaAnnotationWriter annotationWriter
				: classAnnotations
		) {

			annotationWriter.writeAnnotation (
				imports,
				formatWriter);

		}

		// class modifiers

		if (
			collectionIsNotEmpty (
				classModifiers)
		) {

			formatWriter.writeLineFormat (
				"%s",
				joinWithSpace (
					classModifiers));

		}

		// class declaration

		if (

			isNull (
				extendsClassName)

			&& collectionIsEmpty (
				implementsInterfaceNames)

		) {

			formatWriter.writeLineFormat (
				"class %s {",
				className);

		} else {

			formatWriter.writeLineFormat (
				"class %s",
				className);

		}

		// class extends declaration

		if (
			isNotNull (
				extendsClassName)
		) {

			if (
				collectionIsEmpty (
					implementsInterfaceNames)
			) {

				formatWriter.writeLineFormat (
					"\textends %s {",
					imports.register (
						extendsClassName));

			} else {

				formatWriter.writeLineFormat (
					"\textends %s",
					imports.register (
						extendsClassName));

			}

		}

		// class implements declaration

		if (
			collectionIsNotEmpty (
				implementsInterfaceNames)
		) {

			if (
				collectionHasOneElement (
					implementsInterfaceNames)
			) {

				formatWriter.writeLineFormat (
					"\timplements %s {",
					imports.register (
						listFirstElementRequired (
							implementsInterfaceNames)));

			} else {

				formatWriter.writeLineFormat (
					"\timplements");

				for (
					String implementsInterfaceName
						: listSliceAllButLastItemRequired (
							implementsInterfaceNames)
				) {

					formatWriter.writeLineFormat (
						"\t\t%s,",
						imports.register (
							implementsInterfaceName));

				}

				formatWriter.writeLineFormat (
					"\t\t%s {",
					imports.register (
						listLastElementRequired (
							implementsInterfaceNames)));

			}

		}

		formatWriter.writeNewline ();

		// class body

		formatWriter.increaseIndent ();

		blocks.forEach (
			block ->
				block.writeBlock (
					imports,
					formatWriter));

		formatWriter.decreaseIndent ();

		// end class

		formatWriter.writeLineFormat (
			"}");

	}

}
