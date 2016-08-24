package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.CollectionUtils.collectionSize;
import static wbs.framework.utils.etc.CollectionUtils.listSlice;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.joinWithFullStop;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringSplitFullStop;
import static wbs.framework.utils.etc.TypeUtils.classForName;
import static wbs.framework.utils.etc.TypeUtils.classForNameRequired;
import static wbs.framework.utils.etc.TypeUtils.classPackageName;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.ImmutableList;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.entity.model.Model;
import wbs.framework.utils.AtomicFileWriter;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.RuntimeIoException;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperGenerator")
public
class ObjectHelperGenerator {

	// properties

	@Getter @Setter
	Model model;

	// state

	String packageName;
	String recordClassName;
	String objectHelperInterfaceName;
	String objectHelperImplementationName;

	boolean hasDao;
	String daoMethodsInterfaceName;
	String daoComponentName;

	boolean hasHooks;
	String hooksImplementationName;

	Class <?> objectHelperInterface;

	FormatWriter javaWriter;

	// implementation

	public
	void generateHelper () {

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

		daoMethodsInterfaceName =
			stringFormat (
				"%sDaoMethods",
				capitalise (
					model.objectName ()));

		daoComponentName =
			stringFormat (
				"%sDao",
				capitalise (
					model.objectName ()));

		hasDao =
			optionalIsPresent (
				classForName (
					packageName + ".hibernate",
					daoMethodsInterfaceName));

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
			"package %s.logic;\n\n",
			packageName);

		writeStandardImports ();

