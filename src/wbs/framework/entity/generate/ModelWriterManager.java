package wbs.framework.entity.generate;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.entity.generate.fields.ModelFieldWriterContext;
import wbs.framework.entity.generate.fields.ModelFieldWriterTarget;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("modelWriterManager")
public
class ModelWriterManager {

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <BuilderFactory <?, TaskLogger>> builderFactoryProvider;

	@ClassSingletonDependency
	LogContext logContext;

	@PrototypeDependency
	@ModelWriter
	Map <Class <?>, ComponentProvider <Object>> modelWriterProviders;

	// state

	Builder <TaskLogger> modelWriter;

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			modelWriter =
				builderFactoryProvider.provide (
					taskLogger)

				.contextClass (
					TaskLogger.class)

				.addBuilders (
					taskLogger,
					modelWriterProviders)

				.create (
					taskLogger);

		}

	}

	// implementation

	public
	void write (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ModelFieldWriterContext context,
			@NonNull List <?> sourceItems,
			@NonNull ModelFieldWriterTarget target) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"write");

		) {

			modelWriter.descend (
				parentTaskLogger,
				context,
				sourceItems,
				target,
				MissingBuilderBehaviour.error);

		}

	}

}
