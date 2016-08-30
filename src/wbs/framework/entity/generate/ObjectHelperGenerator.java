package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.ArrayUtils.arrayIsEmpty;
import static wbs.framework.utils.etc.ArrayUtils.arrayIsNotEmpty;
import static wbs.framework.utils.etc.ArrayUtils.arrayMap;
import static wbs.framework.utils.etc.CollectionUtils.collectionIsEmpty;
import static wbs.framework.utils.etc.CollectionUtils.listLastElementRequired;
import static wbs.framework.utils.etc.CollectionUtils.listSlice;
import static wbs.framework.utils.etc.CollectionUtils.listSliceAllButLastItemRequired;
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
import static wbs.framework.utils.etc.StringUtils.uncapitalise;
import static wbs.framework.utils.etc.TypeUtils.classForName;
import static wbs.framework.utils.etc.TypeUtils.classForNameRequired;
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

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.codegen.JavaAnnotationWriter;
import wbs.framework.codegen.JavaClassUnitWriter;
import wbs.framework.codegen.JavaClassWriter;
import wbs.framework.codegen.JavaImportRegistry;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelMethods;
import wbs.framework.object.ObjectDatabaseHelper;
import wbs.framework.object.ObjectHelperImplementation;
import wbs.framework.object.ObjectHooks;
import wbs.framework.object.ObjectManager;
import wbs.framework.object.ObjectModel;
import wbs.framework.object.ObjectModelImplementation;
import wbs.framework.object.ObjectModelMethods;
import wbs.framework.object.ObjectTypeEntry;
import wbs.framework.object.ObjectTypeRegistry;
import wbs.framework.utils.etc.OptionalUtils;
import wbs.framework.utils.etc.RuntimeIoException;
import wbs.framework.utils.formatwriter.AtomicFileWriter;
import wbs.framework.utils.formatwriter.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperGenerator")
public
class ObjectHelperGenerator {

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

		packageName =
			joinWithFullStop (
				listSliceAllButLastItemRequired (
					stringSplitFullStop (
						classPackageName (
							model.objectClass ()))));

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
		FormatWriter formatWriter =
			new AtomicFileWriter (
				filename);

		JavaClassUnitWriter classUnitWriter =
			new JavaClassUnitWriter ()

			.formatWriter (
				formatWriter)

			.packageNameFormat (
				"%s.logic",
				packageName);

		JavaClassWriter classWriter =
			new JavaClassWriter ()

			.className (
				objectHelperImplementationName)

			.addClassAnnotation (
				new JavaAnnotationWriter ()

				.name (
					"java.lang.SuppressWarnings")

				.addAttributeFormat (
					"value",
					"{ \"rawtypes\", \"unchecked\" }"))

			.addClassModifier (
				"public")

			.addImplements (
				ObjectHelperImplementation.class)

			.addImplements (
				packageName + ".model",
				objectHelperInterfaceName);

		classUnitWriter.addBlock (
			classWriter);
	
		classWriter.addBlock (
			this::writeDependencies);
		
		classWriter.addBlock (
			this::writePrototypeDependencies);
		
		classWriter.addBlock (
			this::writeState);
		
		classWriter.addBlock (
			this::writeLifecycle);
		
		classWriter.addBlock (
			this::writeImplementation);
		
		classWriter.addBlock (
			this::writeDelegations);
		
