package wbs.framework.codegen;

import static wbs.framework.utils.etc.ArrayUtils.arrayIsNotEmpty;
import static wbs.framework.utils.etc.CollectionUtils.collectionHasOneElement;
import static wbs.framework.utils.etc.CollectionUtils.collectionIsEmpty;
import static wbs.framework.utils.etc.CollectionUtils.collectionIsNotEmpty;
import static wbs.framework.utils.etc.CollectionUtils.listFirstElementRequired;
import static wbs.framework.utils.etc.CollectionUtils.listLastElementRequired;
import static wbs.framework.utils.etc.CollectionUtils.listSlice;
import static wbs.framework.utils.etc.CollectionUtils.listSliceAllButLastItemRequired;
import static wbs.framework.utils.etc.IterableUtils.iterableMap;
import static wbs.framework.utils.etc.LogicUtils.ifThenElse;
import static wbs.framework.utils.etc.Misc.contains;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;
import static wbs.framework.utils.etc.StringUtils.joinWithSpace;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormatArray;
import static wbs.framework.utils.etc.StringUtils.uncapitalise;
import static wbs.framework.utils.etc.TypeUtils.parameterSourceTypeName;
import static wbs.framework.utils.etc.TypeUtils.typeSourceName;
import static wbs.framework.utils.etc.TypeUtils.typeVariableSourceDeclaration;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
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
	List <Function <JavaImportRegistry, String>> implementsInterfaces =
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

	@Getter @Setter
	Map <String, String> typeParameterMappings =
		new HashMap<> ();

	@Getter @Setter
	List <Pair <Class <?>, String>> delegations =
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
			@NonNull Function <JavaImportRegistry, String>
				implementsInterface) {

		implementsInterfaces.add (
			implementsInterface);

		return this;

	}

	public
	JavaClassWriter addImplementsName (
			@NonNull String interfaceName) {

		return addImplements (
			imports ->
				imports.register (
					interfaceName));

	}

	public
	JavaClassWriter addImplementsFormat (
			@NonNull String ... arguments) {

		return addImplementsName (
			stringFormatArray (
				arguments));

	}

	public
	JavaClassWriter addImplementsClass (
			@NonNull Class <?> interfaceClass) {

		return addImplementsName (
			interfaceClass.getName ());

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

	public
	JavaClassWriter addTypeParameterMapping (
			@NonNull String parameterName,
			@NonNull String parameterValue) {

		typeParameterMappings.put (
			parameterName,
			parameterValue);

		return this;

	}

	public
	JavaClassWriter addDelegation (
			@NonNull Class <?> delegateInterface,
			@NonNull String delegateName) {

		delegations.add (
			Pair.of (
				delegateInterface,
				delegateName));

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
				implementsInterfaces)

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
					implementsInterfaces)
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
				implementsInterfaces)
		) {

			if (
				collectionHasOneElement (
					implementsInterfaces)
			) {

				Function <JavaImportRegistry, String> implementsInterface =
					listFirstElementRequired (
						implementsInterfaces);

				formatWriter.writeLineFormat (
					"\timplements %s {",
					implementsInterface.apply (
						imports));

			} else {

				formatWriter.writeLineFormat (
					"\timplements");

				for (
					Function <JavaImportRegistry, String> implementsInterface
						: listSliceAllButLastItemRequired (
							implementsInterfaces)
				) {

					formatWriter.writeLineFormat (
						"\t\t%s,",
						implementsInterface.apply (
							imports));

				}

				Function <JavaImportRegistry, String> implementsInterface =
					listLastElementRequired (
						implementsInterfaces);

				formatWriter.writeLineFormat (
					"\t\t%s {",
					implementsInterface.apply (
						imports));

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

		writeDelegations (
			imports,
			formatWriter);

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

	void writeDelegations (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		Set <Pair <String, List <Class <?>>>> delegatedMethods =
			new HashSet<> ();

		for (
			Pair <Class <?>, String> delegate
				: delegations
		) {

			Class <?> delegateInterface =
				delegate.getLeft ();

			String delegateName =
				delegate.getRight ();

			formatWriter.writeLineFormat (
				"// delegate %s",
				camelToSpaces (
					uncapitalise (
						delegateInterface.getSimpleName ())));

			formatWriter.writeNewline ();

			for (
				Method method
					: delegateInterface.getMethods ()
			) {

				// don't add the same method twice

				Pair <String, List <Class <?>>> methodKey =
					Pair.of (
						method.getName (),
						ImmutableList.copyOf (
							method.getParameterTypes ()));

				if (
					contains (
						delegatedMethods,
						methodKey)
				) {
					continue;
				}

				delegatedMethods.add (
					methodKey);

				// write method delegate

				writeDelegationMethod (
					imports,
					formatWriter,
					delegateInterface,
					delegateName,
					method);

			}

		}

	}

	void writeDelegationMethod (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter,
			@NonNull Class <?> delegationInterface,
			@NonNull String delegationName,
			@NonNull Method method) {

		String returnTypeName =
			typeSourceName (
				imports,
				typeParameterMappings,
				method.getGenericReturnType ());

		formatWriter.writeLineFormat (
			"@%s",
			imports.register (
				Override.class));

		if (
			arrayIsNotEmpty (
				method.getTypeParameters ())
		) {

			formatWriter.writeLineFormat (
				"public <%s>",
				joinWithCommaAndSpace (
					iterableMap (
						typeParameter ->
							typeVariableSourceDeclaration (
								imports,
								typeParameterMappings,
								typeParameter),
						Arrays.asList (
							method.getTypeParameters ()))));

		} else {

			formatWriter.writeLineFormat (
				"public");

		}

		List <Parameter> parameters =
			ImmutableList.copyOf (
				method.getParameters ());

		if (
			collectionIsEmpty (
				parameters)
		) {

			formatWriter.writeLineFormat (
				"%s %s () {",
				returnTypeName,
				method.getName ());

		} else {

			formatWriter.writeLineFormat (
				"%s %s (",
				returnTypeName,
				method.getName ());

			for (
				Parameter parameter
					: listSliceAllButLastItemRequired (
						parameters)
			) {

				formatWriter.writeLineFormat (
					"\t\t%s %s,",
					parameterSourceTypeName (
						imports,
						typeParameterMappings,
						parameter),
					parameter.getName ());

			}

			Parameter lastParameter =
				listLastElementRequired (
					parameters);

			formatWriter.writeLineFormat (
				"\t\t%s %s) {",
				parameterSourceTypeName (
					imports,
					typeParameterMappings,
					lastParameter),
				lastParameter.getName ());

		}

		formatWriter.writeNewline ();

		if (
			stringEqualSafe (
				returnTypeName,
				"void")
		) {

			if (
				collectionIsEmpty (
					parameters)
			) {

				formatWriter.writeLineFormat (
					"\t%s.%s ();",
					delegationName,
					method.getName ());

			} else {

				formatWriter.writeLineFormat (
					"\t%s.%s (",
					delegationName,
					method.getName ());

				for (
					Parameter parameter
						: listSlice (
							parameters,
							0,
							parameters.size () - 1)
				) {

					formatWriter.writeLineFormat (
						"\t\t%s,",
						parameter.getName ());

				}

				Parameter lastParameter =
					listLastElementRequired (
						parameters);

				formatWriter.writeLineFormat (
					"\t\t%s);",
					lastParameter.getName ());

			}

		} else {

			if (
				collectionIsEmpty (
					parameters)
			) {

				formatWriter.writeLineFormat (
					"\treturn %s.%s ();",
					delegationName,
					method.getName ());

			} else {

				formatWriter.writeLineFormat (
					"\treturn %s.%s (",
					delegationName,
					method.getName ());

				for (
					Parameter parameter
						: listSlice (
							parameters,
							0,
							parameters.size () - 1)
				) {

					formatWriter.writeLineFormat (
						"\t\t%s,",
						parameter.getName ());

				}

				Parameter lastParameter =
					listLastElementRequired (
						parameters);

				formatWriter.writeLineFormat (
					"\t\t%s);",
					lastParameter.getName ());

			}

		}

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

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
