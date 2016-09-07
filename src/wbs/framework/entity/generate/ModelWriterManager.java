package wbs.framework.entity.generate;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.entity.generate.fields.ModelFieldWriterContext;
import wbs.framework.entity.generate.fields.ModelFieldWriterTarget;

@SingletonComponent ("modelWriterManager")
public
class ModelWriterManager {

	// prototype dependencies

	@Inject
	Provider <BuilderFactory> builderFactoryProvider;

	// collection dependencies

	@Inject
	@ModelWriter
	Map <Class <?>, Provider <Object>> modelWriterProviders;

	// state

	Builder modelWriter;

	// lifecycle

	@PostConstruct
	public
	void setup () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (
			Map.Entry<Class<?>,Provider<Object>> modelWriterEntry
				: modelWriterProviders.entrySet ()
		) {

			builderFactory.addBuilder (
				modelWriterEntry.getKey (),
				modelWriterEntry.getValue ());

		}

		modelWriter =
			builderFactory.create ();

	}

	// implementation

	public
	void write (
			ModelFieldWriterContext context,
			List<?> sourceItems,
			ModelFieldWriterTarget target) {

		modelWriter.descend (
			context,
			sourceItems,
			target,
			MissingBuilderBehaviour.error);

	}

}
