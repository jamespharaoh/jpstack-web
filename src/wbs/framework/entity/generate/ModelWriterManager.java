package wbs.framework.entity.generate;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.entity.generate.fields.ModelFieldWriterContext;
import wbs.framework.entity.generate.fields.ModelFieldWriterTarget;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("modelWriterManager")
public
class ModelWriterManager {

	// prototype dependencies

	@PrototypeDependency
	Provider <BuilderFactory> builderFactoryProvider;

	@PrototypeDependency
	@ModelWriter
	Map <Class <?>, Provider <Object>> modelWriterProviders;

	// state

	Builder modelWriter;

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (
			Map.Entry <Class <?>, Provider <Object>> modelWriterEntry
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ModelFieldWriterContext context,
			@NonNull List <?> sourceItems,
			@NonNull ModelFieldWriterTarget target) {

		modelWriter.descend (
			parentTaskLogger,
			context,
			sourceItems,
			target,
			MissingBuilderBehaviour.error);

	}

}
