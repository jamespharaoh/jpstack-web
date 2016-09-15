package wbs.framework.entity.generate;

import static wbs.utils.io.FileUtils.directoryCreateWithParents;
import static wbs.utils.io.FileUtils.fileExistsFormat;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.codegen.JavaClassUnitWriter;
import wbs.framework.codegen.JavaInterfaceWriter;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.scaffold.PluginModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.utils.string.AtomicFileWriter;
import wbs.utils.string.FormatWriter;

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

	String recordName;
	String daoName;
	String daoMethodsName;
	String objectHelperName;
	String objectHelperMethodsName;
	String consoleHelperName;

	boolean gotObjectHelperMethods;
	boolean gotDaoMethods;

	// implementation

	public
	void generateInterfaces () {

		setup ();
		findRelated ();

		generateDaoInterface ();
		generateObjectHelperInterface ();
		generateConsoleHelperInterface ();

	}

	private
	void setup () {

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

	}

	private
	void findRelated () {

		gotObjectHelperMethods =
			fileExistsFormat (
				"src/%s/model/%s.java",
				plugin.packageName ().replace ('.', '/'),
				objectHelperMethodsName);

		gotDaoMethods =
			fileExistsFormat (
				"src/%s/model/%s.java",
				plugin.packageName ().replace ('.', '/'),
				daoMethodsName);

	}

	private
	void generateObjectHelperInterface () {

		// create directory

		String directory =
			stringFormat (
				"work/generated/%s/model",
				plugin.packageName ().replace ('.', '/'));

		directoryCreateWithParents (
			directory);

		// write interface

		String filename =
			stringFormat (
				"%s/%s.java",
				directory,
				objectHelperName);

		@Cleanup
		FormatWriter formatWriter =
			new AtomicFileWriter (
				filename);

		JavaClassUnitWriter classUnitWriter =
			new JavaClassUnitWriter ()

			.formatWriter (
				formatWriter)

			.packageNameFormat (
				"%s.model",
				plugin.packageName ());

		JavaInterfaceWriter objectHelperWriter =
			new JavaInterfaceWriter ()

			.interfaceName (
				objectHelperName)

			.addInterfaceModifier (
				"public");

		if (gotObjectHelperMethods) {

			objectHelperWriter

				.addInterfaceFormat (
					"%s.model.%s",
					plugin.packageName (),
					objectHelperMethodsName);

		}

		if (gotDaoMethods) {

			objectHelperWriter

				.addInterfaceFormat (
					"%s.model.%s",
					plugin.packageName (),
					daoMethodsName);

		}

		objectHelperWriter

			.addInterface (
				imports ->
					stringFormat (
						"%s <%s>",
						imports.register (
							"wbs.framework.object.ObjectHelper"),
						imports.registerFormat (
							"%s.model.%s",
							plugin.packageName (),
							recordName)));

		classUnitWriter.addBlock (
			objectHelperWriter);

		classUnitWriter.write ();

	}

	private
	void generateDaoInterface () {

		if (! gotDaoMethods) {
			return;
		}

		// create directory

		String directory =
			stringFormat (
				"work/generated/%s/model",
				plugin.packageName ().replace ('.', '/'));

		directoryCreateWithParents (
			directory);

		// write interface

		String filename =
			stringFormat (
				"%s/%s.java",
				directory,
				daoName);

		// write interface

		@Cleanup
		FormatWriter formatWriter =
			new AtomicFileWriter (
				filename);

		JavaClassUnitWriter classUnitWriter =
			new JavaClassUnitWriter ()

			.formatWriter (
				formatWriter)

			.packageNameFormat (
				"%s.model",
				plugin.packageName ());

		JavaInterfaceWriter daoWriter =
			new JavaInterfaceWriter ()

			.interfaceName (
				daoName)

			.addInterfaceModifier (
				"public");

		if (gotDaoMethods) {

			daoWriter

				.addInterfaceFormat (
					"%s.model.%s",
					plugin.packageName (),
					daoMethodsName);

		}

		classUnitWriter.addBlock (
			daoWriter);

		classUnitWriter.write ();

	}

	private
	void generateConsoleHelperInterface () {

		// create directory

		String directory =
			stringFormat (
				"work/generated/%s/console",
				plugin.packageName ().replace ('.', '/'));

		directoryCreateWithParents (
			directory);

		// write interface

		String filename =
			stringFormat (
				"%s/%s.java",
				directory,
				consoleHelperName);

		// write interface

		@Cleanup
		FormatWriter formatWriter =
			new AtomicFileWriter (
				filename);

		JavaClassUnitWriter classUnitWriter =
			new JavaClassUnitWriter ()

			.formatWriter (
				formatWriter)

			.packageNameFormat (
				"%s.console",
				plugin.packageName ());

		JavaInterfaceWriter consoleHelperWriter =
			new JavaInterfaceWriter ()

			.interfaceName (
				consoleHelperName)

			.addInterfaceModifier (
				"public");

		if (gotObjectHelperMethods) {

			consoleHelperWriter

				.addInterfaceFormat (
					"%s.model.%s",
					plugin.packageName (),
					objectHelperMethodsName);

		}

		if (gotDaoMethods) {

			consoleHelperWriter

				.addInterfaceFormat (
					"%s.model.%s",
					plugin.packageName (),
					daoMethodsName);

		}

		consoleHelperWriter

			.addInterface (
				imports ->
					stringFormat (
						"%s <%s>",
						imports.register (
							"wbs.console.helper.ConsoleHelper"),
						imports.registerFormat (
							"%s.model.%s",
							plugin.packageName (),
							recordName)));

		classUnitWriter.addBlock (
			consoleHelperWriter);

		classUnitWriter.write ();

	}

}
