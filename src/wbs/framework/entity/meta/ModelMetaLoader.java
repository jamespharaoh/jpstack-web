package wbs.framework.entity.meta;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.camelToHyphen;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.TaskLogger;

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
	Map<String,ModelMetaSpec> modelMetas;

	@Getter
	Map<String,ModelMetaSpec> componentMetas;

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

		DataFromXmlBuilder dataFromXmlBuilder =
			new DataFromXmlBuilder ();

		for (
			Map.Entry<Class<?>,Provider<Object>> modelMetaDataEntry
				: modelMetaDataProviders.entrySet ()
		) {

			Class<?> modelMetaDataClass =
				modelMetaDataEntry.getKey ();

			Provider<?> modelMetaDataProvider =
				modelMetaDataEntry.getValue ();

			dataFromXmlBuilder.registerBuilder (
				modelMetaDataClass,
				modelMetaDataProvider);

		}

		dataFromXml =
			dataFromXmlBuilder.build ();

	}

	private
	void loadSpecs () {

		ImmutableMap.Builder <String, ModelMetaSpec> modelBuilder =
			ImmutableMap.builder ();

		ImmutableMap.Builder <String, ModelMetaSpec> componentBuilder =
			ImmutableMap.builder ();

		TaskLogger taskLog =
			new TaskLogger (
				log);

		pluginManager.plugins ().forEach (
			pluginSpec -> {

			if (
				isNull (
					pluginSpec.models ())
			) {
				return;
			}

			pluginSpec.models ().models ().forEach (
				pluginModelSpec ->
					loadModelMeta (
						taskLog,
						modelBuilder,
						pluginSpec,
						pluginModelSpec.name ()));

			pluginSpec.models ().componentTypes ().forEach (
				pluginComponentTypeSpec ->
					loadModelMeta (
						taskLog,
						componentBuilder,
						pluginSpec,
						pluginComponentTypeSpec.name ()));

		});

		if (taskLog.errors ()) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					taskLog.errorCount ()));

		}

		modelMetas =
			modelBuilder.build ();

		componentMetas =
			componentBuilder.build ();

	}

	private
	void loadModelMeta (
			@NonNull TaskLogger taskLog,
			@NonNull ImmutableMap.Builder <String, ModelMetaSpec> builder,
			@NonNull PluginSpec plugin,
			@NonNull String modelName) {

		String resourceName =
			stringFormat (
				"/%s/model/%s-model.xml",
				plugin.packageName ().replace ('.', '/'),
				camelToHyphen (
					modelName));

		InputStream inputStream =
			getClass ().getResourceAsStream (
				resourceName);

		if (inputStream == null) {

			taskLog.errorFormat (
				"Model meta not found for %s.%s: %s",
				plugin.name (),
				modelName,
				resourceName);

			return;

		}

		ModelMetaSpec spec;

		try {

			spec =
				(ModelMetaSpec)
				dataFromXml.readInputStream (
					inputStream,
					resourceName,
					ImmutableList.<Object>of (
						plugin));

		} catch (Exception exception) {

			taskLog.errorFormatException (
				exception,
				"Error reading model meta for %s.%s: %s",
				plugin.name (),
				modelName,
				resourceName);

			return;

		}

		if (
			stringNotEqualSafe (
				spec.name (),
				modelName)
		) {

			taskLog.errorFormat (
				"Model meta name %s should be %s in %s",
				spec.name (),
				modelName,
				resourceName);

			return;

		}

		builder.put (
			modelName,
			spec);

	}

}
