package wbs.framework.entity.meta;

import static wbs.framework.utils.etc.Misc.camelToHyphen;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.data.tools.DataFromXml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@Accessors (fluent = true)
@Log4j
@SingletonComponent ("modelMetaLoader")
public
class ModelMetaLoader {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// collection dependencies

	@Inject
	@ModelMetaData
	Map<Class<?>,Provider<Object>> modelMetaDataProviders;

	// properties

	@Getter
	Map<String,ModelMetaSpec> modelSpecs;

	// state

	DataFromXml dataFromXml;

	// lifecycle

	@PostConstruct
	public
	void setup () {

		createDataFromXml ();
		loadSpecs ();

	}

	// implementation

	private
	void createDataFromXml () {

		dataFromXml =
			new DataFromXml ();

		for (
			Map.Entry<Class<?>,Provider<Object>> modelMetaDataEntry
				: modelMetaDataProviders.entrySet ()
		) {

			Class<?> modelMetaDataClass =
				modelMetaDataEntry.getKey ();

			Provider<?> modelMetaDataProvider =
				modelMetaDataEntry.getValue ();

			dataFromXml.registerBuilder (
				modelMetaDataClass,
				modelMetaDataProvider);

		}

	}

	private
	void loadSpecs () {

		ImmutableMap.Builder<String,ModelMetaSpec> builder =
			ImmutableMap.<String,ModelMetaSpec>builder ();

		int errorCount = 0;

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

				errorCount +=
					loadModelMeta (
						builder,
						plugin,
						model);

			}

		}

		if (errorCount > 0) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					errorCount));

		}

		modelSpecs = builder.build ();

	}

	private
	int loadModelMeta (
			ImmutableMap.Builder<String,ModelMetaSpec> builder,
			PluginSpec plugin,
			PluginModelSpec model) {

		String resourceName =
			stringFormat (
				"/%s/model/%s-model.xml",
				plugin.packageName ().replace ('.', '/'),
				camelToHyphen (
					model.name ()));

		InputStream inputStream =
			getClass ().getClassLoader ().getResourceAsStream (
				resourceName);

		if (inputStream == null) {

			log.error (
				stringFormat (
					"Model meta not found for %s.%s: %s",
					plugin.name (),
					model.name (),
					resourceName));

			return 1;

		}

		ModelMetaSpec spec;

		try {

			spec =
				(ModelMetaSpec)
				dataFromXml.readInputStream (
					inputStream,
					resourceName,
					ImmutableList.<Object>of (
						model));

		} catch (Exception exception) {

			log.error (
				stringFormat (
					"Error reading model meta for %s.%s: %s",
					plugin.name (),
					model.name (),
					resourceName),
				exception);

			return 1;

		}

		if (
			notEqual (
				spec.name (),
				model.name ())
		) {

			log.error (
				stringFormat (
					"Model meta name %s should be %s in %s",
					spec.name (),
					model.name (),
					resourceName));

			return 1;

		}

		builder.put (
			model.name (),
			spec);

		return 0;

	}

}
