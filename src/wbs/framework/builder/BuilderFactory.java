package wbs.framework.builder;

import javax.inject.Provider;

public
interface BuilderFactory {

	BuilderFactory addBuilder (
			Class<?> builderClass,
			Provider<?> builderProvider);

	Builder create ();

}
