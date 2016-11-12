package wbs.framework.entity.generate;

import static wbs.utils.io.FileUtils.directoryCreateWithParents;
import static wbs.utils.io.FileUtils.fileExistsFormat;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.codegen.JavaClassUnitWriter;
import wbs.framework.codegen.JavaInterfaceWriter;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.scaffold.PluginModelSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.AtomicFileWriter;

@Accessors (fluent = true)
@PrototypeComponent ("modelInterfacesGenerator")
public
class ModelInterfacesGenerator {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <JavaClassUnitWriter> javaClassUnitWriterProvider;

	@PrototypeDependency
	Provider <JavaInterfaceWriter> javaInterfaceWriterProvider;

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
	void generateInterfaces (
			@NonNull TaskLogger taskLogger) {

		setup ();
		findRelated ();

		generateDaoInterface (
			taskLogger);

		generateObjectHelperInterface (
			taskLogger);

		generateConsoleHelperInterface (
			taskLogger);

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
	void generateObjectHelperInterface (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"generateObjectHelperInterface");

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

		try (

			AtomicFileWriter formatWriter =
				new AtomicFileWriter (
					filename);

		) {

			JavaClassUnitWriter classUnitWriter =
				javaClassUnitWriterProvider.get ()

				.formatWriter (
					formatWriter)

				.packageNameFormat (
					"%s.model",
					plugin.packageName ());

			JavaInterfaceWriter objectHelperWriter =
				javaInterfaceWriterProvider.get ()

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

			if (taskLogger.errors ()) {
				return;
			}

			classUnitWriter.write (
				taskLogger);

			if (taskLogger.errors ()) {
				return;
			}

			formatWriter.commit ();

		}

	}

	private
	void generateDaoInterface (
			@NonNull TaskLogger taskLogger) {

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

		try (

			AtomicFileWriter formatWriter =
				new AtomicFileWriter (
					filename);

		) {

			JavaClassUnitWriter classUnitWriter =
				javaClassUnitWriterProvider.get ()

				.formatWriter (
					formatWriter)

				.packageNameFormat (
					"%s.model",
					plugin.packageName ());

			JavaInterfaceWriter daoWriter =
				javaInterfaceWriterProvider.get ()

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

			if (taskLogger.errors ()) {
				return;
			}

			classUnitWriter.write (
				taskLogger);

			if (taskLogger.errors ()) {
				return;
			}

			formatWriter.commit ();

		}

	}

	private
	void generateConsoleHelperInterface (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"generateConsoleHelperInterface");

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
		AtomicFileWriter formatWriter =
			new AtomicFileWriter (
				filename);

		JavaClassUnitWriter classUnitWriter =
			javaClassUnitWriterProvider.get ()

			.formatWriter (
				formatWriter)

			.packageNameFormat (
				"%s.console",
				plugin.packageName ());

		JavaInterfaceWriter consoleHelperWriter =
			javaInterfaceWriterProvider.get ()

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
							"wbs.console.helper.core.ConsoleHelper"),
						imports.registerFormat (
							"%s.model.%s",
							plugin.packageName (),
							recordName)));

		classUnitWriter.addBlock (
			consoleHelperWriter);

		if (taskLogger.errors ()) {
			return;
		}

		classUnitWriter.write (
			taskLogger);

		if (taskLogger.errors ()) {
			return;
		}

		formatWriter.commit ();

	}

}
