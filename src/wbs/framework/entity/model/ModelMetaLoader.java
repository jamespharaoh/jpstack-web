package wbs.framework.entity.model;

import static wbs.framework.utils.etc.Misc.camelToHyphen;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.entity.helper.EntityHelper;

import com.google.common.collect.ImmutableMap;

@Log4j
@SingletonComponent ("modelMetaLoader")
public
class ModelMetaLoader {

	// dependencies

	@Inject
	EntityHelper entityHelper;

	// prototype dependencies

	@Inject
	Provider<BuilderFactory> builderFactoryProvider;

	// collection dependencies

	@Inject
	@ModelMetaData
	Map<Class<?>,Provider<Object>> modelMetaDataProviders;

	@Inject
	@ModelMetaBuilderHandler
	Map<Class<?>,Provider<Object>> modelMetaBuilderProviders;

	// state

	DataFromXml dataFromXml;
	Builder fixtureBuilder;

	Map<String,ModelMetaSpec> modelSpecs;

	// lifecycle

	@PostConstruct
	public
	void setup () {

		createDataFromXml ();
		createFixtureBuilder ();
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
	void createFixtureBuilder () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (
			Map.Entry<Class<?>,Provider<Object>> modelMetaBuilderEntry
				: modelMetaBuilderProviders.entrySet ()
		) {

			builderFactory.addBuilder (
				modelMetaBuilderEntry.getKey (),
				modelMetaBuilderEntry.getValue ());

		}

		fixtureBuilder =
			builderFactory.create ();

	}

	private
	void loadSpecs () {

		ImmutableMap.Builder<String,ModelMetaSpec> builder =
			ImmutableMap.<String,ModelMetaSpec>builder ();

		for (
			Model model
				: entityHelper.models ()
		) {

			String resourceName =
				stringFormat (
					"/%s/%s-model.xml",
					model.objectClass ()
						.getPackage ()
						.getName ()
						.replace ('.', '/'),
					camelToHyphen (model.objectName ()));

			InputStream inputStream =
				getClass ().getClassLoader ().getResourceAsStream (
					resourceName);

			if (inputStream == null) {

				log.warn (
					stringFormat (
						"Model meta not found for %s: %s",
						model.objectName (),
						resourceName));

				continue;

			}

			ModelMetaSpec spec =
				(ModelMetaSpec)
				dataFromXml.readInputStream (
					inputStream,
					resourceName);

			builder.put (
				model.objectName (),
				spec);

		}

		modelSpecs = builder.build ();

	}

	public
	void createFixtures () {

		for (
			ModelMetaSpec spec
				: modelSpecs.values ()
		) {

			Model model =
				entityHelper.modelsByName ().get (
					spec.name ());

			fixtureBuilder.descend (
				spec,
				spec.builders (),
				model);

		}

	}

}
