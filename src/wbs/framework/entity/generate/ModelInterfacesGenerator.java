package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.camelToHyphen;
import static wbs.framework.utils.etc.StringUtils.capitalise;

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.io.FileUtils;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.entity.meta.ModelMetaSpec;

@Accessors (fluent = true)
@PrototypeComponent ("modelInterfacesGenerator")
public
class ModelInterfacesGenerator {

	// properties

	@Getter @Setter
	PluginSpec plugin;

	@Getter @Setter
	PluginModelSpec pluginModel;

	@Getter @Setter
	ModelMetaSpec modelMeta;

	// state

	String modelPackageName;
	String consolePackageName;

	String recordName;
	String daoName;
	String daoMethodsName;
	String objectHelperName;
	String objectHelperMethodsName;
	String consoleHelperName;

	String sourceModelDirectoryName;
	String sourceModelFilename;

	String targetModelDirectoryName;
	String targetConsoleDirectoryName;

	String daoFilename;
	String objectHelperFilename;
	String consoleHelperFilename;

	boolean gotObjectHelperMethods;
	boolean gotDaoMethods;

	// implementation

	public
	void generateInterfaces ()
		throws IOException {

		setup ();
		findRelated ();

		generateDao ();
		generateObjectHelper ();
		generateConsoleHelper ();

	}

	private
	void setup ()
		throws IOException {

		// package names

		modelPackageName =
			stringFormat (
				"%s.model",
				plugin.packageName ());

		consolePackageName =
			stringFormat (
				"%s.console",
				plugin.packageName ());

		// class and interface names

		recordName =
			stringFormat (
				"%sRec",
				capitalise (
					modelMeta.name ()));

		daoName =
			stringFormat (
				"%sDao",
				capitalise (
					modelMeta.name ()));

		daoMethodsName =
			stringFormat (
				"%sDaoMethods",
				capitalise (
					modelMeta.name ()));

		objectHelperName =
			stringFormat (
				"%sObjectHelper",
				capitalise (
					modelMeta.name ()));

		objectHelperMethodsName =
			stringFormat (
				"%sObjectHelperMethods",
				capitalise (
					modelMeta.name ()));

		consoleHelperName =
			stringFormat (
				"%sConsoleHelper",
				capitalise (
					modelMeta.name ()));

		// directory and filenames

		sourceModelDirectoryName =
			stringFormat (
				"src/%s",
				modelPackageName.replace ('.', '/'));

		sourceModelFilename =
			stringFormat (
				"%s/%s-model.xml",
				sourceModelDirectoryName,
				camelToHyphen (
					modelMeta.name ()));

		targetModelDirectoryName =
			stringFormat (
				"work/generated/%s",
				modelPackageName.replace ('.', '/'));

		targetConsoleDirectoryName =
			stringFormat (
				"work/generated/%s",
				consolePackageName.replace ('.', '/'));

		FileUtils.forceMkdir (
			new File (
				targetModelDirectoryName));

		FileUtils.forceMkdir (
			new File (
				targetConsoleDirectoryName));

		daoFilename =
			stringFormat (
				"%s/%s.java",
				targetModelDirectoryName,
				daoName);

		objectHelperFilename =
			stringFormat (
				"%s/%s.java",
				targetModelDirectoryName,
				objectHelperName);

		consoleHelperFilename =
			stringFormat (
				"%s/%s.java",
				targetConsoleDirectoryName,
				consoleHelperName);

	}

	private
	void findRelated () {

		String objectHelperMethodsFilename =
			stringFormat (
				"%s/%s.java",
				sourceModelDirectoryName,
				objectHelperMethodsName);

		gotObjectHelperMethods =
			new File (
				objectHelperMethodsFilename
			).exists ();

		String daoMethodsFilename =
			stringFormat (
				"%s/%s.java",
				sourceModelDirectoryName,
				daoMethodsName);

		gotDaoMethods =
			new File (
				daoMethodsFilename
			).exists ();

	}

	private
	void generateObjectHelper ()
		throws IOException {

		if (
			FileUtils.isFileNewer (
				new File (objectHelperFilename),
				new File (sourceModelFilename))
		) {
			return;
		}

		InterfaceWriter objectHelperWriter =
			new InterfaceWriter ()

			.packageName (
				modelPackageName)

			.name (
				objectHelperName);

		if (gotObjectHelperMethods) {

			objectHelperWriter

				.addImport (
					"%s.%s",
					modelPackageName,
					objectHelperMethodsName)

				.addInterface (
					"%s",
					objectHelperMethodsName);

		}

		if (gotDaoMethods) {

			objectHelperWriter

				.addImport (
					"%s.%s",
					modelPackageName,
					daoMethodsName)

				.addInterface (
					"%s",
					daoMethodsName);

		}

		objectHelperWriter

			.addImport (
				"wbs.framework.object.ObjectHelper")

			.addInterface (
				"ObjectHelper<%s>",
				recordName);

		objectHelperWriter.write (
			objectHelperFilename);

	}

	private
	void generateDao ()
		throws IOException {

		if (! gotDaoMethods) {
			return;
		}

		if (
			FileUtils.isFileNewer (
				new File (daoFilename),
				new File (sourceModelFilename))
		) {
			return;
		}

		// write dao

		InterfaceWriter daoWriter =
			new InterfaceWriter ()

			.packageName (
				modelPackageName)

			.name (
				daoName);

		if (gotDaoMethods) {

			daoWriter

				.addImport (
					"%s.%s",
					modelPackageName,
					daoMethodsName)

				.addInterface (
					"%s",
					daoMethodsName);

		}

		daoWriter.write (
			daoFilename);

	}

	private
	void generateConsoleHelper ()
		throws IOException {

		if (
			FileUtils.isFileNewer (
				new File (consoleHelperFilename),
				new File (sourceModelFilename))
		) {
			return;
		}

		InterfaceWriter consoleHelperWriter =
			new InterfaceWriter ()

			.packageName (
				consolePackageName)

			.name (
				consoleHelperName)

			.addImport (
				"%s.%s",
				modelPackageName,
				recordName);

		if (gotObjectHelperMethods) {

			consoleHelperWriter

				.addImport (
					"%s.%s",
					modelPackageName,
					objectHelperMethodsName)

				.addInterface (
					"%s",
					objectHelperMethodsName);

		}

		if (gotDaoMethods) {

			consoleHelperWriter

				.addImport (
					"%s.%s",
					modelPackageName,
					daoMethodsName)

				.addInterface (
					"%s",
					daoMethodsName);

		}

		consoleHelperWriter

			.addImport (
				"wbs.console.helper.ConsoleHelper")

			.addInterface (
				"ConsoleHelper<%s>",
				recordName);

		consoleHelperWriter.write (
			consoleHelperFilename);

	}

}
