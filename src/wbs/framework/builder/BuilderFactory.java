package wbs.framework.builder;

import java.util.Map;

import javax.inject.Provider;

public
interface BuilderFactory {

	BuilderFactory addBuilder (
			Class<?> builderClass,
			Provider<?> builderProvider);

	BuilderFactory addBuilders (
			Map<Class<?>,Provider<Object>> builders);

	Builder create ();

}