		classUnitWriter.write ();

	}

	void writeDependencies (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"// dependencies");

		formatWriter.writeNewline ();

		// application context

		formatWriter.writeLineFormat (
			"@%s",
			imports.register (
				SingletonDependency.class));

		formatWriter.writeLineFormat (
			"%s applicationContext;",
			imports.register (
				ApplicationContext.class));

		formatWriter.writeNewline ();

		// database

		formatWriter.writeLineFormat (
			"@%s",
			imports.register (
				SingletonDependency.class));

		formatWriter.writeLineFormat (
			"%s database;",
			imports.register (
				Database.class));

		formatWriter.writeNewline ();

		// entity helper

		formatWriter.writeLineFormat (
			"@%s",
			imports.register (
				SingletonDependency.class));

		formatWriter.writeLineFormat (
			"%s entityHelper;",
			imports.register (
				EntityHelper.class));

		formatWriter.writeNewline ();

		// object type registry

		formatWriter.writeLineFormat (
			"@%s",
			imports.register (
				SingletonDependency.class));

		formatWriter.writeLineFormat (
			"%s objectTypeRegistry;",
			imports.register (
				ObjectTypeRegistry.class));

		formatWriter.writeNewline ();

		// dao

		if (hasDao) {

			formatWriter.writeLineFormat (
				"@%s",
				imports.register (
					SingletonDependency.class));
	
			formatWriter.writeLineFormat (
				"@%s",
				imports.register (
					Named.class));
	
			formatWriter.writeLineFormat (
				"%s %s;",
				imports.register (
					daoMethodsInterface),
				daoComponentName);
	
			formatWriter.writeNewline ();

		}

		if (hasExtra) {

			formatWriter.writeLineFormat (
				"@%s",
				imports.register (
					SingletonDependency.class));
	
			formatWriter.writeLineFormat (
				"@%s",
				imports.register (
					Named.class));
	
			formatWriter.writeLineFormat (
				"%s %s;",
				imports.register (
					extraMethodsInterface),
				extraComponentName);
	
			formatWriter.writeNewline ();

		}

	}	

	void writePrototypeDependencies (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"// prototype dependencies");

		formatWriter.writeNewline ();

		// object database helper

		formatWriter.writeLineFormat (
			"@%s",
			imports.register (
				PrototypeDependency.class));

		formatWriter.writeLineFormat (
			"%s <%s>",
			imports.register (
				Provider.class),
			imports.register (
				ObjectDatabaseHelper.class));

		formatWriter.writeLineFormat (
			"objectDatabaseHelperProvider;");

		formatWriter.writeNewline ();

		// components

		for (
			String componentName
				: componentNames
		) {

			formatWriter.writeLineFormat (
				"@%s",
				imports.register (
					PrototypeDependency.class));

			formatWriter.writeLineFormat (
				"%s <%s>",
				imports.register (
					Provider.class),
				stringFormat (
					"%s <%s>",
					imports.registerFormat (
						"wbs.framework.object.ObjectHelper%sImplementation",
						capitalise (
							componentName)),
					imports.register (
						model.objectClass ())));

			formatWriter.writeLineFormat (
				"%s;",
				stringFormat (
					"objectHelper%sImplementationProvider",
					capitalise (
						componentName)));

			formatWriter.writeNewline ();

		}

	}

	void writeState (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"// state");

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"%s model;",
			imports.register (
				Model.class));

		formatWriter.writeLineFormat (
			"%s parentModel;",
			imports.register (
				Model.class));

		formatWriter.writeLineFormat (
			"%s objectModel;",
			imports.register (
				ObjectModel.class));

		formatWriter.writeLineFormat (
			"%s databaseHelper;",
			imports.register (
				ObjectDatabaseHelper.class));

		formatWriter.writeLineFormat (
			"%s hooksImplementation;",
			imports.register (
				ObjectHooks.class));

		formatWriter.writeNewline ();

		for (
			String componentName
				: componentNames
		) {

			formatWriter.writeLineFormat (
				"%s <%s> %s;",
				imports.registerFormat (
					"wbs.framework.object.ObjectHelper%sImplementation",
					capitalise (
						componentName)),
				imports.register (
					model.objectClass ()),
				stringFormat (
					"%sImplementation",
					componentName));

		}

		formatWriter.writeNewline ();

	}

	void writeLifecycle (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"// lifecycle");

		formatWriter.writeNewline ();

		// open method

		formatWriter.writeLineFormat (
			"@%s",
			imports.register (
				PostConstruct.class));

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"void setup () {");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

		// open transaction

		formatWriter.writeLineFormat (
			"try (");

		formatWriter.writeLineFormat (
			"\t%s transaction =",
			imports.register (
				Transaction.class));

		formatWriter.writeLineFormat (
			"\t\tdatabase.beginReadOnly (");

		formatWriter.writeLineFormat (
			"\t\t\t\"setup\",");

		formatWriter.writeLineFormat (
			"\t\t\tthis);");

		formatWriter.writeLineFormat (
			") {");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

		// model

		formatWriter.writeLineFormat (
			"model =");

		formatWriter.writeLineFormat (
			"\tentityHelper.modelsByName ().get (");

		formatWriter.writeLineFormat (
			"\t\t\"%s\");",
			model.objectName ());

		formatWriter.writeNewline ();

		// object type

		formatWriter.writeLineFormat (
			"%s objectType =",
			imports.register (
				ObjectTypeEntry.class));

		formatWriter.writeLineFormat (
			"\tobjectTypeRegistry.findByCode (");

		formatWriter.writeLineFormat (
			"\t\t\"%s\");",
			model.objectTypeCode ());

		formatWriter.writeNewline ();

		if (
			isNotNull (
				parentModel)
		) {

			// parent model

			formatWriter.writeLineFormat (
				"parentModel =");

			formatWriter.writeLineFormat (
				"\tentityHelper.modelsByName ().get (");

			formatWriter.writeLineFormat (
				"\t\t\"%s\");",
				parentModel.objectName ());

			formatWriter.writeNewline ();

			// parent type

			formatWriter.writeLineFormat (
				"ObjectTypeEntry parentType =");
	
			formatWriter.writeLineFormat (
				"\tobjectTypeRegistry.findByCode (");
	
			formatWriter.writeLineFormat (
				"\t\t\"%s\");",
				parentModel.objectTypeCode ());
	
			formatWriter.writeNewline ();

		}

		// hooks

		formatWriter.writeLineFormat (
			"hooksImplementation =");

		formatWriter.writeLineFormat (
			"\t%s.optionalOrNull (",
			imports.register (
				OptionalUtils.class));

		formatWriter.writeLineFormat (
			"\t\tapplicationContext.getComponent (");

		formatWriter.writeLineFormat (
			"\t\t\t\"%s\",",
			hooksComponentName);

		formatWriter.writeLineFormat (
			"\t\t\t%s.class));",
			imports.register (
				ObjectHooks.class));

		formatWriter.writeNewline ();

		// object model

		formatWriter.writeLineFormat (
			"%s objectModel =",
			imports.register (
				ObjectModel.class));

		formatWriter.writeLineFormat (
			"\tnew %s ()",
			imports.register (
				ObjectModelImplementation.class));

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"\t.model (");

		formatWriter.writeLineFormat (
			"\t\tmodel)");

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"\t.objectTypeId (");

		formatWriter.writeLineFormat (
			"\t\tobjectType.getId ())");

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"\t.objectTypeCode (");

		formatWriter.writeLineFormat (
			"\t\tobjectType.getCode ())");

		formatWriter.writeNewline ();

		if (
			isNotNull (
				parentModel)
		) {

			formatWriter.writeLineFormat (
				"\t.parentTypeId (");

			formatWriter.writeLineFormat (
				"\t\tparentType.getId ())");

			formatWriter.writeNewline ();
	
			formatWriter.writeLineFormat (
				"\t.parentClass (");
	
			formatWriter.writeLineFormat (
				"\t\tparentModel.objectClass ())");

			formatWriter.writeNewline ();

		}

		if (hasDao) {

			formatWriter.writeLineFormat (
				"\t.daoImplementation (");

			formatWriter.writeLineFormat (
				"\t\t%s)",
				daoComponentName);
	
			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"\t.daoInterface (");
	
			formatWriter.writeLineFormat (
				"\t\t%s.class)",
				daoMethodsInterfaceName);
	
			formatWriter.writeNewline ();

		}

		formatWriter.writeLineFormat (
			"\t.hooks (");

		formatWriter.writeLineFormat (
			"\t\thooksImplementation)");

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			";");

		formatWriter.writeNewline ();

		// database helper

		formatWriter.writeLineFormat (
			"databaseHelper =");

		formatWriter.writeLineFormat (
			"\tobjectDatabaseHelperProvider.get ()");

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"\t.model (");

		formatWriter.writeLineFormat (
			"\t\tobjectModel);");

		formatWriter.writeNewline ();

		// components

		for (
			String componentName
				: componentNames
		) {

			formatWriter.writeLineFormat (
				"%sImplementation =",
				componentName);

			formatWriter.writeLineFormat (
				"\t%s.get ()",
				stringFormat (
					"objectHelper%sImplementationProvider",
					capitalise (
						componentName)));

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"\t.objectHelper (");

			formatWriter.writeLineFormat (
				"\t\tthis)");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"\t.objectDatabaseHelper (");

			formatWriter.writeLineFormat (
				"\t\tdatabaseHelper)");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"\t.model (");

			formatWriter.writeLineFormat (
				"\t\tobjectModel);");

			formatWriter.writeNewline ();

		}

		// close transaction

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

		// close method

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

	void writeImplementation (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"// implementation");

		formatWriter.writeNewline ();

		// open method

		formatWriter.writeLineFormat (
			"@%s",
			imports.register (
				Override.class));

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"void objectManager (");

		formatWriter.writeLineFormat (
			"\t\t%s objectManager) {",
			imports.register (
				ObjectManager.class));

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

		// initialise components

		for (
			String componentName
				: componentNames
		) {

			formatWriter.writeLineFormat (
				"%sImplementation.objectManager (",
				componentName);

			formatWriter.writeLineFormat (
				"\tobjectManager);");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"%sImplementation.setup ();",
				componentName);

			formatWriter.writeNewline ();

		}

		// close method

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

	void writeDelegations (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		Set <Pair <String, List <Class <?>>>> delegatedMethods =
			new HashSet<> ();

		if (hasExtra) {

			writeDelegate (
				delegatedMethods,
				imports,
				formatWriter,
				extraMethodsInterface,
				extraComponentName);

		}

		if (hasDao) {

			writeDelegate (
				delegatedMethods,
				imports,
				formatWriter,
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
				delegatedMethods,
				imports,
				formatWriter,
				componentClass,
				stringFormat (
					"%sImplementation",
					componentName));

		}

		writeDelegate (
			delegatedMethods,
			imports,
			formatWriter,
			ObjectModelMethods.class,
			"objectModel");

		writeDelegate (
			delegatedMethods,
			imports,
			formatWriter,
			ModelMethods.class,
			"model");

	}

	void writeDelegate (
			@NonNull Set <Pair <String, List <Class <?>>>> delegatedMethods,
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter,
			@NonNull Class <?> delegateInterface,
			@NonNull String delegateName) {

		formatWriter.writeLineFormat (
			"// delegate %s",
			delegateInterface.getSimpleName ());

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

			String returnTypeName =
				methodReturnSourceName (
					imports,
					method);

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
						delegateName,
						method.getName ());

				} else {

					formatWriter.writeLineFormat (
						"\t%s.%s (",
						delegateName,
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
						"\t\t\t%s);",
						lastParameter.getName ());

				}

			} else {

				if (
					collectionIsEmpty (
						parameters)
				) {

					formatWriter.writeLineFormat (
						"\treturn %s.%s ();",
						delegateName,
						method.getName ());

				} else {

					formatWriter.writeLineFormat (
						"\treturn %s.%s (",
						delegateName,
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

	}

	String methodReturnSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull Method method) {

		return stringReplaceAllRegex (
			"\\bRecordType\\b",
			model.objectClass ().getSimpleName (),
			typeSourceName (
				imports,
				method.getGenericReturnType ()));

	}

	public
	String typeSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull Type type) {

		if (
			isInstanceOf (
				Class.class,
				type)
		) {

			return classSourceName (
				imports,
				(Class <?>)
				type);

		} else if (
			isInstanceOf (
				ParameterizedType.class,
				type)
		) {

			return parameterizedTypeSourceName (
				imports,
				(ParameterizedType)
				type);

		} else if (
			isInstanceOf (
				TypeVariable.class,
				type)
		) {

			return typeVariableSourceName (
				imports,
				(TypeVariable <?>)
				type);

		} else if (
			isInstanceOf (
				WildcardType.class,
				type)
		) {

			return wildcardTypeSourceName (
				imports,
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
			@NonNull JavaImportRegistry imports,
			@NonNull Class <?> theClass) {

		if (theClass.isArray ()) {

			return stringFormat (
				"%s[]",
				classSourceName (
					imports,
					theClass.getComponentType ()));

		} else {

			return imports.register (
				theClass);

		}

	}

	public
	String parameterizedTypeSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull ParameterizedType parameterizedType) {

		if (
			arrayIsEmpty (
				parameterizedType.getActualTypeArguments ())
		) {

			return imports.register (
				(Class <?>)
				parameterizedType.getRawType ()); 

		} else {

			return stringFormat (
				"%s <%s>",
				typeSourceName (
					imports,
					parameterizedType.getRawType ()),
				joinWithCommaAndSpace (
					arrayMap (
						typeArgument ->
							typeSourceName (
								imports,
								typeArgument),
						parameterizedType.getActualTypeArguments ())));

		}

	}

	public
	String typeVariableSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull TypeVariable <?> typeVariable) {

		return typeVariable.getName ();

	}

	public
	String typeVariableSourceDeclaration (
			@NonNull JavaImportRegistry imports,
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
						bound ->
							typeSourceName (
								imports,
								bound),
						bounds)));

		}

	}

	public
	String wildcardTypeSourceName (
			@NonNull JavaImportRegistry imports,
			@NonNull WildcardType wildcardType) {

		return wildcardType.toString ();

	}

	public
	String parameterSourceTypeName (
			@NonNull JavaImportRegistry imports,
			@NonNull Parameter parameter) {

		if (parameter.isVarArgs ()) {

			return stringFormat (
				"%s ...",
				typeSourceName (
					imports,
					parameter.getType ().getComponentType ()));

		} else {

			return typeSourceName (
				imports,
				parameter.getParameterizedType ());

		}

	}

	// data

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
