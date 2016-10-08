package wbs.framework.component.tools;

import static wbs.utils.etc.ReflectionUtils.methodInvokeByName;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class MethodComponentFactory
	implements ComponentFactory {

	@Getter @Setter
	Object factoryComponent;

	@Getter @Setter
	String factoryMethodName;

	@Getter @Setter
	Boolean initialized;

	@Override
	public
	Object makeComponent (
			@NonNull TaskLogger taskLogger) {

		return methodInvokeByName (
			factoryComponent,
			factoryMethodName);

	}

}