		writeClass ();

	}

	private
	void setJavaWriter (
			@NonNull FormatWriter javaWriter) {

		this.javaWriter =
			javaWriter;

	}

	private
	void writeStandardImports () {

		List <Class <?>> standardImportClasses =
			ImmutableList.of (

			java.util.ArrayList.class,
			java.util.Date.class,
			java.util.LinkedHashMap.class,
			java.util.LinkedHashSet.class,
			java.util.List.class,
			java.util.Map.class,
			java.util.Set.class,

			javax.annotation.PostConstruct.class,
			javax.inject.Inject.class,
			javax.inject.Provider.class,

			wbs.framework.entity.model.Model.class,

			wbs.framework.entity.record.UnsavedRecordDetector.class,
			wbs.framework.entity.record.CommonRecord.class,
			wbs.framework.entity.record.EphemeralRecord.class,
			wbs.framework.entity.record.EventRecord.class,
			wbs.framework.entity.record.MajorRecord.class,
			wbs.framework.entity.record.MinorRecord.class,
			wbs.framework.entity.record.Record.class,
			wbs.framework.entity.record.RecordComponent.class,
			wbs.framework.entity.record.RootRecord.class,
			wbs.framework.entity.record.TypeRecord.class,

			wbs.framework.object.ObjectDatabaseHelper.class,
			wbs.framework.object.ObjectModel.class,
			wbs.framework.object.ObjectModelImplementation.class,
			wbs.framework.object.ObjectTypeEntry.class,
			wbs.framework.object.ObjectTypeRegistry.class,

			org.joda.time.Instant.class,
			org.joda.time.LocalDate.class,
			org.joda.time.ReadableInstant.class

		);

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

		javaWriter.writeFormat (
			"\n");

	}

	void writeClass () {

		javaWriter.writeFormat (
			"public abstract\n");

		javaWriter.writeFormat (
			"class %s\n",
			objectHelperImplementationName);

		javaWriter.writeFormat (
			"\timplements %s.model.%s {\n",
			packageName,
			objectHelperInterfaceName);

		javaWriter.writeFormat (
			"\n");

		writeDependencies ();
		writePrototypeDependencies ();

		writeState ();

		writeLifecycle ();

		javaWriter.writeFormat (
			"}\n");

	}

	void writeDependencies () {

		javaWriter.writeFormat (
			"\t// dependencies\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t@Inject\n");

		javaWriter.writeFormat (
			"\tObjectTypeRegistry objectTypeRegistry;\n");

		javaWriter.writeFormat (
			"\n");

	}	

	void writePrototypeDependencies () {

		javaWriter.writeFormat (
			"\t// prototype dependencies\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t@Inject\n");

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
				"\t@Inject\n");

			javaWriter.writeFormat (
				"\tProvider <%s>\n",
				stringFormat (
					"ObjectHelper%sImplementation",
					capitalise (
						componentName)));

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
			"\tModel model;\n");

		javaWriter.writeFormat (
			"\tModel parentModel;\n");

		javaWriter.writeFormat (
			"\tObjectModel objectModel;\n");

		javaWriter.writeFormat (
			"\tObjectDatabaseHelper databaseHelper;\n");

		if (hasDao) {

			javaWriter.writeFormat (
				"\tClass <?> daoInterface;\n");
	
			javaWriter.writeFormat (
				"\tObject daoImplementation;\n");

		}

		if (hasHooks) {
	
			javaWriter.writeFormat (
				"\tObject hooksImplementation;\n");
	
		}

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\tObjectTypeEntry objectType;\n");

		javaWriter.writeFormat (
			"\tObjectTypeEntry parentType;\n");

		javaWriter.writeFormat (
			"\n");

		for (
			String componentName
				: componentNames
		) {

			javaWriter.writeFormat (
				"\t%s %s;\n",
				stringFormat (
					"ObjectHelper%sImplementation",
					capitalise (
						componentName)),
				stringFormat (
					"%sImplementation",
					componentName));

		}

		javaWriter.writeFormat (
			"\n");

	}

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

		javaWriter.writeFormat (
			"\t\tObjectModel objectModel =\n");

		javaWriter.writeFormat (
			"\t\t\tnew ObjectModelImplementation ()\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\t\t.model (\n");

		javaWriter.writeFormat (
			"\t\t\t\tmodel)\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\t\t.objectTypeId (\n");

		javaWriter.writeFormat (
			"\t\t\t\tobjectType.getId ())\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\t\t.objectTypeCode (\n");

		javaWriter.writeFormat (
			"\t\t\t\tobjectType.getCode ())\n");

		javaWriter.writeFormat (
			"\n");

		if (model.parentTypeIsFixed ()) {

			javaWriter.writeFormat (
				"\t\t\t.parentTypeId (\n");

			javaWriter.writeFormat (
				"\t\t\t\tparentType.getId ())\n");

			javaWriter.writeFormat (
				"\n");
	
			javaWriter.writeFormat (
				"\t\t\t.parentClass (\n");
	
			javaWriter.writeFormat (
				"\t\t\t\tparentModel.objectClass ())\n");

			javaWriter.writeFormat (
				"\n");

		}

		if (hasDao) {

			javaWriter.writeFormat (
				"\n");
	
			javaWriter.writeFormat (
				"\t\t\tdaoImplementation (\n");

			javaWriter.writeFormat (
				"\t\t\t\tdaoImplementation)\n");
	
			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\tdaoInterface (\n");
	
			javaWriter.writeFormat (
				"\t\t\t\tdaoInterface\n");
	
			javaWriter.writeFormat (
				"\n");

		}

		if (hasHooks) {

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t.hooks (\n");

			javaWriter.writeFormat (
				"\t\t\t\thooksImplementation)\n");

			javaWriter.writeFormat (
				"\n");

		}

		javaWriter.writeFormat (
			"\t\t;\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\tdatabaseHelper =\n");

		javaWriter.writeFormat (
			"\t\t\tobjectDatabaseHelperProvider.get ()\n");

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"\t\t\t.model (\n");

		javaWriter.writeFormat (
			"\t\t\t\tobjectModel);\n");

		javaWriter.writeFormat (
			"\n");

		for (
			String componentName
				: componentNames
		) {

			javaWriter.writeFormat (
				"\t\t%sImplementation =\n",
				componentName);

			javaWriter.writeFormat (
				"\t\t\t%s.get ()\n",
				stringFormat (
					"objectHelper%sImplementationProvider",
					capitalise (
						componentName)));

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t.objectHelper (\n");

			javaWriter.writeFormat (
				"\t\t\t\tthis)\n");

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t.objectDatabaseHelper (\n");

			javaWriter.writeFormat (
				"\t\t\t\tdatabaseHelper)\n");

			javaWriter.writeFormat (
				"\n");

			javaWriter.writeFormat (
				"\t\t\t.model (\n");

			javaWriter.writeFormat (
				"\t\t\t\tobjectModel);\n");

			javaWriter.writeFormat (
				"\n");

		}	

		javaWriter.writeFormat (
			"\t}\n");

		javaWriter.writeFormat (
			"\n");

	}

}
