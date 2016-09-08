package wbs.framework.entity.build;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Provider;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("modelBuilderManager")
public
class ModelBuilderManager {

	// prototype dependencies

	@PrototypeDependency
	Provider <BuilderFactory> builderFactoryProvider;

	@PrototypeDependency
	@ModelBuilder
	Map <Class <?>, Provider <Object>> modelBuilderProviders;

	// state

	Builder modelBuilder;

	// lifecycle

	@PostConstruct
	public
	void setup () {

		modelBuilder =
			builderFactoryProvider.get ()

			.addBuilders (
				modelBuilderProviders)

			.create ();

	}

	// implementation

	public
	void build (
			ModelFieldBuilderContext context,
			List <?> sourceItems,
			ModelFieldBuilderTarget target) {

		modelBuilder.descend (
			context,
			sourceItems,
			target,
			MissingBuilderBehaviour.error);

	}

}
