package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.entity.meta.ModelMetaLoader;
import wbs.framework.entity.meta.ModelMetaSpec;

@Log4j
public
class ModelGeneratorTool {

	// dependencies

	@Inject
	ModelMetaLoader modelMetaLoader;

	// prototype dependencies

	@Inject
	Provider<ModelRecordGenerator> modelRecordGeneratorProvider;

	@Inject
	Provider<ModelInterfacesGenerator> modelInterfacesGeneratorProvider;

	// implementation

	public
	void generateModels (
			List<String> params) {

		log.info (
			stringFormat (
				"About to generate %s models",
				modelMetaLoader.modelSpecs ().size ()));

		int recordSuccessCount = 0;
		int recordErrorCount = 0;
		int recordSkipCount = 0;

		int interfacesSuccessCount = 0;
		int interfacesErrorCount = 0;

		for (
			ModelMetaSpec modelMeta
				: modelMetaLoader.modelSpecs ().values ()
		) {

			PluginModelSpec pluginModel =
				modelMeta.pluginModel ();

			PluginSpec plugin =
				pluginModel.plugin ();

			if (modelMeta.type () != null) {

				try {

					modelRecordGeneratorProvider.get ()

						.plugin (
							plugin)

						.pluginModel (
							pluginModel)

						.modelMeta (
							modelMeta)

						.generateRecord ();

					recordSuccessCount ++;

				} catch (Exception exception) {

					log.error (
						stringFormat (
							"Error writing model record for %s",
							modelMeta.name ()),
						exception);

					recordErrorCount ++;

				}

			} else {

				recordSkipCount ++;

			}

			try {

				modelInterfacesGeneratorProvider.get ()

					.plugin (
						plugin)

					.pluginModel (
						pluginModel)

					.modelMeta (
						modelMeta)

					.generateInterfaces ();

				interfacesSuccessCount ++;

			} catch (Exception exception) {

				log.error (
					stringFormat (
						"Error writing model interfaces for %s",
						modelMeta.name ()),
					exception);

				interfacesErrorCount ++;

			}

		}

		log.info (
			stringFormat (
				"Successfully generated %s records and %s interfaces",
				recordSuccessCount,
				interfacesSuccessCount));

		log.warn (
			stringFormat (
				"Skipped %s records which are explicitly defined",
				recordSkipCount));

		if (
			recordErrorCount > 0
			|| interfacesErrorCount > 0
		) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					recordErrorCount + interfacesErrorCount));

		}

	}

}
