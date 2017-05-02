package wbs.framework.entity.helper;

import static wbs.utils.io.FileUtils.deleteDirectory;
import static wbs.utils.io.FileUtils.forceMkdir;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.data.tools.DataToXml;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@SingletonComponent ("entityHelper")
public
class EntityHelperImplementation
	implements EntityHelper {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	// prototype dependencies

	@PrototypeDependency
	Provider <ModelBuilder <?>> modelBuilder;

	// properties

	@Getter
	List <String> entityClassNames;

	@Getter
	List <Class <?>> entityClasses;

	@Getter
	List <Model <?>> models;

	@Getter
	Map <Class <?>, Model <?>> modelsByClass;

	@Getter
	Map <String, Model <?>> modelsByName;

	// life cycle

	@NormalLifecycleSetup
	public
	void init (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"init");

		) {

			initEntityClassNames (
				taskLogger);

			initEntityClasses (
				taskLogger);

			initModels (
				taskLogger);

		}

	}

	void initEntityClassNames (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initEntityClassNames");

		) {

			ImmutableList.Builder <String> entityClassNamesBuilder =
				ImmutableList.builder ();

			for (
				ModelMetaSpec modelMeta
					: modelMetaLoader.modelMetas ().values ()
			) {

				if (! modelMeta.type ().record ()) {
					continue;
				}

				PluginSpec plugin =
					modelMeta.plugin ();

				entityClassNamesBuilder.add (
					stringFormat (
						"%s.model.%sRec",
						plugin.packageName (),
						capitalise (
							modelMeta.name ())));

			}

			entityClassNames =
				entityClassNamesBuilder.build ();

		}

	}

	void initEntityClasses (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initEntityClasses");

		) {

			ImmutableList.Builder <Class <?>> entityClassesBuilder =
				ImmutableList.builder ();

			for (
				String entityClassName
					: entityClassNames
			) {

				try {

					Class <?> entityClass =
						Class.forName (
							entityClassName);

					entityClassesBuilder.add (
						entityClass);

				} catch (ClassNotFoundException exception) {

					taskLogger.errorFormat (
						"No such class %s",
						entityClassName);

				}

			}

			taskLogger.makeException ();

			entityClasses =
				entityClassesBuilder.build ();

		}

	}

	void initModels (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initModels");

		) {

			try {

				deleteDirectory (
					"work/model");

				forceMkdir (
					"work/model");

			} catch (RuntimeException exception) {

				taskLogger.errorFormat (
					"Error deleting contents of work/model: %s",
					exception.getMessage ());

			}

			ImmutableList.Builder <Model <?>> modelsBuilder =
				ImmutableList.builder ();

			ImmutableMap.Builder <Class <?>, Model <?>> modelsByClassBuilder =
				ImmutableMap.builder ();

			ImmutableMap.Builder <String, Model <?>> modelsByNameBuilder =
				ImmutableMap.builder ();

			int errors = 0;

			for (
				ModelMetaSpec modelMeta
					: modelMetaLoader.modelMetas ().values ()
			) {

				if (! modelMeta.type ().record ()) {
					continue;
				}

				Model <?> model =
					modelBuilder.get ()

					.modelMeta (
						modelMeta)

					.build (
						taskLogger);

				if (model == null) {

					errors ++;

					continue;

				}

				modelsBuilder.add (
					model);

				modelsByClassBuilder.put (
					model.objectClass (),
					model);

				modelsByNameBuilder.put (
					model.objectName (),
					model);

				String outputFilename =
					stringFormat (
						"work/model/%s.xml",
						camelToHyphen (
							model.objectName ()));

				try {

					new DataToXml ().writeToFile (
						outputFilename,
						model);

				} catch (Exception exception) {

					taskLogger.warningFormat (
						"Error writing %s",
						outputFilename);

				}

			}

			if (errors > 0)
				throw new RuntimeException ();

			models =
				modelsBuilder.build ();

			modelsByClass =
				modelsByClassBuilder.build ();

			modelsByName =
				modelsByNameBuilder.build ();

		}

	}

}
