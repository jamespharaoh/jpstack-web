package wbs.framework.builder;

import java.util.List;

import wbs.framework.logging.TaskLogger;

public
interface Builder {

	void descend (
			TaskLogger taskLogger,
			Object parentObject,
			List<?> childObjects,
			Object targetObject,
			MissingBuilderBehaviour missingBuilderBehaviour);

	public
	enum MissingBuilderBehaviour {
		ignore,
		error;
	}

}
