package wbs.framework.codegen;

import static wbs.framework.utils.etc.CollectionUtils.collectionHasOneElement;
import static wbs.framework.utils.etc.CollectionUtils.collectionIsEmpty;
import static wbs.framework.utils.etc.CollectionUtils.collectionIsNotEmpty;
import static wbs.framework.utils.etc.CollectionUtils.listFirstElementRequired;
import static wbs.framework.utils.etc.CollectionUtils.listLastElementRequired;
import static wbs.framework.utils.etc.CollectionUtils.listSliceAllButLastItemRequired;
import static wbs.framework.utils.etc.LogicUtils.ifThenElse;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.joinWithFullStop;
import static wbs.framework.utils.etc.StringUtils.joinWithSpace;
import static wbs.framework.utils.etc.StringUtils.stringFormatArray;
import static wbs.framework.utils.etc.StringUtils.uncapitalise;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.inject.Provider;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;
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

	@Getter @Setter
	List <Dependency> singletonDependencies =
		new ArrayList<> ();

	@Getter @Setter
	List <Dependency> prototypeDependencies =
		new ArrayList<> ();

	@Getter @Setter
	List <State> states =
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

	public
	JavaClassWriter addSingletonDependency (
			@NonNull String typeName,
			@NonNull String variableName) {

		return addSingletonDependency (
			typeName,
			variableName,
			false);

	}

	public
	JavaClassWriter addNamedSingletonDependency (
			@NonNull Class <?> typeClass,
			@NonNull String variableName) {

		return addSingletonDependency (
			typeClass.getName (),
			variableName,
			true);

	}

	public
	JavaClassWriter addNamedSingletonDependency (
			@NonNull String typeName,
			@NonNull String variableName) {

		return addSingletonDependency (
			typeName,
			variableName,
			true);

	}

	public
	JavaClassWriter addSingletonDependency (
			@NonNull String typeName,
			@NonNull String variableName,
			@NonNull Boolean named) {

		singletonDependencies.add (
			new Dependency ()

			.className (
				typeName)

			.memberName (
				variableName)

			.named (
				named)

		);

		return this;

	}

	public
	JavaClassWriter addSingletonDependency (
			@NonNull Class <?> dependencyClass) {

		SingletonComponent singletonComponentAnnotation =
			(SingletonComponent)
			dependencyClass.getAnnotation (
				SingletonComponent.class);

		String variableName;

		if (
			isNotNull (
				singletonComponentAnnotation)
		) {

			variableName =
				singletonComponentAnnotation.value ();

		} else {

			variableName =
				uncapitalise (
					dependencyClass.getSimpleName ());

		}

		return addSingletonDependency (
			dependencyClass.getName (),
			variableName);

	}

	public
	JavaClassWriter addPrototypeDependency (
			@NonNull String typeName,
			@NonNull String variableName) {

		return addPrototypeDependency (
			typeName,
			variableName,
			false);

	}

	public
	JavaClassWriter addPrototypeDependency (
			@NonNull String typeName,
			@NonNull String variableName,
			@NonNull Boolean named) {

		prototypeDependencies.add (
			new Dependency ()

			.className (
				typeName)

			.memberName (
				variableName)

			.named (
				named)

		);

		return this;

	}

	public
	JavaClassWriter addPrototypeDependency (
			@NonNull Class <?> dependencyClass) {

		PrototypeComponent prototypeComponentAnnotation =
			(PrototypeComponent)
			dependencyClass.getAnnotation (
				PrototypeComponent.class);

		String variableName =
			ifThenElse (
				isNotNull (
					prototypeComponentAnnotation),
				() ->
					prototypeComponentAnnotation.value (),
				() ->
					uncapitalise (
						dependencyClass.getSimpleName ()));

		return addPrototypeDependency (
			dependencyClass.getName (),
			variableName);

	}

	public
	JavaClassWriter addState (
			@NonNull Class <?> classObject,
			@NonNull String memberName) {

		return addState (
			classObject.getName (),
			memberName);

	}

	public
	JavaClassWriter addState (
			@NonNull String typeName,
			@NonNull String memberName) {

		states.add (
			new State ()

			.typeNameSupplier (
				imports ->
					imports.register (
						typeName))

			.memberName (
				memberName)

		);

		return this;

	}

	public
	JavaClassWriter addState (
			@NonNull Function <JavaImportRegistry, String> typeNameSupplier,
			@NonNull String memberName) {

		states.add (
			new State ()

			.typeNameSupplier (
				typeNameSupplier)

			.memberName (
				memberName)

		);

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

		writeSingletonDependencies (
			imports,
			formatWriter);

		writePrototypeDependencies (
			imports,
			formatWriter);

		writeState (
			imports,
				formatWriter);

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

	public
	JavaClassWriter writeSingletonDependencies (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		if (
			collectionIsEmpty (
				singletonDependencies)
		) {
			return this;
		}

		formatWriter.writeLineFormat (
			"// singleton dependencies");

		formatWriter.writeNewline ();

		for (
			Dependency dependency
				: singletonDependencies
		) {

			formatWriter.writeLineFormat (
				"@%s",
				imports.register (
					SingletonDependency.class));

			formatWriter.writeLineFormat (
				"%s %s;",
				imports.register (
					dependency.className),
				dependency.memberName);

			formatWriter.writeNewline ();

		}

		return this;

	}

	public
	JavaClassWriter writePrototypeDependencies (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		if (
			collectionIsEmpty (
				prototypeDependencies)
		) {
			return this;
		}

		formatWriter.writeLineFormat (
			"// prototype dependencies");

		formatWriter.writeNewline ();

		for (
			Dependency dependency
				: prototypeDependencies
		) {

			formatWriter.writeLineFormat (
				"@%s",
				imports.register (
					PrototypeDependency.class));

			formatWriter.writeLineFormat (
				"%s <%s> %sProvider;",
				imports.register (
					Provider.class),
				imports.register (
					dependency.className),
				dependency.memberName);

			formatWriter.writeNewline ();

		}

		return this;

	}

	public
	JavaClassWriter writeState (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		if (
			collectionIsEmpty (
				states)
		) {
			return this;
		}

		formatWriter.writeLineFormat (
			"// state");

		formatWriter.writeNewline ();

		for (
			State state
				: states
		) {

			formatWriter.writeLineFormat (
				"%s %s;",
				state.typeNameSupplier.apply (
					imports),
				state.memberName ());

		}

		formatWriter.writeNewline ();

		return this;

	}


	@Accessors (fluent = true)
	@Data
	public static
	class Dependency {

		String className;
		String memberName;
		Boolean named = false;

	}

	@Accessors (fluent = true)
	@Data
	public static
	class State {

		Function <JavaImportRegistry, String> typeNameSupplier;
		String memberName;

		Boolean getter = false;
		Boolean setter = false;

	}

}
