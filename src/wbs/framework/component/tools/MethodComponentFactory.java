package wbs.framework.component.tools;

import static wbs.utils.etc.NumberUtils.equalToOne;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.ReflectionUtils.methodGetByNameRequired;
import static wbs.utils.etc.ReflectionUtils.methodInvoke;

import java.lang.reflect.Method;

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
			@NonNull TaskLogger parentTaskLogger) {

		Method method =
			methodGetByNameRequired (
				factoryComponent.getClass (),
				factoryMethodName);

		if (
			equalToZero (
				method.getParameterCount ())
		) {

			return methodInvoke (
				method,
				factoryComponent);

		} else if (
			equalToOne (
				method.getParameterCount ())
		) {

			return methodInvoke (
				method,
				factoryComponent,
				parentTaskLogger);

		} else {

			throw new RuntimeException ();

		}

	}

}
