package wbs.console.helper;

import static wbs.framework.utils.etc.ArrayUtils.arrayIsEmpty;
import static wbs.framework.utils.etc.ArrayUtils.arrayIsNotEmpty;
import static wbs.framework.utils.etc.ArrayUtils.arrayMap;
import static wbs.framework.utils.etc.CollectionUtils.collectionIsEmpty;
import static wbs.framework.utils.etc.CollectionUtils.collectionSize;
import static wbs.framework.utils.etc.CollectionUtils.listLastElementRequired;
import static wbs.framework.utils.etc.CollectionUtils.listSlice;
import static wbs.framework.utils.etc.IterableUtils.iterableMap;
import static wbs.framework.utils.etc.LogicUtils.ifThenElse;
import static wbs.framework.utils.etc.LogicUtils.referenceNotEqualUnsafe;
import static wbs.framework.utils.etc.Misc.contains;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;
import static wbs.framework.utils.etc.StringUtils.joinWithFullStop;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringReplaceAllRegex;
import static wbs.framework.utils.etc.StringUtils.stringSplitFullStop;
import static wbs.framework.utils.etc.StringUtils.stringStartsWithSimple;
import static wbs.framework.utils.etc.StringUtils.uncapitalise;
import static wbs.framework.utils.etc.TypeUtils.classEqualSafe;
import static wbs.framework.utils.etc.TypeUtils.classForName;
import static wbs.framework.utils.etc.TypeUtils.classForNameRequired;
import static wbs.framework.utils.etc.TypeUtils.classNameFull;
import static wbs.framework.utils.etc.TypeUtils.classPackageName;
import static wbs.framework.utils.etc.TypeUtils.isInstanceOf;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelMethods;
import wbs.framework.object.ObjectModelMethods;
import wbs.framework.utils.etc.RuntimeIoException;
import wbs.framework.utils.formatwriter.AtomicFileWriter;
import wbs.framework.utils.formatwriter.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("consoleHelperGenerator")
public
class ConsoleHelperGenerator {

	// dependencies

	@SingletonDependency
	EntityHelper entityHelper;

	// properties

	@Getter @Setter
	Model model;

	// state

	Model parentModel;

	String packageName;
	String recordClassName;
	String objectHelperInterfaceName;
	String objectHelperImplementationName;

	boolean hasDao;
	String daoMethodsInterfaceName;
	String daoComponentName;
	Class <?> daoMethodsInterface;

	boolean hasExtra;
	String extraMethodsInterfaceName;
	String extraComponentName;
	Class <?> extraMethodsInterface;

	String hooksComponentName;

	Class <?> objectHelperInterface;

	FormatWriter javaWriter;

	Set <Pair <String, List <Class <?>>>> delegatedMethods =
		new HashSet<> ();

	// implementation

