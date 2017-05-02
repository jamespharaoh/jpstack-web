package wbs.framework.builder;

import java.util.List;

public
interface Builder <Context> {

	void descend (
			Context taskLogger,
			Object parentObject,
			List <?> childObjects,
			Object targetObject,
			MissingBuilderBehaviour missingBuilderBehaviour);

	public
	enum MissingBuilderBehaviour {
		ignore,
		error;
	}

}
