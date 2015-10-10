package wbs.framework.entity.helper;

import static wbs.framework.utils.etc.Misc.camelToHyphen;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.io.FileUtils;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.data.tools.DataToXml;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@Accessors (fluent = true)
@Log4j
@SingletonComponent ("entityHelper")
public
class EntityHelperImplementation
	implements EntityHelper {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// prototype dependencies

	@Inject
	Provider<ModelBuilder> modelBuilder;

	// properties

	@Getter
	List<String> entityClassNames;

	@Getter
	List<Class<?>> entityClasses;

	@Getter
	List<Model> models;

	@Getter
	Map<Class<?>,Model> modelsByClass;

	@Getter
	Map<String,Model> modelsByName;

	@PostConstruct
	public
	void init () {

		initEntityClassNames ();
		initEntityClasses ();

		initModels ();

	}

	void initEntityClassNames () {

		ImmutableList.Builder<String> entityClassNamesBuilder =
			ImmutableList.<String>builder ();

		for (
			PluginSpec plugin
				: pluginManager.plugins ()
		) {

			if (plugin.models () == null)
				continue;

			for (
				PluginModelSpec model
					: plugin.models ().models ()
			) {

				entityClassNamesBuilder.add (
					stringFormat (
						"%s.model.%sRec",
						model.plugin ().packageName (),
						capitalise (model.name ())));

			}

		}

		entityClassNames =
			entityClassNamesBuilder.build ();

	}

	void initEntityClasses () {

		ImmutableList.Builder<Class<?>> entityClassesBuilder =
			ImmutableList.<Class<?>>builder ();

		int errors = 0;

		for (String entityClassName : entityClassNames) {

			try {

				Class<?> entityClass =
					Class.forName (entityClassName);

				entityClassesBuilder.add (
					entityClass);

			} catch (ClassNotFoundException exception) {

				log.error (
					stringFormat (
						"No such class %s",
						entityClassName));

				errors ++;

			}

		}

		if (errors > 0) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s classes not found",
					errors));

		}

		entityClasses =
			entityClassesBuilder.build ();

	}

	void initModels () {

		try {

			FileUtils.deleteDirectory (
				new File (
					"work/model"));

			FileUtils.forceMkdir (
				new File (
					"work/model"));

		} catch (IOException exception) {

			log.error (
				"Error deleting contents of work/model",
				exception);

		}

		ImmutableList.Builder<Model> modelsBuilder =
			ImmutableList.<Model>builder ();

		ImmutableMap.Builder<Class<?>,Model> modelsByClassBuilder =
			ImmutableMap.<Class<?>,Model>builder ();

		ImmutableMap.Builder<String,Model> modelsByNameBuilder =
			ImmutableMap.<String,Model>builder ();

		int errors = 0;

		for (Class<?> entityClass
				: entityClasses) {

			Model model =
				modelBuilder.get ()
					.objectClass (entityClass)
					.build ();

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
					camelToHyphen (model.objectName ()));

			try {

				new DataToXml ()
					.object (model)
					.write (outputFilename);

			} catch (IOException exception) {

				log.warn (
					stringFormat (
						"Error writing %s",
						outputFilename));

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
