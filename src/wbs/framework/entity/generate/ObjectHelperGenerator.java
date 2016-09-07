package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.CollectionUtils.listSliceAllButLastItemRequired;
import static wbs.framework.utils.etc.LogicUtils.ifThenElse;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.joinWithFullStop;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringSplitFullStop;
import static wbs.framework.utils.etc.StringUtils.uncapitalise;
import static wbs.framework.utils.etc.TypeUtils.classForName;
import static wbs.framework.utils.etc.TypeUtils.classForNameRequired;
import static wbs.framework.utils.etc.TypeUtils.classPackageName;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.io.FileUtils;

import wbs.framework.codegen.JavaAnnotationWriter;
import wbs.framework.codegen.JavaClassUnitWriter;
import wbs.framework.codegen.JavaClassWriter;
import wbs.framework.codegen.JavaImportRegistry;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
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
	String daoImplementationClassName;
	String daoComponentName;
	Class <?> daoMethodsInterface;

	boolean hasExtra;
	String extraMethodsInterfaceName;
	String extraImplementationClassName;
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

			daoImplementationClassName =
				stringFormat (
					"%s.hibernate.%sDaoHibernate",
					packageName,
					capitalise (
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

			extraImplementationClassName =
				stringFormat (
					"%s.logic.%sObjectHelperMethodsImplementation",
					packageName,
					capitalise (
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

			.addImplementsClass (
				ObjectHelperImplementation.class)

			.addImplementsFormat (
				"%s.model.%s",
				packageName,
				objectHelperInterfaceName)

			.addTypeParameterMapping (
				"RecordType",
				model.objectClass ().getSimpleName ());

		addDependencies (
			classWriter);

		addState (
			classWriter);

		classWriter.addBlock (
			this::writeLifecycle);

		classWriter.addBlock (
			this::writeImplementation);

		addDelegations (
			classWriter);

		classUnitWriter.addBlock (
			classWriter);

		classUnitWriter.write ();

	}

	void addDependencies (
			@NonNull JavaClassWriter classWriter) {

		classWriter.addSingletonDependency (
			ComponentManager.class);

		classWriter.addSingletonDependency (
			Database.class);

		classWriter.addSingletonDependency (
			EntityHelper.class);

		classWriter.addSingletonDependency (
			ObjectTypeRegistry.class);

		if (hasDao) {

			classWriter.addNamedSingletonDependency (
				daoImplementationClassName,
				daoComponentName);

		}

		if (hasExtra) {

			classWriter.addNamedSingletonDependency (
				extraImplementationClassName,
				extraComponentName);

		}

		classWriter.addPrototypeDependency (
			ObjectDatabaseHelper.class);

		// components

		for (
			Class <?> componentClass
				: componentImplementationsByName.values ()
		) {

			classWriter.addPrototypeDependency (
				componentClass);

		}

	}

	void addState (
			@NonNull JavaClassWriter classWriter) {

		classWriter.addState (
			Model.class,
			"model");

		classWriter.addState (
			Model.class,
			"parentModel");

		classWriter.addState (
			ObjectModel.class,
			"objectModel");;

		classWriter.addState (
			ObjectDatabaseHelper.class,
			"databaseHelper");;

		classWriter.addState (
			ObjectHooks.class,
			"hooksImplementation");

		for (
			String componentName
				: componentNames
		) {

			classWriter.addState (
				imports ->
					stringFormat (
						"%s <%s>",
						imports.register (
							stringFormat (
								"wbs.framework.object.ObjectHelper%sImplementation",
								capitalise (
									componentName))),
						imports.register (
							model.objectClass ())),
				stringFormat (
					"%sImplementation",
					componentName));

		}

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
			"\t\tcomponentManager.getComponent (");

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
				imports.register (
					daoMethodsInterface));

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

	void addDelegations (
			@NonNull JavaClassWriter javaWriter) {

		if (hasExtra) {

			javaWriter.addDelegation (
				extraMethodsInterface,
				extraComponentName);

		}

		if (hasDao) {

			javaWriter.addDelegation (
				daoMethodsInterface,
				daoComponentName);

		}

		for (
			Map.Entry <String, Class <?>> componentEntry
				: componentInterfacesByName.entrySet ()
		) {

			String componentName =
				componentEntry.getKey ();

			Class <?> componentClass =
				componentEntry.getValue ();

			javaWriter.addDelegation (
				componentClass,
				stringFormat (
					"%sImplementation",
					componentName));

		}

		javaWriter.addDelegation (
			ObjectModelMethods.class,
			"objectModel");

		javaWriter.addDelegation (
			ModelMethods.class,
			"model");

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
	Map <String, Class <?>> componentInterfacesByName =
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

	public static
	Map <String, Class <?>> componentImplementationsByName =
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
						"ObjectHelper%sImplementation",
						capitalise (
							componentName))))

		)

	);

}
