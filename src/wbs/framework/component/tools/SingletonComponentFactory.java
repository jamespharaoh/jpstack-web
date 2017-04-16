package wbs.framework.component.tools;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class SingletonComponentFactory
	implements ComponentFactory {

	@Getter @Setter
	Object object;

	@Override
	public
	Object makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		return object;

	}

}
