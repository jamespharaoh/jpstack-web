package wbs.framework.entity.meta;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;

@Log4j
public
class ModelFixtureCreator {

	// dependencies

	@Inject
	ModelMetaLoader modelMetaLoader;

	@Inject
	EntityHelper entityHelper;

	// prototype dependencies

	@Inject
	Provider<BuilderFactory> builderFactoryProvider;

	// collection dependencies

	@Inject
	@ModelMetaBuilderHandler
	Map<Class<?>,Provider<Object>> modelMetaBuilderProviders;

	// state

	Builder fixtureBuilder;

	// lifecycle

	@PostConstruct
	public
	void setup () {

		createFixtureBuilder ();

	}

	// implementation

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

	public
	void runModelFixtureCreators (
			@NonNull List<String> arguments) {

		log.info (
			stringFormat (
				"About to create model fixtures"));

		for (
			ModelMetaSpec spec
				: modelMetaLoader.modelMetas ().values ()
		) {

			Model model =
				entityHelper.modelsByName ().get (
					spec.name ());

			fixtureBuilder.descend (
				spec,
				spec.children (),
				model,
				MissingBuilderBehaviour.error);

		}

		log.info (
			stringFormat (
				"All model fixtures created successfully"));

	}

}
