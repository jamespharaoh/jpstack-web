package wbs.console.module;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;

@SingletonComponent ("consoleMetaModuleBuilder")
public
class ConsoleMetaModuleBuilder
	implements Builder {

	// prototype dependencies

	@Inject
	Provider<BuilderFactory> builderFactoryProvider;

	@Inject
	@ConsoleMetaModuleBuilderHandler
	Map<Class<?>,Provider<Object>> consoleMetaModuleBuilders;

	// state

	Builder builder;

	// init

	@PostConstruct
	public
	void init () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (Map.Entry<Class<?>,Provider<Object>> entry
				: consoleMetaModuleBuilders.entrySet ()) {

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
			Object targetObject) {

		builder.descend (
			parentObject,
			childObjects,
			targetObject);

	}

}
