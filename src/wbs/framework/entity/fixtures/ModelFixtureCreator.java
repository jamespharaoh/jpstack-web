package wbs.framework.entity.fixtures;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public
class ModelFixtureCreator {

	// singleton dependencies

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	// prototype dependencies

	@PrototypeDependency
	Provider <BuilderFactory> builderFactoryProvider;

	@PrototypeDependency
	@ModelMetaBuilderHandler
	Map <Class <?>, Provider <Object>> modelMetaBuilderProviders;

	// state

	Builder fixtureBuilder;

	// lifecycle

	@NormalLifecycleSetup
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
			Map.Entry <Class <?>, Provider <Object>> modelMetaBuilderEntry
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> arguments) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runModelFixtureCreators");

		) {

			taskLogger.noticeFormat (
				"About to create model fixtures");

			for (
				ModelMetaSpec spec
					: modelMetaLoader.modelMetas ().values ()
			) {

				Model <?> model =
					entityHelper.modelsByName ().get (
						spec.name ());

				fixtureBuilder.descend (
					taskLogger,
					spec,
					spec.children (),
					model,
					MissingBuilderBehaviour.error);

			}

			taskLogger.noticeFormat (
				"All model fixtures created successfully");

		}

	}

}
