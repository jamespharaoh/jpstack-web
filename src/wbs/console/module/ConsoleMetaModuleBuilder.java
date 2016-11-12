package wbs.console.module;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleMetaModuleBuilder")
public
class ConsoleMetaModuleBuilder
	implements Builder {

	// prototype dependencies

	@PrototypeDependency
	Provider <BuilderFactory> builderFactoryProvider;

	@PrototypeDependency
	@ConsoleMetaModuleBuilderHandler
	Map <Class <?>, Provider <Object>> consoleMetaModuleBuilders;

	// state

	Builder builder;

	// init

	@NormalLifecycleSetup
	public
	void init () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (
			Map.Entry <Class <?>, Provider <Object>> entry
				: consoleMetaModuleBuilders.entrySet ()
		) {

			builderFactory.addBuilder (
				entry.getKey (),
				entry.getValue ());

		}

		builder =
			builderFactory.create ();

	}

	// builder

	@Override
	public
	void descend (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object parentObject,
			@NonNull List <?> childObjects,
			@NonNull Object targetObject,
			@NonNull MissingBuilderBehaviour missingBuilderBehaviour) {

		builder.descend (
			parentTaskLogger,
			parentObject,
			childObjects,
			targetObject,
			missingBuilderBehaviour);

	}

}