	public
	void generateHelper () {

		if (model.parentTypeIsFixed ()) {

			parentModel =
				ifThenElse (
					model.isRooted (),

				() -> entityHelper.modelsByName ().get (
					"root"),

				() -> ifThenElse (
					model.parentTypeIsFixed ()
					&& ! model.isRoot (),

					() -> entityHelper.modelsByClass ().get (
						model.parentField ().valueType ()),

					() -> null

				)

			);

		}

		List <String> modelPackageNameParts =
			stringSplitFullStop (
				classPackageName (
					model.objectClass ()));

		packageName =
			joinWithFullStop (
				listSlice (
					modelPackageNameParts,
					0l,
					collectionSize (
						modelPackageNameParts) - 1));

		recordClassName =
			stringFormat (
				"%sRec",
				capitalise (
					model.objectName ()));

		objectHelperInterfaceName =
			stringFormat (
				"%sObjectHelper",
				capitalise (
					model.objectName ()));

		objectHelperImplementationName =
			stringFormat (
				"%sObjectHelperImplementation",
				capitalise (
					model.objectName ()));

		objectHelperInterface =
			classForNameRequired (
				packageName + ".model",
				objectHelperInterfaceName);

		// dao

		daoMethodsInterfaceName =
			stringFormat (
				"%sDaoMethods",
				capitalise (
					model.objectName ()));

		Optional <Class <?>> daoMethodsInterfaceOptional =
			classForName (
				packageName + ".model",
				daoMethodsInterfaceName);

		if (
			optionalIsPresent (
				daoMethodsInterfaceOptional)
		) {

			hasDao =
				true;

			daoMethodsInterface =
				optionalGetRequired (
					daoMethodsInterfaceOptional);

			daoComponentName =
				stringFormat (
					"%sDao",
					uncapitalise (
						model.objectName ()));

		} else {

			hasDao =
				false;

		}

		// extra

		extraMethodsInterfaceName =
			stringFormat (
				"%sObjectHelperMethods",
				capitalise (
					model.objectName ()));

		Optional <Class <?>> extraMethodsInterfaceOptional =
			classForName (
				packageName + ".model",
				extraMethodsInterfaceName);

		if (
			optionalIsPresent (
				extraMethodsInterfaceOptional)
		) {

			hasExtra =
				true;

			extraMethodsInterface =
				optionalGetRequired (
					extraMethodsInterfaceOptional);

			extraComponentName =
				stringFormat (
					"%sObjectHelperMethodsImplementation",
					uncapitalise (
						model.objectName ()));

		} else {

			hasExtra =
				false;

		}

		// hooks

		hooksComponentName =
			stringFormat (
				"%sHooks",
				model.objectName ());

		// create directory

		String directory =
			stringFormat (
				"work/generated/%s/logic",
				packageName.replace ('.', '/'));

		try {

			FileUtils.forceMkdir (
				new File (
					directory));

		} catch (IOException exception) {

			throw new RuntimeIoException (
				exception);

		}

		// write class

		String filename =
			stringFormat (
				"%s/%s.java",
				directory,
				objectHelperImplementationName);

		@Cleanup
		FormatWriter javaWriterTemp =
			new AtomicFileWriter (
				filename);

		setJavaWriter (
			javaWriterTemp);

		javaWriter.writeFormat (
			"package %s.logic;\n",
			packageName);

		javaWriter.writeFormat (
			"\n");

		writeImports ();
		writeClass ();

	}

	private
	void setJavaWriter (
			@NonNull FormatWriter javaWriter) {

		this.javaWriter =
			javaWriter;

	}

	private
	void writeImports () {

		for (
			Class<?> standardImportClass
				: standardImportClasses
		) {

			javaWriter.writeFormat (
				"import %s;\n",
				standardImportClass.getName ());

		}

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"import %s;\n",
			model.objectClass ().getName ());

		for (
			String componentName
				: componentNames
		) {

			javaWriter.writeFormat (
				"import %s;\n",
				stringFormat (
					"wbs.framework.object.ObjectHelper%sImplementation",
					capitalise (
						componentName)));

		}

		if (hasDao) {

			javaWriter.writeFormat (
				"import %s;\n",
				daoMethodsInterface.getName ());

		}

		if (hasExtra) {

			javaWriter.writeFormat (
				"import %s;\n",
				extraMethodsInterface.getName ());

		}

