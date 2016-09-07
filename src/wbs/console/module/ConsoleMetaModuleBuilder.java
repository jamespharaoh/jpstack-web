package wbs.console.module;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;

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

	@PostConstruct
	public
	void init () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (
			Map.Entry<Class<?>,Provider<Object>> entry
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
			Object parentObject,
			List<?> childObjects,
			Object targetObject,
			MissingBuilderBehaviour missingBuilderBehaviour) {

		builder.descend (
			parentObject,
			childObjects,
			targetObject,
			missingBuilderBehaviour);

	}

}
