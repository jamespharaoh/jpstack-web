package wbs.framework.component.tools;

import static wbs.utils.etc.NumberUtils.equalToOne;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.ReflectionUtils.methodGetByNameRequired;
import static wbs.utils.etc.ReflectionUtils.methodInvoke;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.lang.reflect.Method;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class MethodComponentFactory <ComponentType>
	implements ComponentFactory <ComponentType> {

	// properties

	@Getter @Setter
	Object factoryComponent;

	@Getter @Setter
	String factoryMethodName;

	@Getter @Setter
	Boolean initialized;

	// implementation

	@Override
	public
	ComponentType makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		Method method =
			methodGetByNameRequired (
				factoryComponent.getClass (),
				factoryMethodName);

		if (
			equalToZero (
				method.getParameterCount ())
		) {

			return genericCastUnchecked (
				methodInvoke (
					method,
					factoryComponent));

		} else if (
			equalToOne (
				method.getParameterCount ())
		) {

			return genericCastUnchecked (
				methodInvoke (
					method,
					factoryComponent,
					parentTaskLogger));

		} else {

			throw new RuntimeException ();

		}

	}

}