		javaWriter.writeFormat (
			"\n");

	}

	void writeClass () {

		javaWriter.writeFormat (
			"@SuppressWarnings ({ \"rawtypes\", \"unchecked\" })\n");

		javaWriter.writeFormat (
			"public\n");

		javaWriter.writeFormat (
			"class %s\n",
			objectHelperImplementationName);

		javaWriter.writeFormat (
			"\timplements\n");

		javaWriter.writeFormat (
			"\t\tObjectHelperImplementation,\n");

		javaWriter.writeFormat (
			"\t\t%s.model.%s {\n",
			packageName,
			objectHelperInterfaceName);

		javaWriter.writeFormat (
			"\n");

		writeDependencies ();
		writePrototypeDependencies ();
		writeState ();

		writeLifecycle ();
		writeImplementation ();
		writeDelegations ();

		javaWriter.writeFormat (
			"}\n");

	}

	void writeDependencies () {

		javaWriter.writeFormat (
			"\t// dependencies\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t@SingletonDependency\n");

		javaWriter.writeFormat (
			"\tApplicationContext applicationContext;\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t@SingletonDependency\n");

		javaWriter.writeFormat (
			"\tDatabase database;\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t@SingletonDependency\n");

		javaWriter.writeFormat (
			"\tEntityHelper entityHelper;\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t@SingletonDependency\n");

		javaWriter.writeFormat (
			"\tObjectTypeRegistry objectTypeRegistry;\n");

		javaWriter.writeFormat (
			"\n");

		if (hasDao) {

			javaWriter.writeFormat (
				"\t@SingletonDependency\n");

			javaWriter.writeFormat (
				"\t@Named\n");

			javaWriter.writeFormat (
				"\t%s %s;\n",
				daoMethodsInterfaceName,
				daoComponentName);

			javaWriter.writeFormat (
				"\n");

		}

		if (hasExtra) {

			javaWriter.writeFormat (
				"\t@SingletonDependency\n");

			javaWriter.writeFormat (
				"\t@Named\n");

			javaWriter.writeFormat (
				"\t%s %s;\n",
				extraMethodsInterfaceName,
				extraComponentName);

			javaWriter.writeFormat (
				"\n");

		}

	}

	void writePrototypeDependencies () {

		javaWriter.writeFormat (
			"\t// prototype dependencies\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t@PrototypeDependency\n");

		javaWriter.writeFormat (
			"\tProvider <ObjectDatabaseHelper>\n");

		javaWriter.writeFormat (
			"\tobjectDatabaseHelperProvider;\n");

		javaWriter.writeFormat (
			"\n");

		for (
			String componentName
				: componentNames
		) {

			javaWriter.writeFormat (
				"\t@PrototypeDependency\n");

			javaWriter.writeFormat (
				"\tProvider <%s>\n",
				stringFormat (
					"ObjectHelper%sImplementation <%s>",
					capitalise (
						componentName),
					model.objectClass ().getSimpleName ()));

			javaWriter.writeFormat (
				"\t%s;\n",
				stringFormat (
					"objectHelper%sImplementationProvider",
					capitalise (
						componentName)));

			javaWriter.writeFormat (
				"\n");

		}

	}

	void writeState () {

		javaWriter.writeFormat (
			"\t// state\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\tModel parentModel;\n");

		javaWriter.writeFormat (
			"\tModel model;\n");

		javaWriter.writeFormat (
			"\tObjectModel objectModel;\n");

		javaWriter.writeFormat (
			"\tObjectDatabaseHelper databaseHelper;\n");

		javaWriter.writeFormat (
			"\tObjectHooks hooksImplementation;\n");

		javaWriter.writeFormat (
			"\n");

		for (
			String componentName
				: componentNames
		) {

			javaWriter.writeFormat (
				"\t%s %s;\n",
				stringFormat (
					"ObjectHelper%sImplementation <%s>",
					capitalise (
						componentName),
					model.objectClass ().getSimpleName ()),
				stringFormat (
					"%sImplementation",
					componentName));

		}

		javaWriter.writeFormat (
			"\n");

	}

	void writeLifecycle () {

		javaWriter.writeFormat (
			"\t// lifecycle\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t@PostConstruct\n");

		javaWriter.writeFormat (
			"\tpublic\n");

		javaWriter.writeFormat (
			"\tvoid setup () {\n");

		javaWriter.writeFormat (
			"\n");

		// start transaction

		javaWriter.writeFormat (
			"\t\ttry (\n");

		javaWriter.writeFormat (
			"\t\t\tTransaction transaction =\n");

		javaWriter.writeFormat (
			"\t\t\t\tdatabase.beginReadOnly (\n");

		javaWriter.writeFormat (
			"\t\t\t\t\t\"setup\",\n");

		javaWriter.writeFormat (
			"\t\t\t\t\tthis)\n");

		javaWriter.writeFormat (
			"\t\t) {\n");

		javaWriter.writeFormat (
			"\n");

		// model

		javaWriter.writeFormat (
			"\t\t\tmodel =\n");

		javaWriter.writeFormat (
			"\t\t\t\tentityHelper.modelsByName ().get (\n");

		javaWriter.writeFormat (
			"\t\t\t\t\t\"%s\");\n",
			model.objectName ());

		javaWriter.writeFormat (
			"\n");

		// object type

		javaWriter.writeFormat (
			"\t\t\tObjectTypeEntry objectType =\n");

		javaWriter.writeFormat (
			"\t\t\t\tobjectTypeRegistry.findByCode (\n");

		javaWriter.writeFormat (
			"\t\t\t\t\t\"%s\");\n",
			model.objectTypeCode ());

		javaWriter.writeFormat (
			"\n");

		if (
			isNotNull (
				parentModel)
		) {

			javaWriter.writeFormat (
				"\t\t\tparentModel =\n");

			javaWriter.writeFormat (
				"\t\t\t\tentityHelper.modelsByName ().get (\n");

			javaWriter.writeFormat (
				"\t\t\t\t\t\"%s\");\n",
				parentModel.objectName ());

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\tObjectTypeEntry parentType =\n");

			javaWriter.writeFormat (
				"\t\t\t\tobjectTypeRegistry.findByCode (\n");

			javaWriter.writeFormat (
				"\t\t\t\t\t\"%s\");\n",
				parentModel.objectTypeCode ());

			javaWriter.writeFormat (
				"\n");

		}

		// hooks

		javaWriter.writeFormat (
			"\t\t\t\thooksImplementation =\n");

		javaWriter.writeFormat (
			"\t\t\t\t\tOptionalUtils.optionalOrNull (\n");

		javaWriter.writeFormat (
			"\t\t\t\t\t\tapplicationContext.getComponent (\n");

		javaWriter.writeFormat (
			"\t\t\t\t\t\t\t\"%s\",\n",
			hooksComponentName);

		javaWriter.writeFormat (
			"\t\t\t\t\t\t\tObjectHooks.class));\n");

		javaWriter.writeFormat (
			"\n");

		// object model

		javaWriter.writeFormat (
			"\t\t\tObjectModel objectModel =\n");

		javaWriter.writeFormat (
			"\t\t\t\tnew ObjectModelImplementation ()\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\t\t\t.model (\n");

		javaWriter.writeFormat (
			"\t\t\t\t\tmodel)\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\t\t\t.objectTypeId (\n");

		javaWriter.writeFormat (
			"\t\t\t\t\tobjectType.getId ())\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\t\t\t.objectTypeCode (\n");

		javaWriter.writeFormat (
			"\t\t\t\t\tobjectType.getCode ())\n");

		javaWriter.writeFormat (
			"\n");

		if (
			isNotNull (
				parentModel)
		) {

			javaWriter.writeFormat (
				"\t\t\t\t.parentTypeId (\n");

			javaWriter.writeFormat (
				"\t\t\t\t\tparentType.getId ())\n");

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t\t.parentClass (\n");

			javaWriter.writeFormat (
				"\t\t\t\t\tparentModel.objectClass ())\n");

			javaWriter.writeFormat (
				"\n");

		}

		if (hasDao) {

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t\t.daoImplementation (\n");

			javaWriter.writeFormat (
				"\t\t\t\t\t%s)\n",
				daoComponentName);

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t\t.daoInterface (\n");

			javaWriter.writeFormat (
				"\t\t\t\t\t%s.class)\n",
				daoMethodsInterfaceName);

			javaWriter.writeFormat (
				"\n");

		}

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\t\t\t.hooks (\n");

		javaWriter.writeFormat (
			"\t\t\t\t\thooksImplementation)\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\t\t;\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\t\tdatabaseHelper =\n");

		javaWriter.writeFormat (
			"\t\t\t\tobjectDatabaseHelperProvider.get ()\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\t\t\t.model (\n");

		javaWriter.writeFormat (
			"\t\t\t\t\tobjectModel);\n");

		javaWriter.writeFormat (
			"\n");

		for (
			String componentName
				: componentNames
		) {

			javaWriter.writeFormat (
				"\t\t\t%sImplementation =\n",
				componentName);

			javaWriter.writeFormat (
				"\t\t\t\t%s.get ()\n",
				stringFormat (
					"objectHelper%sImplementationProvider",
					capitalise (
						componentName)));

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t\t.objectHelper (\n");

			javaWriter.writeFormat (
				"\t\t\t\t\tthis)\n");

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t\t.objectDatabaseHelper (\n");

			javaWriter.writeFormat (
				"\t\t\t\t\tdatabaseHelper)\n");

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t\t.model (\n");

			javaWriter.writeFormat (
				"\t\t\t\t\tobjectModel);\n");

			javaWriter.writeFormat (
				"\n");

		}

		javaWriter.writeFormat (
			"\t\t}\n");

		javaWriter.writeFormat (
			"\t}\n");

		javaWriter.writeFormat (
			"\n");

	}

	void writeImplementation () {

		javaWriter.writeFormat (
			"\t// implementation\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t@Override\n");

		javaWriter.writeFormat (
			"\tpublic\n");

		javaWriter.writeFormat (
			"\tvoid objectManager (\n");

		javaWriter.writeFormat (
			"\t\t\tObjectManager objectManager) {\n");

		javaWriter.writeFormat (
			"\n");

		for (
			String componentName
				: componentNames
		) {

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t%sImplementation.objectManager (\n",
				componentName);

			javaWriter.writeFormat (
				"\t\t\t\tobjectManager);\n");

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t%sImplementation.setup ();\n",
				componentName);

			javaWriter.writeFormat (
				"\n");

		}

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t}\n");

		javaWriter.writeFormat (
			"\n");

	}

	void writeDelegations () {

		if (hasExtra) {

			writeDelegate (
				extraMethodsInterface,
				extraComponentName);

		}

		if (hasDao) {

			writeDelegate (
				daoMethodsInterface,
				daoComponentName);

		}

		for (
			Map.Entry <String, Class <?>> componentEntry
				: componentClassesByName.entrySet ()
		) {

			String componentName =
				componentEntry.getKey ();

			Class <?> componentClass =
				componentEntry.getValue ();

			writeDelegate (
				componentClass,
				stringFormat (
					"%sImplementation",
					componentName));

		}

		writeDelegate (
			ObjectModelMethods.class,
			"objectModel");

		writeDelegate (
			ModelMethods.class,
			"model");

	}

	void writeDelegate (
			@NonNull Class <?> delegateInterface,
			@NonNull String delegateName) {

		javaWriter.writeFormat (
			"\t// delegate %s\n",
			delegateInterface.getSimpleName ());

		javaWriter.writeFormat (
			"\n");

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

			String returnTypeName =
				methodReturnSourceName (
					method);

			javaWriter.writeFormat (
				"\t@Override\n");

			if (
				arrayIsNotEmpty (
					method.getTypeParameters ())
			) {

				javaWriter.writeFormat (
					"\tpublic <%s>\n",
					joinWithCommaAndSpace (
						Arrays.stream (
							method.getTypeParameters ())

					.map (
						this::typeVariableSourceDeclaration)

					.collect (
						Collectors.toList ()

				)));

			} else {

				javaWriter.writeFormat (
					"\tpublic\n");

			}

			List <Parameter> parameters =
				ImmutableList.copyOf (
					method.getParameters ());

			if (
				collectionIsEmpty (
					parameters)
			) {

				javaWriter.writeFormat (
					"\t%s %s () {\n",
					returnTypeName,
					method.getName ());

			} else {

				javaWriter.writeFormat (
					"\t%s %s (\n",
					returnTypeName,
					method.getName ());

				for (
					Parameter parameter
						: listSlice (
							parameters,
							0,
							parameters.size () - 1)
				) {

					javaWriter.writeFormat (
						"\t\t\t%s %s,\n",
						parameterSourceName (
							parameter),
						parameter.getName ());

				}

				Parameter lastParameter =
					listLastElementRequired (
						parameters);

				javaWriter.writeFormat (
					"\t\t\t%s %s) {\n",
					parameterSourceName (
						lastParameter),
					lastParameter.getName ());

			}

			javaWriter.writeFormat (
				"\n");

			if (
				stringEqualSafe (
					returnTypeName,
					"void")
			) {

				if (
					collectionIsEmpty (
						parameters)
				) {

					javaWriter.writeFormat (
						"\t\t%s.%s ();\n",
						delegateName,
						method.getName ());

				} else {

					javaWriter.writeFormat (
						"\t\t%s.%s (\n",
						delegateName,
						method.getName ());

					for (
						Parameter parameter
							: listSlice (
								parameters,
								0,
								parameters.size () - 1)
					) {

						javaWriter.writeFormat (
							"\t\t\t%s,\n",
							parameter.getName ());

					}

					Parameter lastParameter =
						listLastElementRequired (
							parameters);

					javaWriter.writeFormat (
						"\t\t\t%s);\n",
						lastParameter.getName ());

				}

			} else {

				if (
					collectionIsEmpty (
						parameters)
				) {

					javaWriter.writeFormat (
						"\t\treturn %s.%s ();\n",
						delegateName,
						method.getName ());

				} else {

					javaWriter.writeFormat (
						"\t\treturn %s.%s (\n",
						delegateName,
						method.getName ());

					for (
						Parameter parameter
							: listSlice (
								parameters,
								0,
								parameters.size () - 1)
					) {

						javaWriter.writeFormat (
							"\t\t\t%s,\n",
							parameter.getName ());

					}

					Parameter lastParameter =
						listLastElementRequired (
							parameters);

					javaWriter.writeFormat (
						"\t\t\t%s);\n",
						lastParameter.getName ());

				}

			}

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t}\n");

			javaWriter.writeFormat (
				"\n");

		}

	}

	String methodReturnSourceName (
			@NonNull Method method) {

		return stringReplaceAllRegex (
			"\\bRecordType\\b",
			model.objectClass ().getSimpleName (),
			typeSourceName (
				method.getGenericReturnType ()));

	}

	public
	String typeSourceName (
			@NonNull Type type) {

		if (
			isInstanceOf (
				Class.class,
				type)
		) {

			return classSourceName (
				(Class <?>)
				type);

		} else if (
			isInstanceOf (
				ParameterizedType.class,
				type)
		) {

			return parameterizedTypeSourceName (
				(ParameterizedType)
				type);

		} else if (
			isInstanceOf (
				TypeVariable.class,
				type)
		) {

			return typeVariableSourceName (
				(TypeVariable <?>)
				type);

		} else if (
			isInstanceOf (
				WildcardType.class,
				type)
		) {

			return wildcardTypeSourceName (
				(WildcardType)
				type);

		} else {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to handle a %s",
					type.getClass ().getSimpleName ()));

		}

	}

	public
	String classSourceName (
			@NonNull Class <?> theClass) {

		if (theClass.isArray ()) {

			return stringFormat (
				"%s[]",
				classSourceName (
					theClass.getComponentType ()));

		} else if (

			classEqualSafe (
				theClass,
				model.objectClass ())

			|| contains (
				standardImportClasses,
				theClass)

			|| stringStartsWithSimple (
				"java.lang.",
				theClass.getName ())

		) {

			return theClass.getSimpleName ();

		} else {

			return theClass.getName ();

		}

	}

	public
	String parameterizedTypeSourceName (
			@NonNull ParameterizedType parameterizedType) {

		if (
			arrayIsEmpty (
				parameterizedType.getActualTypeArguments ())
		) {

			return classNameFull (
				(Class <?>)
				parameterizedType.getRawType ());

		} else {

			return stringFormat (
				"%s <%s>",
				typeSourceName (
					parameterizedType.getRawType ()),
				joinWithCommaAndSpace (
					arrayMap (
						this::typeSourceName,
						parameterizedType.getActualTypeArguments ())));

		}

	}

	public
	String typeVariableSourceName (
			@NonNull TypeVariable <?> typeVariable) {

		return typeVariable.getName ();

	}

	public
	String typeVariableSourceDeclaration (
			@NonNull TypeVariable <?> typeVariable) {

		List <Type> bounds =
			Arrays.stream (
				typeVariable.getBounds ())

			.filter (
				bound ->
					referenceNotEqualUnsafe (
						bound,
						Object.class))

			.collect (
				Collectors.toList ());

		if (
			collectionIsEmpty (
				bounds)
		) {

			return typeVariable.getName ();

		} else {

			return stringFormat (
				"%s extends %s",
				typeVariable.getName (),
				joinWithCommaAndSpace (
					iterableMap (
						this::typeSourceName,
						bounds)));

		}

	}

	public
	String wildcardTypeSourceName (
			@NonNull WildcardType wildcardType) {

		return wildcardType.toString ();

	}

	public
	String parameterSourceName (
			@NonNull Parameter parameter) {

		if (parameter.isVarArgs ()) {

			return stringFormat (
				"%s ...",
				typeSourceName (
					parameter.getType ().getComponentType ()));

		} else {

			return typeSourceName (
				parameter.getParameterizedType ());

		}

	}

	// data

	public static
	List <Class <?>> standardImportClasses =
		ImmutableList.of (

		java.util.function.Supplier.class,

		java.util.ArrayList.class,
		java.util.Date.class,
		java.util.LinkedHashMap.class,
		java.util.LinkedHashSet.class,
		java.util.List.class,
		java.util.Map.class,
		java.util.Set.class,

		javax.annotation.PostConstruct.class,
		javax.inject.Named.class,
		javax.inject.Provider.class,

		wbs.framework.component.annotations.PrototypeDependency.class,
		wbs.framework.component.annotations.SingletonDependency.class,
		wbs.framework.component.manager.ComponentManager.class,

		wbs.framework.entity.helper.EntityHelper.class,
		wbs.framework.entity.model.Model.class,
		wbs.framework.entity.record.GlobalId.class,
		wbs.framework.entity.record.Record.class,

		wbs.framework.database.Database.class,
		wbs.framework.database.Transaction.class,

		wbs.framework.object.ObjectDatabaseHelper.class,
		wbs.framework.object.ObjectHelperImplementation.class,
		wbs.framework.object.ObjectHooks.class,
		wbs.framework.object.ObjectManager.class,
		wbs.framework.object.ObjectModel.class,
		wbs.framework.object.ObjectModelImplementation.class,
		wbs.framework.object.ObjectTypeEntry.class,
		wbs.framework.object.ObjectTypeRegistry.class,

		wbs.framework.utils.etc.OptionalUtils.class,

		com.google.common.base.Optional.class,

		org.joda.time.Instant.class,
		org.joda.time.LocalDate.class,
		org.joda.time.ReadableInstant.class

	);

	public static
	Set <Class <?>> standardImportClassesSet =
		ImmutableSet.copyOf (
			standardImportClasses);

	public static
	List <String> componentNames =
		ImmutableList.of (
			"children",
			"code",
			"find",
			"id",
			"index",
			"model",
			"property",
			"update");

	public static
	Map <String, Class <?>> componentClassesByName =
		ImmutableMap.copyOf (
			componentNames.stream ()

		.collect (
			Collectors.toMap (

			componentName ->
				componentName,

			componentName ->
				classForNameRequired (
					"wbs.framework.object",
					stringFormat (
						"ObjectHelper%sMethods",
						capitalise (
							componentName))))

		)

	);

}
